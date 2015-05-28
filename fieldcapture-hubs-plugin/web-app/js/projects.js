/*
    Utilities for managing project representations.
 */

/**
 * A chance to make any on-the-fly changes to projects as they are opened.
 * @param project
 * @param callback optional callback for the results of any asynch saves
 * @returns updated project object
 */
function checkAndUpdateProject (project, callback, programs) {
    var propertiesToSave = {},
        isEmpty=function(x,p){for(p in x)return!1;return!0};
    // add any checks here - return true if the project representation needs to be saved
    var program = null;
    if (programs && project.associatedProgram) {
        var matchingProgram = $.grep(programs.programs, function(program, index) {
            return program.name == project.associatedProgram;
        });
        program = matchingProgram[0];
    }
    propertiesToSave = $.extend(propertiesToSave, createTimelineIfMissing(project, program));
    // check for saves
    if (!isEmpty(propertiesToSave) && fcConfig.projectUpdateUrl !== undefined) {
        $.ajax({
            url: fcConfig.projectUpdateUrl,
            type: 'POST',
            data: JSON.stringify(propertiesToSave),
            contentType: 'application/json',
            success: function (data) {
                if (callback) {
                    if (data.error) {
                        callback.call(this, 'error', data.detail + ' \n' + data.error);
                    } else {
                        callback.call(this, 'success');
                    }
                }
            },
            error: function (data) {
                if (callback) {
                    callback.call(this, 'error', data.status);
                }
            }
        });
    }
    return project;
}

/**
 * Injects a newly created timeline if none exists.
 * Clears (but can't delete) any currentStage property. This prop is
 * deprecated because current stage is calculated from the timeline and
 * the current date.
 * @param project
 * @returns updated properties
 */
function createTimelineIfMissing (project, program) {
    if (project.timeline === undefined) {
        var props = {};
        if (project.currentStage !== undefined) {
            props.currentStage = '';
        }
        if (program) {
            addTimelineBasedOnStartDate(project, program.reportingPeriod, program.reportingPeriodAlignedToCalendar || false);
        }
        else {
            addTimelineBasedOnStartDate(project);
        }
        props.timeline = project.timeline;
        return props;
    }
    return {};
}

/**
 * Creates a default timeline based on project start date.
 * Assumes 6 monthly stages with the first containing the project's
 * planned start date.
 * @param project
 */
function addTimelineBasedOnStartDate (project, reportingPeriod, alignToCalendar) {

    if (!reportingPeriod) {
        reportingPeriod = 6;
    }
    if (alignToCalendar == undefined) {
        alignToCalendar = true;
    }

    // planned start date should be an ISO8601 UTC string
    if (project.plannedStartDate === undefined || project.plannedStartDate === '') {
        // make one up so we can proceed
        project.plannedStartDate = new Date(Date.now()).toISOStringNoMillis();
    }
    if (project.plannedEndDate === undefined || project.plannedEndDate === '') {
        // make one up so we can proceed
        var endDate = new Date(Date.now());
        endDate = endDate.setUTCFullYear(endDate.getUTCFullYear()+5);
        project.plannedEndDate = endDate.toISOStringNoMillis();
    }

    var date = Date.fromISO(project.plannedStartDate),
        endDate = Date.fromISO(project.plannedEndDate),
        i = 0;

    if (alignToCalendar) {
        var month = date.getMonth();
        var numPeriods = Math.floor(month/reportingPeriod);
        var monthOfStartDate = numPeriods*reportingPeriod;
        var dayOfStartDate = 1;

        date = new Date(date.getFullYear(), monthOfStartDate, dayOfStartDate);
    }
    project.timeline = [];

    var duration = moment.duration({'months':reportingPeriod});

    var periodStart = moment(date);
    while (periodStart.isBefore(endDate)) {

        var periodEnd = moment(periodStart).add(duration);
        var period = {
            fromDate: periodStart.toISOString(),
            toDate:periodEnd.toISOString()
        };
        period.name = 'Stage ' + (i + 1);
        project.timeline.push(period);

        // add 6 months to date
        periodStart = periodEnd;
        i++;
    }
}

