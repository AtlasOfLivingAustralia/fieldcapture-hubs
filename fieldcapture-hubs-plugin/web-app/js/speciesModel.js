

var speciesFormatters = function() {
    function markMatch (text, term) {
        // Find where the match is
        var match = text.toUpperCase().indexOf(term.toUpperCase());

        var $result = $('<span></span>');

        // If there is no match, move on
        if (match < 0) {
            return $result.text(text);
        }

        // Put in whatever text is before the match
        $result.text(text.substring(0, match));

        // Mark the match
        var $match = $('<span class="select2-rendered__match"></span>');
        $match.text(text.substring(match, match + term.length));

        // Append the matching text
        $result.append($match);

        // Put in whatever is after the match
        $result.append(text.substring(match + term.length));

        return $result;
    }

    var singleLineSpeciesFormatter = function(species) {
        if (species.id == -1) {
            return 'Please select...';
        }
        if (species.scientificName && species.commonName) {
            return species.scientificName + ' (' + species.commonName + ')';
        }
        else if (species.scientificName) {
            return species.scientificName;
        }
        else {
            return species.name;
        }
    };
    var multiLineSpeciesFormatter = function(species, queryTerm) {

        if (!species) return '';

        var result;
        if (species.scientificName && species.commonName) {
            result = $("<div/>");
            if (species.lsid) {
                result.append($('<span/>').append($('<img style="width:75px; height:75px;">').attr('src', 'http://devt.ala.org.au:8087/fieldcapture/species/speciesImage?id='+encodeURIComponent(species.lsid))));
            }
            result.append($('<div style="display:inline-block; padding-left:10px;"></div>').append($("<i></i>").append(markMatch(species.scientificName, queryTerm))).append($("<br/>")).append(markMatch(species.commonName, queryTerm)));

        }
        else if (species.scientificName) {
            result = $("<i></i>").append(markMatch(species.scientificName, queryTerm));
        }
        else {
            result = markMatch(species.name, queryTerm);
        }

        return result;
    };
    return {
        singleLineSpeciesFormatter:singleLineSpeciesFormatter,
        multiLineSpeciesFormatter:multiLineSpeciesFormatter
    }
}();



var speciesSearchEngines = function(config) {

    var speciesId = function (species) {
        if (species.guid || species.lsid) {
            return species.guid || species.lsid;
        }
        return species.name;
    };

    var speciesTokenizer = function (species) {
        var result = [];
        if (species.scientificName) {
            result = result.concat(species.scientificName.split(/\W+/));
        }
        if (species.commonName) {
            result = result.concat(species.commonName.split(/\W+/));
        }
        return result;
    };

    var select2Transformer = function (speciesArray) {
        if (!speciesArray) {
            return [];
        }
        for (var i in speciesArray) {
            speciesArray[i].id = speciesId(speciesArray[i]);
        }
        return speciesArray;
    };

    var engines = {};

    function engineKey(listId, alaFallback) {
        return listId || '' + alaFallback;
    }

    function get(listId, alaFallback) {
        var engine = engines[engineKey(listId, alaFallback)];
        if (!engine) {
            engine = define(listId, alaFallback);
        }
        return engine;
    };

    function define(listId, alaFallback) {
        var options = {
            datumTokenizer: speciesTokenizer,
            queryTokenizer: Bloodhound.tokenizers.nonword,
            identify: speciesId
        };
        if (listId) {
            options.prefetch = {
                url: config.speciesListUrl + '?druid='+listId+'&includeKvp=true',
                cache: false,
                transform: function (results) {
                    return select2Transformer(results);
                }
            };
        }
        if (alaFallback) {
            options.remote = {
                url: config.searchBieUrl + '?q=%',
                wildcard: '%',
                transform: function (results) {
                    return select2Transformer(results.autoCompleteList);
                }
            };
        }

        return new Bloodhound(options);
    };

    return {
        get:get,
        speciesId:speciesId
    };
}(fcConfig || {});


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

    self.toJS = function() {
        return {
            guid:self.guid(),
            name:self.name(),
            scientificName:self.scientificName(),
            commonName:self.commonName(),
            listId:self.listId
        }
    };

    self.loadData = function(data) {
        if (!data) data = {};
        self.guid(orBlank(data.guid || data.lsid));
        self.name(orBlank(data.name));
        self.listId(orBlank(data.listId));
        self.scientificName(orBlank(data.scientificName));
        self.commonName(orBlank(data.commonName));

        self.transients.textFieldValue(self.name());
        if (self.guid() && !options.printable) {

            var profileUrl = fcConfig.bieUrl + '/species/' + encodeURIComponent(self.guid());
            $.ajax({
                url: fcConfig.speciesProfileUrl+'?id=' + encodeURIComponent(self.guid()),
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

    self.formatSpeciesListItem = speciesFormatters.multiLineSpeciesFormatter;
    self.formatSelectedSpecies = speciesFormatters.singleLineSpeciesFormatter;
    self.engine = function() {return speciesSearchEngines.get(self.listId() || 'dr7394', true)};
    self.id = function() {
        return speciesSearchEngines.speciesId({guid:self.guid(), name:self.name()});
    }
};

$.fn.select2.amd.define('select2/species', [
    'select2/data/ajax',
    'select2/utils'
], function (BaseAdapter, Utils) {
    function SpeciesAdapter($element, options) {
        this.$element = $element;
        this.queryHolder = options.get('queryHolder');
        this.model = options.get("model");
        this.engine = this.model.engine();
        SpeciesAdapter.__super__.constructor.call(this, $element, options);
    }

    Utils.Extend(SpeciesAdapter, BaseAdapter);

    SpeciesAdapter.prototype.query = function (params, callback) {
        var self = this;
        self.queryHolder.queryTerm = params.term;
        var noLocalResults = false;
        if (params.term) {
            self.engine.search(
                params.term, function (resultArr) {
                    if (resultArr.length > 0) {
                        callback({results: [{text: "Species List", children: resultArr}]});
                    }
                    else {
                        noLocalResults = true;
                    }

                },
                function (resultArr) {
                    var results = {results: [{text: "Atlas of Living Australia", children: resultArr}]};
                    if (noLocalResults) {
                        callback(results);
                    }
                    else {
                        self.trigger("results:append", {data: results, query: params});
                    }
                });
        }
        else {
            callback({results: [{text: "Species List", children: self.engine.all()}]});
        }
    };

    SpeciesAdapter.prototype.current = function (callback) {
        var data = this.model.toJS();
        data.id = speciesSearchEngines.speciesId(data);
        if (!data.id) {
            data = {id: -1, text: "Please select..."}
        }
        callback([data]);
    };

    return SpeciesAdapter;
});
