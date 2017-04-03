
/**
 * Manages the species data type in the output model.
 * Allows species information to be searched for and displayed.
 */
var SpeciesViewModel = function(data, options) {

    var self = this;

    self.guid = ko.observable();
    self.name = ko.observable();
    self.scientificName = ko.observable();
    self.commonName = ko.observable();

    self.listId = ko.observable();
    self.transients = {};
    self.transients.speciesInformation = ko.observable();
    self.transients.editing = ko.observable(false);
    self.transients.textFieldValue = ko.observable();
    self.transients.bioProfileUrl =  ko.computed(function (){
        return  fcConfig.bieUrl + '/species/' + self.guid();
    });

    self.transients.speciesSearchUrl = options.speciesSearchUrl+'&dataFieldName='+options.dataFieldName;

    self.speciesSelected = function(event, data) {
        self.loadData(data);
        self.transients.editing(!data.name);
    };

    self.textFieldChanged = function(newValue) {
        if (newValue != self.name()) {
            self.transients.editing(true);
        }
    };

    self.loadData = function(data) {
        if (!data) data = {};
        self.guid(orBlank(data.guid));
        self.name(orBlank(data.name));
        self.listId(orBlank(data.listId));
        self.scientificName(orBlank(data.scientificName));
        self.commonName(orBlank(data.commonName));

        self.transients.textFieldValue(self.name());
        if (self.guid() && !options.printable) {

            var profileUrl = fcConfig.bieUrl + '/species/' + encodeURIComponent(self.guid());
            $.ajax({
                url: fcConfig.speciesProfileUrl+'/' + encodeURIComponent(self.guid()),
                dataType: 'json',
                success: function (data) {
                    var profileInfo = '<a href="'+profileUrl+'" target="_blank">';
                    var imageUrl = data.thumbnail || (data.taxonConcept && data.taxonConcept.smallImageUrl);

                    if (imageUrl) {
                        profileInfo += "<img title='Click to show profile' class='taxon-image ui-corner-all' src='"+imageUrl+"'>";
                    }
                    else {
                        profileInfo += "No profile image available";
                    }
                    profileInfo += "</a>";
                    self.transients.speciesInformation(profileInfo);
                },
                error: function(request, status, error) {
                    console.log(error);
                }
            });
        }
        else {
            self.transients.speciesInformation("No profile information is available.");
        }

    };

    if (data) {
        self.loadData(data);
    }
    self.focusLost = function(event) {
        self.transients.editing(false);
        if (self.name()) {
            self.transients.textFieldValue(self.name());
        }
        else {
            self.transients.textFieldValue('');
        }
    };


};