function ProjectActivitiesViewModel(pActivities, pActivityForms, projectId, sites) {
    var self = this;

    self.speciesOptions =  [{id: 'ALL_SPECIES', name:'All species'},{id:'SINGLE_SPECIES', name:'Single species'}, {id:'GROUP_OF_SPECIES',name:'A selection or group of species'}];
    self.projectId = ko.observable(projectId);
    self.pActivityForms = pActivityForms;
    self.sites = sites;
    self.formNames = ko.observableArray($.map(pActivityForms ? pActivityForms : [], function (obj, i) {
        return obj.name;
    }));

    self.projectActivities = ko.observableArray($.map(pActivities, function (obj, i) {
        return new ProjectActivity(obj, pActivityForms, self.projectId(), i == 0 ? true : false, self.sites);
    }));

    self.addProjectActivity = function() {
        self.reset();
        self.projectActivities.push(new ProjectActivity([], self.pActivityForms, self.projectId(), true, self.sites));
        initialiseValidator();
    };

    self.reset = function(){
        $.each(self.projectActivities(), function(i, obj){
            obj.current(false);
        });
    };

    self.setCurrent = function(pActivity) {
        self.reset();
        pActivity.current(true);
    };

    self.saveAccess = function(access){
        var caller = "access";
        return self.genericUpdate(self.current().asJSON(caller), caller);
    };

    self.saveForm = function(){
        var caller = "form";
        return self.genericUpdate(self.current().asJSON(caller), caller);
    };

    self.saveInfo = function(){
        var caller = "info";
        return self.genericUpdate(self.current().asJSON(caller), caller);
    };

    self.saveSpecies = function(){
        var caller = "species";
        return self.genericUpdate(self.current().asJSON(caller), caller);
    };

    self.saveSites = function(){
        var caller = "sites";
        return self.genericUpdate(self.current().asJSON(caller), caller);
    };


    self.deleteProjectActivity = function() {
        bootbox.confirm("Are you sure you want to delete the survey?", function (result) {
            if (result) {
                var that = this;

                var pActivity;
                $.each(self.projectActivities(), function(i, obj){
                    if(obj.current()){
                        obj.status("deleted");
                        pActivity = obj;
                    }
                });

                if(pActivity.projectActivityId() === undefined){
                    self.projectActivities.remove(pActivity);
                    if(self.projectActivities().length > 0){
                        self.projectActivities()[0].current(true);
                    }
                    showAlert("Successfully deleted.", "alert-success",  'project-activities-info-result-placeholder');
                }
                else{
                    self.genericUpdate(self.current().asJSON("info"), "info");
                }
            }
        });

    };

    self.current = function (){
        var pActivity;
        $.each(self.projectActivities(), function(i, obj){
            if(obj.current()){
                pActivity = obj;
            }
        });
        return pActivity;
    };

    self.genericUpdate = function(model, caller, message){
        if (!$('#project-activities-'+caller+'-validation').validationEngine('validate')){
            return false;
        }

        message = typeof message !== 'undefined' ? message : 'Successfully updated';
        var pActivity = self.current();
        var url = pActivity.projectActivityId() ? fcConfig.projectActivityUpdateUrl + "/" +
        pActivity.projectActivityId() : fcConfig.projectActivityCreateUrl;

        var divId = 'project-activities-'+ caller +'-result-placeholder';

        if(caller != "info" && pActivity.projectActivityId() === undefined){
            showAlert("Please save 'Survey Info' details before applying other constraints.", "alert-error",  divId);
            return;
        }

        $.ajax({
            url: url,
            type: 'POST',
            data: model,
            contentType: 'application/json',
            success: function (data) {

                if (data.error) {
                    showAlert("Error :" + data.text, "alert-error", divId);
                }
                else if(data.resp && data.resp.projectActivityId) {
                    $.each(self.projectActivities(), function(i, obj){
                        if(obj.current()){
                            obj.projectActivityId(data.resp.projectActivityId);
                        }
                    });
                    showAlert(message, "alert-success",  divId);
                }
                else{
                    if(pActivity.status() == "deleted"){
                        self.projectActivities.remove(pActivity);
                        if(self.projectActivities().length > 0){
                            self.projectActivities()[0].current(true);
                        }
                        showAlert(message, "alert-success",  divId);
                    }else{
                        showAlert(message, "alert-success",  divId);
                    }
                }
            },
            error: function (data) {
                var status = data.status;
                showAlert("Error : An unhandled error occurred" + data.status, "alert-error", divId);
            }
        });
    };
}