/**
 * Returns the from and to dates of the half year that the specified
 * date falls in.
 * @param date
 * @returns {{fromDate: string, toDate: string}}
 */
function getSixMonthPeriodContainingDate (date) {
    var year = date.getUTCFullYear(),
        midYear = new Date(Date.UTC(year, 6, 0));
    if (date.getTime() < midYear.getTime()) {
        return {
            fromDate: year + "-01-01T00:00:00Z",
            toDate: year + "-07-01T00:00:00Z"
        };
    } else {
        return {
            fromDate: year + "-07-01T00:00:00Z",
            toDate: (year + 1) + "-01-01T00:00:00Z"
        };
    }
}

/**
 * Returns the stage within the timeline that contains the specified date.
 * @param timeline
 * @param UTCDateStr date must be an ISO8601 string
 * @returns {string}
 */
function findStageFromDate (timeline, UTCDateStr) {
    var stage = 'unknown';
    // try a simple lexical comparison
    $.each(timeline, function (i, period) {
        if (UTCDateStr > period.fromDate && UTCDateStr <= period.toDate) {
            stage = period.name;
        }
    });
    return stage;
}

/**
 * Returns stage report status.
 * @param project
 * @param stage
 * @returns {boolean}
 */
function isStageReportable (project, stage) {

    //is current stage a last stage?
    if(project.timeline && project.timeline.length > 0 &&
        project.timeline[project.timeline.length-1].name == stage.name){
       return project.plannedEndDate < new Date().toISOStringNoMillis();
    }
    else{
        return stage.toDate < new Date().toISOStringNoMillis();
    }
}
/**
 * Returns the activities associated with the stage.
 * @param activities
 * @param timeline
 * @param stage stage name
 * @returns {[]}
 */
function findActivitiesForStage (activities, timeline, stage) {
	var stageFromDate = '';
	var stageToDate = '';

	$.each(timeline, function (i, period) {
		if(period.name == stage){
			stageFromDate = period.fromDate;
			stageToDate = period.toDate;
		}
	});

    stageActivities = $.map(activities, function(act, i) {
    	var endDate = act.endDate ? act.endDate : act.plannedEndDate;
    	var startDate = act.startDate ? act.startDate : act.plannedStartDate;
        if(startDate >= stageFromDate && endDate <= stageToDate){
        	return act;
        }
    });
    return stageActivities;
}

/**
 * Is it a current or past stage
 * @param timeline
 * @param stage current stage name
 * @param period stage period
 * @returns true if past stage.
 */
function isPastStage(timeline, currentStage, period) {

	var stageFromDate = '';
	var stageToDate = '';
	$.each(timeline, function (i, period) {
		if(period.name == currentStage){
			stageFromDate = period.fromDate;
			stageToDate = period.toDate;
		}
	});
	return period.toDate <= stageToDate;
}

function getBugetHeaders(timeline) {
	var headers = [];
    var startYr = moment(timeline[0].fromDate).format('YYYY');
    var endYr = moment(timeline[timeline.length-1].toDate).format('YYYY');;
    var startMonth = moment(timeline[0].fromDate).format('M');
    var endMonth = moment(timeline[timeline.length-1].toDate).format('M');

    //Is startYr is between jan to june?
    if(startMonth >= 1 &&  startMonth <= 6 ){
        startYr--;
    }

    //Is the end year is between july to dec?
    if(endMonth >= 7 &&  endMonth <= 12 ){
        endYr++;
    }

    var count = endYr - startYr;
    for (i = 0; i < count; i++){
        headers.push(startYr + '/' + ++startYr);
    }
    return headers;

}

function isValid(p, a) {
	 a = a.split(".");
	 for (i in a) {
		var key = a[i];
		if (p[key] == null || p[key] == undefined){
			return '';
		}
		p = p[key];
	 }
	 return p;
}



