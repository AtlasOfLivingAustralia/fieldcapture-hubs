/**
 * User: MEW
 * Date: 18/06/13
 * Time: 2:24 PM
 */

//iterates over the outputs specified in the meta-model and builds a temp object for
// each containing the name, and the scores and id of any matching outputs in the data
ko.bindingHandlers.foreachModelOutput = {
    init: function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
        if (valueAccessor() === undefined) {
            var dummyRow = {name: 'No model was found for this activity', scores: [], outputId: '', editLink:''};
            ko.applyBindingsToNode(element, { foreach: [dummyRow] });
            return { controlsDescendantBindings: true };
        }
        var metaOutputs = ko.utils.unwrapObservable(valueAccessor()),// list of String names of outputs
            activity = bindingContext.$data,// activity data
            transformedOutputs = [];//created list of temp objects

        $.each(metaOutputs, function (i, name) { // for each output name
            var scores = [],
                outputId = '',
                editLink = fcConfig.serverUrl + "/output/";

            // search for corresponding outputs in the data
            $.each(activity.outputs(), function (i,output) { // iterate output data in the activity to
                                                             // find any matching the meta-model name
                if (output.name === name) {
                    outputId = output.outputId;
                    $.each(output.scores, function (k, v) {
                        scores.push({key: k, value: v});
                    });
                }
            });

            if (outputId) {
                // build edit link
                editLink += 'edit/' + outputId +
                    "?returnTo=" + returnTo;
            } else {
                // build create link
                editLink += 'create?activityId=' + activity.activityId +
                    '&outputName=' + encodeURIComponent(name) +
                    "&returnTo=" + returnTo;
            }
            // build the array that we will actually iterate over in the inner template
            transformedOutputs.push({name: name, scores: scores, outputId: outputId,
                editLink: editLink});
        });

        // re-cast the binding to iterate over our new array
        ko.applyBindingsToNode(element, { foreach: transformedOutputs });
        return { controlsDescendantBindings: true };
    }
};
ko.virtualElements.allowedBindings.foreachModelOutput = true;

// handle activity accordion
$('#activities').
    on('show', 'div.collapse', function() {
        $(this).parents('tr').prev().find('td:first-child a').empty()
            .html("&#9660;").attr('title','hide').parent('a').tooltip();
    }).
    on('hide', 'div.collapse', function() {
        $(this).parents('tr').prev().find('td:first-child a').empty()
            .html("&#9658;").attr('title','expand');
    }).
    on('shown', 'div.collapse', function() {
        trackState();
    }).
    on('hidden', 'div.collapse', function() {
        trackState();
    });

function trackState () {
    var $leaves = $('#activityList div.collapse'),
        state = [];
    $.each($leaves, function (i, leaf) {
        if ($(leaf).hasClass('in')) {
            state.push($(leaf).attr('id'));
        }
    });
    console.log('state stored = ' + state);
    amplify.store.sessionStorage('output-accordion-state',state);
}

function readState () {
    var $leaves = $('#activityList div.collapse'),
        state = amplify.store.sessionStorage('output-accordion-state'),
        id;
    console.log('state retrieved = ' + state);
    $.each($leaves, function (i, leaf) {
        id = $(leaf).attr('id');
        if (($.inArray(id, state) > -1)) {
            $(leaf).collapse('show');
        }
    });
}

var image = function(props) {

    var imageObj = {
        id:props.id,
        name:props.name,
        size:props.size,
        url: props.url,
        thumbnail_url: props.thumbnail_url,
        viewImage : function() {
            window['showImageInViewer'](this.id, this.url, this.name);
        }
    };
    return imageObj;
};

ko.bindingHandlers.photoPoint = {
    init: function(element, valueAccessor) {

    }
}


ko.bindingHandlers.photoPointUpload = {
    init: function(element, valueAccessor, allBindings, viewModel, bindingContext) {

        var defaultConfig = {
            maxWidth: 300,
            minWidth:150,
            minHeight:150,
            maxHeight: 300,
            previewSelector: '.preview'
        };
        var size = ko.observable();
        var progress = ko.observable();
        var error = ko.observable();
        var complete = ko.observable(true);

        var uploadProperties = {

            size: size,
            progress: progress,
            error:error,
            complete:complete

        };
        var innerContext = bindingContext.createChildContext(bindingContext);
        ko.utils.extend(innerContext, uploadProperties);

        var config = valueAccessor();
        config = $.extend({}, config, defaultConfig);

        var target = config.target; // Expected to be a ko.observableArray
        $(element).fileupload({
            url:config.url,
            autoUpload:true,
            forceIframeTransport: true
        }).on('fileuploadadd', function(e, data) {
            complete(false);
            progress(1);
        }).on('fileuploadprocessalways', function(e, data) {
            if (data.files[0].preview) {
                if (config.previewSelector !== undefined) {
                    var previewElem = $(element).parent().find(config.previewSelector);
                    previewElem.append(data.files[0].preview);
                }
            }
        }).on('fileuploadprogressall', function(e, data) {
            progress(Math.floor(data.loaded / data.total * 100));
            size(data.total);
        }).on('fileuploaddone', function(e, data) {

//            var resultText = $('pre', data.result).text();
//            var result = $.parseJSON(resultText);


            var result = data.result;
            if (!result) {
                result = {};
                error('No response from server');
            }

            if (result.files[0]) {
                target.push(result.files[0]);
                complete(true);
            }
            else {
                error(result.error);
            }

        }).on('fileuploadfail', function(e, data) {
            error(data.errorThrown);
        });

        ko.applyBindingsToDescendants(innerContext, element);

        return { controlsDescendantBindings: true };
    }
};