var ProjectActivity = function (o, pActivityForms, projectId, selected, sites){
    var self = this;
    self.projectActivityId = ko.observable(o.projectActivityId);
    self.projectId = ko.observable(o.projectId ? o.projectId  : projectId);
    self.name = ko.observable(o.name ? o.name : "Survey name");
    self.description = ko.observable(o.description);
    self.status = ko.observable(o.status ? o.status : "active");
    self.startDate = ko.observable(o.startDate).extend({simpleDate:false});
    self.endDate = ko.observable(o.endDate).extend({simpleDate:false});
    self.commentsAllowed = ko.observable(o.commentsAllowed ? o.commentsAllowed : false);
    self.published = ko.observable(o.published ? o.published : false);
    self.current = ko.observable(selected);
    self.pActivityFormName = ko.observable(o.pActivityFormName);
    self.species = new SpeciesConstraintViewModel(o.species);
    self.transients = {};
    self.transients.siteSelectUrl = ko.observable(fcConfig.siteSelectUrl +"&pActivityId="+self.projectActivityId());
    self.transients.siteCreateUrl = ko.observable(fcConfig.siteCreateUrl);
    self.transients.siteUploadUrl = ko.observable(fcConfig.siteUploadUrl);

    self.transients.warning = ko.computed(function(){
        return self.projectActivityId() === undefined ? true : false;
    });
    self.sites = ko.observableArray($.map(sites ? sites : [], function (obj, i) {
        return new SiteList(obj, o.sites);
    }));

    var images = [];
    $.each(pActivityForms, function(index, form){
        if(form.name == self.pActivityFormName()){
            images = form.images ? form.images : [];
        }
    });

    self.pActivityFormImages =  ko.observableArray($.map(images, function (obj, i) {
        return new ImagesViewModel(obj);
    }));

    self.pActivityForms = pActivityForms;

    self.pActivityFormName.subscribe(function(formName) {
        self.pActivityFormImages.removeAll();
        $.each(self.pActivityForms, function(index, form){
            if(form.name == formName && form.images){
                for(var i =0; i < form.images.length; i++){
                    self.pActivityFormImages.push(new ImagesViewModel(form.images[i]));
                }
            }
        });
    });

    self.asJSON = function(by){
        var jsData;
        if(by == "access"){
            jsData = {};
            jsData.access =  ko.mapping.toJS(self.access, {ignore:[]});
        }
        else if(by == "form"){
            jsData = {};
            jsData.pActivityFormName = self.pActivityFormName();
        }
        else if(by == "info"){
            jsData = ko.mapping.toJS(self, {ignore:['current','pActivityForms','pActivityFormImages', 'access', 'species','sites','transients']});
        }
        else if(by == "species"){
            jsData = {};
            jsData.species = self.species.asJson();
        }
        else if(by == "sites"){
            jsData = {};
            var sites = [];
            $.each(self.sites(), function(index, site){
                if(site.added()){
                    sites.push(site.siteId());
                }
            });
            jsData.sites = sites;
        }

        return JSON.stringify(jsData, function (key, value) { return value === undefined ? "" : value; });
    }
};

var SiteList = function(o, surveySites){
    var self = this;
    if(!o) o = {};
    if(!surveySites) surveySites = {};

    self.siteId = ko.observable(o.siteId);
    self.name = ko.observable(o.name);
    self.added = ko.observable(false);
    self.siteUrl = ko.observable(fcConfig.siteViewUrl + "/" + self.siteId())

    self.addSite = function(){
        self.added(true);
    };
    self.removeSite = function(){
        self.added(false);
    };

    self.load = function(surveySites){
        $.each(surveySites, function( index, siteId ) {
            if(siteId == self.siteId()){
                self.added(true);
            }
        });
    };
    self.load(surveySites);

};

