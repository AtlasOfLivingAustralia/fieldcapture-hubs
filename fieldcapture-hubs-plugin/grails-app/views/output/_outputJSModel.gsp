<r:script>
    <g:set var="outputNameAsIdentifer" value="${fc.toSingleWord([name: outputName])}"/>
    // load dynamic models - usually objects in a list
    <md:jsModelObjects model="${model}" site="${site}" speciesLists="${speciesLists}" edit="${edit}" printable="${printable?:''}"/>

    window["${outputNameAsIdentifer + 'ViewModel'}"] = function (output, site, config) {
        var self = this;
        if (!output) {
            output = {};
        }
        self.name = "${outputName}";
        self.outputId = orBlank(output.outputId);

        self.data = {};
        self.transients = {};
        var notCompleted = output.outputNotCompleted;

        if (notCompleted === undefined) {
            notCompleted = config.collapsedByDefault;
        }

        self.transients.selectedSite = ko.observable(site);
        self.outputNotCompleted = ko.observable(notCompleted);
        self.transients.optional = config.optional || false;
        self.transients.questionText = config.optionalQuestionText || 'Not applicable';
        self.transients.dummy = ko.observable();

        // add declarations for dynamic data
        <md:jsViewModel model="${model}"  output="${outputName}"  edit="${edit}" printable="${printable?:''}"/>

        // this will be called when generating a savable model to remove transient properties
        self.removeBeforeSave = function (jsData) {
            // add code to remove any transients added by the dynamic tags
            <md:jsRemoveBeforeSave model="${model}"/>
            delete jsData.activityType;
            delete jsData.transients;
            return jsData;
        };

        // this returns a JS object ready for saving
        self.modelForSaving = function () {
            // get model as a plain javascript object
            var jsData = ko.mapping.toJS(self, {'ignore':['transients']});
            if (self.outputNotCompleted()) {
                jsData.data = {};
            }

            // get rid of any transient observables
            return self.removeBeforeSave(jsData);
        };

        // this is a version of toJSON that just returns the model as it will be saved
        // it is used for detecting when the model is modified (in a way that should invoke a save)
        // the ko.toJSON conversion is preserved so we can use it to view the active model for debugging
        self.modelAsJSON = function () {
            return JSON.stringify(self.modelForSaving());
        };

        self.loadData = function (data, documents) {
            // load dynamic data
            <md:jsLoadModel model="${model}"/>

            // if there is no data in tables then add an empty row for the user to add data
            if (typeof self.addRow === 'function' && self.rowCount() === 0) {
                self.addRow();
            }
            self.transients.dummy.notifySubscribers();
        };

        self.attachDocument = function(target) {
            var url = config.documentUpdateUrl || fcConfig.documentUpdateUrl;
            showDocumentAttachInModal(url, new DocumentViewModel({role:'information'},{key:'activityId', value:output.activityId}), '#attachDocument')
                    .done(function(result) {
                        target(new DocumentViewModel(result))
                    });
        };
        self.editDocumentMetadata = function(document) {
            var url = (config.documentUpdateUrl || fcConfig.documentUpdateUrl) + "/" + document.documentId;
            showDocumentAttachInModal(url, document, '#attachDocument');
        };
        self.deleteDocument = function(document) {
            document.status('deleted');
            var url = (config.documentDeleteUrl || fcConfig.documentDeleteUrl)+'/'+document.documentId;
            $.post(url, {}, function() {});

        };
    };
</r:script>
