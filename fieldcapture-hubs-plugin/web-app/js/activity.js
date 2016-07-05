function ActivityViewModel (act, site, project, metaModel, themes) {
    var self = this;
    self.activityId = act.activityId;
    self.description = ko.observable(act.description);
    self.notes = ko.observable(act.notes);
    self.startDate = ko.observable(act.startDate || act.plannedStartDate).extend({simpleDate: false});
    self.endDate = ko.observable(act.endDate || act.plannedEndDate).extend({simpleDate: false});
    self.eventPurpose = ko.observable(act.eventPurpose);
    self.fieldNotes = ko.observable(act.fieldNotes);
    self.associatedProgram = ko.observable(act.associatedProgram);
    self.associatedSubProgram = ko.observable(act.associatedSubProgram);
    self.projectStage = ko.observable(act.projectStage || "");
    self.progress = ko.observable(act.progress || 'started');
    self.mainTheme = ko.observable(act.mainTheme);
    self.type = ko.observable(act.type);
    self.siteId = ko.observable(act.siteId);
    self.projectId = act.projectId;
    self.transients = {};
    self.transients.site = site;
    self.transients.project = project;
    self.transients.metaModel = metaModel || {};
    self.transients.activityProgressValues = ['planned','started','finished'];
    self.transients.themes = $.map(themes || [], function (obj, i) { return obj.name });
    self.goToProject = function () {
        if (self.projectId) {
            document.location.href = fcConfig.projectViewUrl + self.projectId;
        }
    };
    self.goToSite = function () {
        if (self.siteId()) {
            document.location.href = fcConfig.siteViewUrl + self.siteId();
        }
    };
    if (metaModel.supportsPhotoPoints) {
        self.transients.photoPointModel = ko.observable(new PhotoPointViewModel(site, act));
    }
}

var PhotoPointViewModel = function(site, activity, config) {

    var self = this;

    self.site = site;
    self.photoPoints = ko.observableArray();

    if (site && site.poi) {

        $.each(site.poi, function(index, obj) {
            var photos = ko.utils.arrayFilter(activity.documents, function(doc) {
                return doc.siteId === site.siteId && doc.poiId === obj.poiId;
            });
            self.photoPoints.push(photoPointPhotos(site, obj, activity.activityId, photos, config));
        });
    }

    self.removePhotoPoint = function(photoPoint) {
        self.photoPoints.remove(photoPoint);
    };

    self.addPhotoPoint = function() {
        self.photoPoints.push(photoPointPhotos(site, null, activity.activityId, [], config));
    };

    self.modelForSaving = function() {
        var siteId = site?site.siteId:''
        var toSave = {siteId:siteId, photos:[], photoPoints:[]};

        $.each(self.photoPoints(), function(i, photoPoint) {

            if (photoPoint.isNew()) {
                var newPhotoPoint = photoPoint.photoPoint.modelForSaving();
                toSave.photoPoints.push(newPhotoPoint);
                $.each(photoPoint.photos(), function(i, photo) {
                    if (!newPhotoPoint.photos) {
                        newPhotoPoint.photos = [];
                    }
                    newPhotoPoint.photos.push(photo.modelForSaving());
                });
            }
            else {
                $.each(photoPoint.photos(), function(i, photo) {
                    toSave.photos.push(photo.modelForSaving());
                });
            }

        });
        return toSave;
    };

    self.isDirty = function() {
        var isDirty = false;
        $.each(self.photoPoints(), function(i, photoPoint) {
            isDirty = isDirty || photoPoint.isDirty();
        });
        return isDirty;
    };

    self.reset = function() {};


};

var photoPointPOI = function(data) {
    if (!data) {
        data = {
            geometry:{}
        };
    }
    var name = ko.observable(data.name);
    var description = ko.observable(data.description);
    var lat = ko.observable(data.geometry.decimalLatitude);
    var lng = ko.observable(data.geometry.decimalLongitude);
    var bearing = ko.observable(data.geometry.bearing);


    return {
        poiId:data.poiId,
        name:name,
        description:description,
        geometry:{
            type:'Point',
            decimalLatitude:lat,
            decimalLongitude:lng,
            bearing:bearing,
            coordinates:[lng, lat]
        },
        type:'photopoint',
        modelForSaving:function() { return ko.toJS(this); }
    }
};

var photoPointPhotos = function(site, photoPoint, activityId, existingPhotos, config) {

    var files = ko.observableArray();
    var photos = ko.observableArray();
    var isNewPhotopoint = !photoPoint;
    var isDirty = isNewPhotopoint;

    var photoPoint = photoPointPOI(photoPoint);

    $.each(existingPhotos, function(i, photo) {
        photos.push(photoPointPhoto(photo));
    });


    files.subscribe(function(newValue) {
        var f = newValue.splice(0, newValue.length);
        for (var i=0; i<f.length; i++) {

            var data = {
                thumbnailUrl:f[i].thumbnail_url,
                url:f[i].url,
                contentType:f[i].contentType,
                filename:f[i].name,
                filesize:f[i].size,
                dateTaken:f[i].isoDate,
                lat:f[i].decimalLatitude,
                lng:f[i].decimalLongitude,
                poiId:photoPoint.poiId,
                siteId:site.siteId,
                activityId:activityId,
                name:site.name+' - '+photoPoint.name(),
                type:'image'


            };
            isDirty = true;
            if (isNewPhotopoint && data.lat && data.lng && !photoPoint.geometry.decimalLatitude() && !photoPoint.geometry.decimalLongitude()) {
                photoPoint.geometry.decimalLatitude(data.lat);
                photoPoint.geometry.decimalLongitude(data.lng);
            }

            photos.push(photoPointPhoto(data));
        }
    });


    return {
        photoPoint:photoPoint,
        photos:photos,
        files:files,

        uploadConfig : {
            url: (config && config.imageUploadUrl) || fcConfig.imageUploadUrl,
            target: files
        },
        removePhoto : function (photo) {
            if (photo.documentId) {
                photo.status('deleted');
            }
            else {
                photos.remove(photo);
            }
        },
        template : function(photoPoint) {
            return isNewPhotopoint ? 'editablePhotoPoint' : 'readOnlyPhotoPoint'
        },
        isNew : function() { return isNewPhotopoint },
        isDirty: function() {
            if (isDirty) {
                return true;
            };
            var tmpPhotos = photos();
            for (var i=0; i<tmpPhotos.length; i++) {
                if (tmpPhotos[i].dirtyFlag.isDirty()) {
                    return true;
                }
            }
            return false;
        }

    }
}

var photoPointPhoto = function(data) {
    if (!data) {
        data = {};
    }
    data.role = 'photoPoint';
    var result = new DocumentViewModel(data);
    result.dateTaken = ko.observable(data.dateTaken).extend({simpleDate:false});
    result.formattedSize = formatBytes(data.filesize);

    for (var prop in data) {
        if (!result.hasOwnProperty(prop)) {
            result[prop]= data[prop];
        }
    }
    var docModelForSaving = result.modelForSaving;
    result.modelForSaving = function() {
        var js = docModelForSaving();
        delete js.lat;
        delete js.lng;
        delete js.thumbnailUrl;
        delete js.formattedSize;

        return js;
    };
    result.dirtyFlag = ko.dirtyFlag(result, false);

    return result;
};