var SpeciesConstraintViewModel = function (o){
    var self = this;
    if(!o) o = {};

    self.type = ko.observable(o.type);
    self.allSpeciesLists  = new SpeciesListsViewModel();
    self.singleSpecies = new SpeciesViewModel(o.singleSpecies ? o.singleSpecies : [],[]);
    self.speciesLists = ko.observableArray($.map(o.speciesLists ? o.speciesLists : [], function (obj, i) {
        return new SpeciesList(obj);
    }));
    self.newSpeciesLists = new SpeciesList();

    self.transients = {};
    self.transients.bioProfileUrl =  ko.computed(function (){
        return  fcConfig.bieUrl + '/species/' + self.singleSpecies.guid();
    });
    self.transients.allowedListTypes = [
        {id:'SPECIES_CHARACTERS', name:'SPECIES_CHARACTERS'},
        {id:'CONSERVATION_LIST', name:'CONSERVATION_LIST'},
        {id:'SENSITIVE_LIST', name:'SENSITIVE_LIST'},
        {id:'LOCAL_LIST', name:'LOCAL_LIST'},
        {id:'COMMON_TRAIT', name:'COMMON_TRAIT'},
        {id:'COMMON_HABITAT', name:'COMMON_HABITAT'},
        {id:'TEST', name:'TEST'},
        {id:'OTHER', name:'OTHER'}];

    self.transients.showAddSpeciesLists = ko.observable(false);
    self.transients.showExistingSpeciesLists = ko.observable(false);
    self.transients.toggleShowAddSpeciesLists = function (){
        self.transients.showAddSpeciesLists(!self.transients.showAddSpeciesLists());
    };
    self.transients.toggleShowExistingSpeciesLists = function (){
        self.transients.showExistingSpeciesLists(!self.transients.showExistingSpeciesLists());
        if(self.transients.showExistingSpeciesLists()){
            self.allSpeciesLists.loadAllSpeciesLists();
        }
    };

    self.addSpeciesLists = function (lists){
        lists.transients.check(true);
        self.speciesLists.push(lists);
    };
    self.removeSpeciesLists = function (lists){
        self.speciesLists.remove(lists);
    };
    self.groupInfoVisible = ko.computed(function() {
        return (self.type() == "GROUP_OF_SPECIES");
    });
    self.singleInfoVisible = ko.computed(function() {
        return (self.type() == "SINGLE_SPECIES");
    });
    self.type.subscribe(function(type) {
        if(self.type() == "SINGLE_SPECIES"){
        }else if(self.type() == "GROUP_OF_SPECIES"){
        }
    });

    self.asJson = function(){
        var jsData = {};
        if(self.type() == "ALL_SPECIES"){
            jsData.type = self.type();
        }
        else if(self.type() == "SINGLE_SPECIES"){
            jsData.type = self.type();
            jsData.singleSpecies = ko.mapping.toJS(self.singleSpecies, {ignore:['transients']});
        }
        else if(self.type() == "GROUP_OF_SPECIES"){
            jsData.type = self.type();
            jsData.speciesLists = ko.mapping.toJS(self.speciesLists, {ignore:['listType','fullName','itemCount', 'description', 'listType','allSpecies','transients']});
        }
        return jsData;
    };

    self.saveNewSpeciesName = function(){
        if (!$('#project-activities-species-validation').validationEngine('validate')){
            return;
        }

        var jsData = {};
        jsData.listName = self.newSpeciesLists.listName();
        jsData.listType = self.newSpeciesLists.listType();
        jsData.description = self.newSpeciesLists.description();
        jsData.listItems = "";

        var lists = ko.mapping.toJS(self.newSpeciesLists);
        $.each(lists.allSpecies, function( index, species ) {
            if(index == 0){
                jsData.listItems = species.name;
            }else{
                jsData.listItems =  jsData.listItems + "," + species.name;
            }
        });
        // Add bulk species names.
        if(jsData.listItems == "") {
            jsData.listItems = self.newSpeciesLists.transients.bulkSpeciesNames();
        }else{
            jsData.listItems = "," + self.newSpeciesLists.transients.bulkSpeciesNames();
        }

        var model = JSON.stringify(jsData, function (key, value) { return value === undefined ? "" : value; });
        var divId = 'project-activities-species-result-placeholder';
        $("#addNewSpecies-status").show();
        $.ajax({
            url: fcConfig.addNewSpeciesListsUrl,
            type: 'POST',
            data: model,
            contentType: 'application/json',
            success: function (data) {
                if (data.error) {
                    showAlert("Error :" + data.error, "alert-error", divId);
                }
                else {
                    showAlert("Successfully added the new species list - "+self.newSpeciesLists.listName() +" ("+ data.id+")", "alert-success", divId);
                    self.newSpeciesLists.dataResourceUid(data.id);
                    self.speciesLists.push(new SpeciesList(ko.mapping.toJS(self.newSpeciesLists)));
                    self.newSpeciesLists = new SpeciesList();
                    self.transients.toggleShowAddSpeciesLists();
                }
                $("#addNewSpecies-status").hide();
            },
            error: function (data) {
                showAlert("Error : An unhandled error occurred" + data.status, "alert-error", divId);
                $("#addNewSpecies-status").hide();
            }
        });
    };

};