function ProjectViewModel(project, isUserEditor, organisations) {
    var self = $.extend(this, new Documents());

    var organisationsMap = {}, organisationsRMap = {};
    $.map(organisations, function(org) {
        organisationsMap[org.organisationId] = org;
        organisationsRMap[org.name] = org.organisationId;
    });

    self.name = ko.observable(project.name);
    self.description = ko.observable(project.description).extend({markdown:true});;
    self.externalId = ko.observable(project.externalId);
    self.grantId = ko.observable(project.grantId);
    self.manager = ko.observable(project.manager);
    self.plannedStartDate = ko.observable(project.plannedStartDate).extend({simpleDate: false});
    self.plannedEndDate = ko.observable(project.plannedEndDate).extend({simpleDate: false});
    self.funding = ko.observable(project.funding).extend({currency:{}});

    self.regenerateProjectTimeline = ko.observable(false);
    self.projectDatesChanged = ko.computed(function() {
        return project.plannedStartDate != self.plannedStartDate() ||
            project.plannedEndDate != self.plannedEndDate();
    });
    var projectDefault = "active";
    if(project.status){
        projectDefault = project.status;
    }
    self.status = ko.observable(projectDefault.toLowerCase());
    self.projectStatus = [{id: 'active', name:'Active'},{id:'completed',name:'Completed'},{id:'deleted', name:'Deleted'}];

    self.organisationId = ko.observable(project.organisationId);
    self.collectoryInstitutionId = ko.computed(function() {
        var org = self.organisationId() && organisationsMap[self.organisationId()];
        return org? org.collectoryInstitutionId: "";
    });
    self.organisationName = ko.computed(function() {
        var org = self.organisationId() && organisationsMap[self.organisationId()];
        return org? org.name: project.organisationName;
    });
    self.orgIdGrantee = ko.observable(project.orgIdGrantee);
    self.orgIdSponsor = ko.observable(project.orgIdSponsor);
    self.orgIdSvcProvider = ko.observable(project.orgIdSvcProvider);

    self.serviceProviderName = ko.observable(project.serviceProviderName);
    self.associatedProgram = ko.observable(); // don't initialise yet - we want the change to trigger dependents
    self.associatedSubProgram = ko.observable(project.associatedSubProgram);
    self.newsAndEvents = ko.observable(project.newsAndEvents).extend({markdown:true});
    self.projectStories = ko.observable(project.projectStories).extend({markdown:true});

    self.dataSharing = ko.observable(project.isDataSharing? "Enabled": "Disabled");
    self.dataSharingLicense = ko.observable(project.dataSharingLicense);
    self.getInvolved = ko.observable(project.getInvolved);
    self.isCitizenScience = ko.observable(project.isCitizenScience);
    self.keywords = ko.observable(project.keywords);
    self.projectPrivacy = ko.observable(project.projectPrivacy);
    self.projectSiteId = project.projectSiteId;
    self.projectType = ko.observable(project.projectType || "works");
    self.scienceType = ko.observable(project.scienceType);
    self.urlAndroid = ko.observable(project.urlAndroid).extend({url:true});
    self.urlITunes = ko.observable(project.urlITunes).extend({url:true});
    self.urlWeb = ko.observable(project.urlWeb).extend({url:true});
    self.isExternal = ko.observable(project.isExternal);
    self.contractStartDate = ko.observable(project.contractStartDate).extend({simpleDate: false});
    self.contractEndDate = ko.observable(project.contractEndDate).extend({simpleDate: false});

    self.transients = {};

    var calculateDuration = function(startDate, endDate) {
        if (!startDate || !endDate) {
            return '';
        }
        var start = moment(startDate);
        var end = moment(endDate);
        var durationInDays = end.diff(start, 'days');

        return Math.ceil(durationInDays/7);
    };
    var calculateEndDate = function(startDate, duration) {
        var start =  moment(startDate);
        var end = start.add(duration*7, 'days');
        return end.toDate().toISOStringNoMillis();
    };

    var contractDatesFixed = function() {
        var programs = self.transients.programs;
        for (var i=0; i<programs.length; i++) {
            if (programs[i].name === self.associatedProgram()) {
                return programs[i].projectDatesContracted;
            }
        }
        return true;
    };

    var updatingDurations = false; // Flag to prevent endless loops during change of end date / duration.

    self.transients.plannedDuration = ko.observable(calculateDuration(self.plannedStartDate(), self.plannedEndDate()));
    self.transients.plannedDuration.subscribe(function(newDuration) {
        if (updatingDurations) {
            return;
        }
        try {
            updatingDurations = true;
            self.plannedEndDate(calculateEndDate(self.plannedStartDate(), newDuration));
        }
        finally {
            updatingDurations = false;
        }
    });

    self.plannedEndDate.subscribe(function(newEndDate) {
        if (updatingDurations) {
            return;
        }
        try {
            updatingDurations = true;
            self.transients.plannedDuration(calculateDuration(self.plannedStartDate(), newEndDate));
        }
        finally {
            updatingDurations = false;
        }
    });

    self.plannedStartDate.subscribe(function(newStartDate) {
        if (updatingDurations) {
            return;
        }
        if (contractDatesFixed()) {
            if (!self.plannedEndDate()) {
                return;
            }
            try {
                updatingDurations = true;
                self.transients.plannedDuration(calculateDuration(newStartDate, self.plannedEndDate()));
            }
            finally {
                updatingDurations = false;
            }
        }
        else {
            if (!self.transients.plannedDuration()) {
                return;
            }
            try {
                updatingDurations = true;
                self.plannedEndDate(calculateEndDate(newStartDate, self.transients.plannedDuration()));
            }
            finally {
                updatingDurations = false;
            }
        }
    });

    self.transients.contractDuration = ko.observable(calculateDuration(self.contractStartDate(), self.contractEndDate()));
    self.transients.contractDuration.subscribe(function(newDuration) {
        if (updatingDurations) {
            return;
        }
        if (!self.contractStartDate()) {
            return;
        }
        try {
            updatingDurations = true;
            self.contractEndDate(calculateEndDate(self.contractStartDate(), newDuration));
        }
        finally {
            updatingDurations = false;
        }
    });


    self.contractEndDate.subscribe(function(newEndDate) {
        if (updatingDurations) {
            return;
        }
        if (!self.contractStartDate()) {
            return;
        }
        try {
            updatingDurations = true;
            self.transients.contractDuration(calculateDuration(self.contractStartDate(), newEndDate));
        }
        finally {
            updatingDurations = false;
        }
    });

    self.contractStartDate.subscribe(function(newStartDate) {
        if (updatingDurations) {
            return;
        }
        if (contractDatesFixed()) {
            if (!self.contractEndDate()) {
                return;
            }
            try {
                updatingDurations = true;
                self.transients.contractDuration(calculateDuration(newStartDate, self.contractEndDate()));
            }
            finally {
                updatingDurations = false;
            }
        }
        else {
            if (!self.transients.contractDuration()) {
                return;
            }
            try {
                updatingDurations = true;
                self.contractEndDate(calculateEndDate(newStartDate, self.transients.contractDuration()));
            }
            finally {
                updatingDurations = false;
            }
        }
    });

    self.transients.programs = [];
    self.transients.subprograms = {};
    self.transients.subprogramsToDisplay = ko.computed(function () {
        return self.transients.subprograms[self.associatedProgram()];
    });
    self.transients.dataSharingLicenses = [
            {lic:'CC BY', name:'Creative Commons Attribution'},
            {lic:'CC BY-NC', name:'Creative Commons Attribution-NonCommercial'},
            {lic:'CC BY-SA', name:'Creative Commons Attribution-ShareAlike'},
            {lic:'CC BY-NC-SA', name:'Creative Commons Attribution-NonCommercial-ShareAlike'}
        ];
    self.transients.organisations = organisations;

    self.loadPrograms = function (programsModel) {
        $.each(programsModel.programs, function (i, program) {
            if (program.readOnly && self.associatedProgram() != program.name) {
                return;
            }
            self.transients.programs.push(program.name);
            self.transients.subprograms[program.name] = $.map(program.subprograms,function (obj, i){return obj.name});
        });
        self.associatedProgram(project.associatedProgram); // to trigger the computation of sub-programs
    };

    self.toJS = function() {
        var toIgnore = self.ignore; // document properties to ignore.
        toIgnore.concat(['transients', 'projectDatesChanged', 'collectoryInstitutionId', 'ignore', 'projectStatus']);
        return ko.mapping.toJS(self, {ignore:toIgnore});
    }

    self.modelAsJSON = function() {
        return JSON.stringify(self.toJS());
    };

    // settings
    self.saveSettings = function () {
        if ($('#settings-validation').validationEngine('validate')) {

            // only collect those fields that can be edited in the settings pane
            var jsData = {
                name: self.name(),
                description: self.description(),
                externalId: self.externalId(),
                grantId: self.grantId(),
                manager: self.manager(),
                plannedStartDate: self.plannedStartDate(),
                plannedEndDate: self.plannedEndDate(),
                organisationId: self.organisationId(),
                organisationName: self.organisationName(),
                serviceProviderName: self.serviceProviderName(),
                associatedProgram: self.associatedProgram(),
                associatedSubProgram: self.associatedSubProgram(),
                funding: new Number(self.funding())
            };
            if (self.regenerateProjectTimeline()) {
                var dates = {
                    plannedStartDate: self.plannedStartDate(),
                    plannedEndDate: self.plannedEndDate()
                };
                addTimelineBasedOnStartDate(dates);
                jsData.timeline = dates.timeline;
            }
            // this call to stringify will make sure that undefined values are propagated to
            // the update call - otherwise it is impossible to erase fields
            var json = JSON.stringify(jsData, function (key, value) {
                return value === undefined ? "" : value;
            });
            var id = "${project?.projectId}";
            $.ajax({
                url: "${createLink(controller:'project', action: 'ajaxUpdate', id: project.projectId)}",
                type: 'POST',
                data: json,
                contentType: 'application/json',
                success: function (data) {
                    if (data.error) {
                        showAlert("Failed to save settings: " + data.detail + ' \n' + data.error,
                            "alert-error","save-result-placeholder");
                    } else {
                        showAlert("Project settings saved","alert-success","save-result-placeholder");
                    }
                },
                error: function (data) {
                    var status = data.status;
                    alert('An unhandled error occurred: ' + data.status);
                }
            });
        }
    };

    // documents
    var maxStages = project.timeline ? project.timeline.length : 0 ;
    self.addDocument = function(doc) {
        // check permissions
        if ((isUserEditor && doc.role !== 'approval') ||  doc.public) {
            doc.maxStages = maxStages;
            self.documents.push(new DocumentViewModel(doc));
        }
    };
    self.attachDocument = function() {
        showDocumentAttachInModal(fcConfig.documentUpdateUrl, new DocumentViewModel({role:'information', maxStages: maxStages},{key:'projectId', value:project.projectId}), '#attachDocument')
            .done(function(result){
                self.documents.push(new DocumentViewModel(result))}
            );
    };
    self.editDocumentMetadata = function(document) {
        var url = fcConfig.documentUpdateUrl + "/" + document.documentId;
        showDocumentAttachInModal( url, document, '#attachDocument')
            .done(function(result){
                window.location.href = here; // The display doesn't update properly otherwise.
            });
    };
    self.deleteDocument = function(document) {
        var url = fcConfig.documentDeleteUrl+'/'+document.documentId;
        $.post(url, {}, function() {self.documents.remove(document);});

    };

    if (project.documents) {
        $.each(project.documents, function(i, doc) {
            self.addDocument(doc);
        });
    }
};

function getHostName(href) {
    var l = document.createElement("a");
    l.href = href;
    return l.hostname;
}