ko.bindingHandlers.imageUpload = {
    init: function(element, valueAccessor) {

        var config = {autoUpload:true};
        var observable;
        var params = valueAccessor();
        if (ko.isObservable(params)) {
            observable = params;
        }
        else {
            observable = params.target;
            $.extend(config, params.config);
        }

        var addCallbacks = function() {
            // The upload URL is specified using the data-url attribute to allow it to be easily pulled from the
            // application configuration.
            $(element).fileupload('option', 'completed', function(e, data) {
                if (data.result && data.result.files) {
                    $.each(data.result.files, function(index, obj) {
                        if (observable.hasOwnProperty('push')) {
                            observable.push(image(obj));
                        }
                        else {
                            observable(image(obj))
                        }
                    });
                }
            });
            $(element).fileupload('option', 'destroyed', function(e, data) {
                var filename = $(e.currentTarget).attr('data-filename');

                if (observable.hasOwnProperty('remove')) {
                    var images = observable();

                    // We rely on the template rendering the filename into the delete button so we can identify which
                    // object has been deleted.
                    $.each(images, function(index, obj) {
                        if (obj.name === filename) {
                            observable.remove(obj);
                            return false;
                        }
                    });
                }
                else {
                    observable({})
                }
            });

        };

        $(element).fileupload(config);

        var value = ko.utils.unwrapObservable(observable);
        var isArray = value.hasOwnProperty('length');

        if ((isArray && value.length > 0) || (!isArray && value['name'] !== undefined)) {
            // Render the existing model items - we are currently storing all of the metadata needed by the
            // jquery-file-upload plugin in the model but we should probably only store the core data and decorate
            // it in the templating code (e.g. the delete URL and HTTP method).
            $(element).fileupload('option', 'completed', function(e, data) {
                addCallbacks();
            });
            var data = {result:{}};
            if (isArray)  {
                data.result.files = value
            }
            else {
                data.result.files = [value];
            }
            var doneFunction = $(element).fileupload('option', 'done');
            var e = {isDefaultPrevented:function(){return false;}};

            doneFunction.call(element, e, data);
        }
        else {
            addCallbacks();
        }

        // Enable iframe cross-domain access via redirect option:
        $(element).fileupload(
            'option',
            'redirect',
            window.location.href.replace(
                /\/[^\/]*$/,
                '/cors/result.html?%s'
            )
        );

    }

};

ko.bindingHandlers.editDocument = {
    init:function(element, valueAccessor) {
        if (ko.isObservable(valueAccessor())) {
            var document = ko.utils.unwrapObservable(valueAccessor());
            if (typeof document.status == 'function') {
                document.status.subscribe(function(status) {
                    if (status == 'deleted') {
                        valueAccessor()(null);
                    }
                });
            }
        }
        var options = {
            name:'documentEditTemplate',
            data:valueAccessor()
        };
        return ko.bindingHandlers['template'].init(element, function() {return options;});
    },
    update:function(element, valueAccessor, allBindings, viewModel, bindingContext) {
        var options = {
            name:'documentEditTemplate',
            data:valueAccessor()
        };
        ko.bindingHandlers['template'].update(element, function() {return options;}, allBindings, viewModel, bindingContext);
    }
};

OutputModel = function(output, context, config) {
    var self = this;
    if (!output) {
        output = {};
    }
    var activityId = output.activityId || config.activityId;
    self.name = output.name;
    self.outputId = orBlank(output.outputId);

    self.data = {};
    self.transients = {};
    var notCompleted = output.outputNotCompleted;

    if (notCompleted === undefined) {
        notCompleted = config.collapsedByDefault;
    }

    self.outputNotCompleted = ko.observable(notCompleted);
    self.transients.optional = config.optional || false;
    self.transients.questionText = config.optionalQuestionText || 'Not applicable';
    self.transients.dummy = ko.observable();

    self.downloadDataTemplate = function(listName) {
        var data = ko.mapping.toJS(self.data[listName](), {ignore:['transients']});
        var params = {
            listName:listName,
            type:self.name,
            data:JSON.stringify(data)
        };
        var url = config.downloadTemplateUrl;
        $.fileDownload(url, {
            httpMethod:'POST',
            data:params
        });

    };
    // this will be called when generating a savable model to remove transient properties
    self.removeBeforeSave = function (jsData) {
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


    self.prepop = function() {
        // How does one do that?

        // call url - answer!



        // call url or map context.

        var conf = config.model['pre-populate'];
        if (!conf) {
            return;
        }
        var data = {};
        _.each(conf, function(item) {
            var prepopData = context;
            var mapping = item.mapping;


            if (!prepopData) {
                return;
            }
            _.extend(data, self.map(mapping, prepopData));
        });

        return data;
    };


    self.map = function(mappingList, data) {
        var result = {};

        _.each(mappingList, function(mapping) {

            // Presence of a nested mapping element indicates a list.
            if (_.has(mapping, 'mapping')) {
                result[mapping.target] = [];
                var selectedData = getNestedValue(data, mapping['source-path']);
                _.each(selectedData, function(d) {
                    var nestedResult = self.map(mapping.mapping, d);
                    if (nestedResult) {
                        result[mapping.target].push(nestedResult);
                    }

                });
            }
            else {
                result[mapping.target] = getNestedValue(data, mapping['source-path']);
            }
        });

        return result;
    };

    self.attachDocument = function(target) {
        var url = config.documentUpdateUrl || fcConfig.documentUpdateUrl;
        showDocumentAttachInModal(url, new DocumentViewModel({role:'information', stage:config.stage},{activityId:activityId, projectId:config.projectId}), '#attachDocument')
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