var SpeciesListsViewModel = function(o){
    var self = this;
    if(!o) o = {};

    self.allSpeciesListsToSelect = ko.observableArray();
    self.offset = ko.observable(0);
    self.max = ko.observable(100);
    self.listCount = ko.observable();
    self.isNext = ko.computed(function() {
        var status = false;
        if(self.listCount() > 0){
            var maxOffset = self.listCount() / self.max();
            if(self.offset() < Math.ceil(maxOffset)-1) {
                status = true;
            }
        }
        return status;
    });

    self.isPrevious = ko.computed(function() {
        return (self.offset() > 0);
    });

    self.next = function(){
        if(self.listCount() > 0){
            var maxOffset = self.listCount() / self.max();
            if(self.offset() < Math.ceil(maxOffset)-1) {
                self.offset(self.offset()+1);
                self.loadAllSpeciesLists();
            }
        }
    };

    self.previous = function(){
        if(self.offset() > 0){
            var newOffset = self.offset() - 1;
            self.offset(newOffset);
            self.loadAllSpeciesLists();
        }
    };

    self.loadAllSpeciesLists = function (){
        var url = fcConfig.speciesListUrl + "?sort=listName&offset="+self.offset()+"&max="+self.max();
        $.ajax({
            url: url,
            type: 'GET',
            contentType: 'application/json',
            success: function (data) {
                if (data.error) {
                    showAlert("Error :" + data.text, "alert-error", divId);
                }
                else {
                    self.listCount(data.listCount);
                    self.allSpeciesListsToSelect($.map(data.lists ? data.lists : [], function (obj, i) {
                            return new SpeciesList(obj);
                        })
                    );
                }
            },
            error: function (data) {
                var status = data.status;
                showAlert("Error : An unhandled error occurred" + data.status, "alert-error", divId);
            }
        });
    };

};

var SpeciesList = function(o){
    var self = this;
    if(!o) o = {};

    self.listName = ko.observable(o.listName);
    self.dataResourceUid = ko.observable(o.dataResourceUid);

    self.listType = ko.observable(o.listType);
    self.fullName = ko.observable(o.fullName);
    self.itemCount = ko.observable(o.itemCount);

    // Only used for new species.
    self.description = ko.observable(o.description);
    self.listType = ko.observable(o.listType);
    self.allSpecies = ko.observableArray();
    self.addNewSpeciesName = function(){
        self.allSpecies.push(new SpeciesViewModel([],[]));
    };
    self.removeNewSpeciesName = function(species){
        self.allSpecies.remove(species);
    };

    self.transients = {};
    self.transients.bulkSpeciesNames = ko.observable(o.bulkSpeciesNames);
    self.transients.url  = ko.observable(fcConfig.speciesListsServerUrl + "/speciesListItem/list/" + o.dataResourceUid);
    self.transients.check = ko.observable(false);

};

var ImagesViewModel = function(image){
    var self = this;
    if(!image) image = {};

    self.thumbnail = ko.observable(image.thumbnail);
    self.url = ko.observable(image.url);
};

function initialiseValidator() {
    $('#project-activities-info-validation').validationEngine();
    $('#project-activities-species-validation').validationEngine();
    $('#project-activities-form-validation').validationEngine();
    $('#project-activities-access-validation').validationEngine();
}
