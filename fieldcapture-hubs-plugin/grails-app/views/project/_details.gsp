<div class="row-fluid">
    <div class="span6">

        <div class="clearfix" data-bind="visible:organisationId()||organisationName()">
            <h4>
                Organisation:
                <a data-bind="visible:organisationId(),text:organisationName,attr:{href:fcConfig.organisationLinkBaseUrl + organisationId()}"></a>
                <span data-bind="visible:!organisationId(),text:organisationName"></span>
            </h4>
        </div>
        <h4 class="header"><g:message code="project.details.tell"/></h4>



        <div class="clearfix control-group">
            <label class="control-label span3" for="name"><g:message code="g.project.name"/>:</label>

            <div class="controls span9">
                <g:textField class="span12" name="name" data-bind="value:name"
                             data-validation-engine="validate[required]"/>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="description"><g:message code="g.project.description"/>:</label>

            <div class="controls span9">
                <g:textArea class="span12" name="description" data-bind="value:description"
                            data-validation-engine="validate[required]" rows="3"/>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="manager"><g:message code="project.details.manager"/>:</label>

            <div class="controls span9">
                <g:textField class="span12" type="email" data-bind="value:manager" name="manager"/>
            </div>
        </div>
    </div>

    <div data-bind="visible:!isCitizenScience()" class="span6">
        <h4 class="header">&nbsp;</h4>

        <div class="control-group">
            <label class="control-label span3" for="externalId"><g:message code="g.project.externalId"/>:</label>

            <div class="controls span9">
                <g:textField class="span12" name="externalId" data-bind="value:externalId"/>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="grantId"><g:message code="g.project.grantId"/>:</label>

            <div class="controls span9">
                <g:textField class="span12" name="grantId" data-bind="value:grantId"/>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="funding"><g:message code="g.project.funding"/>:</label>

            <div class="controls span9">
                <g:textField class="span12" name="funding" data-bind="value:funding"
                             data-validation-engine="validate[custom[number]]"/>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="program"><g:message code="g.program.name"/>:</label>

            <div class="controls span9">
                <select class="span12" id="program"
                        data-bind="value:associatedProgram,options:transients.programs,optionsCaption: 'Choose...'"
                        data-validation-engine="validate[required]"></select>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="subProgram"><g:message code="g.subprogram.name"/>:</label>

            <div class="controls span9">
                <select class="span12" id="subProgram"
                        data-bind="value:associatedSubProgram,options:transients.subprogramsToDisplay,optionsCaption: 'Choose...'"></select>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3"
                   for="orgGrantee"><g:message code="project.details.orgGrantee"/>:</label>

            <div class="controls span9">
                <select class="span12" id="orgGrantee"
                        data-bind="options:transients.organisations, optionsText:'name', optionsValue:'uid', value:orgIdGrantee, optionsCaption: 'Choose...'"></select>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3"
                   for="orgSponsor"><g:message code="project.details.orgSponsor"/>:</label>

            <div class="controls span9">
                <select class="span12" id="orgSponsor"
                        data-bind="options:transients.organisations, optionsText:'name', optionsValue:'uid', value:orgIdSponsor, optionsCaption: 'Choose...'"></select>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3"
                   for="orgSvcProvider"><g:message code="project.details.orgSvcProvider"/>:</label>

            <div class="controls span9">
                <select class="span12" id="orgSvcProvider"
                        data-bind="options:transients.organisations, optionsText:'name', optionsValue:'uid', value:orgIdSvcProvider, optionsCaption: 'Choose...'"></select>
            </div>
        </div>
    </div>

    <div data-bind="visible:isCitizenScience()" class="span6">
        <h4 class="header span12"><g:message code="project.details.involved"/></h4>

        <div class="clearfix control-group">
            <label class="control-label span3" for="getInvolved"><g:message code="project.details.involved"/></label>

            <div class="controls span9">
                <g:textArea class="span12" name="getInvolved" data-bind="value:getInvolved"
                            rows="2"/>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3"
                   for="scienceType"><g:message code="project.details.scienceType"/>:</label>

            <div class="controls span9">
                <g:textField class="span12" name="scienceType" data-bind="value:scienceType"/>
            </div>
        </div>
    </div>
    <div class="row-fluid">
        <div class="span6">
            <h4 class="header"><g:message code="project.details.find"/>:</h4>

            <div class="control-group">
                <label class="control-label span3" for="urlWeb"><g:message code="g.website"/>:</label>

                <div class="controls span9">
                    <g:textField class="span12" tye="url" name="urlWeb" data-bind="value:urlWeb"/>
                </div>
            </div>

            <div class="clearfix control-group">
                <label class="control-label span3" for="urlAndroid"><g:message code="g.android"/>:</label>

                <div class="controls span9">
                    <g:textField class="span12" tye="url" name="urlAndroid" data-bind="value:urlAndroid"/>
                </div>
            </div>

            <div class="clearfix control-group">
                <label class="control-label span3" for="urlITunes"><g:message code="g.iTunes"/>:</label>

                <div class="controls span9">
                    <g:textField class="span12" tye="url" name="urlITunes" data-bind="value:urlITunes"/>
                </div>
            </div>
        </div>

        <div class="span6">
            <h4 class="header">&nbsp;</h4>

            <div class="control-group">
                <label class="control-label span3" for="keywords"><g:message code="g.keywords"/>:</label>

                <div class="controls span9">
                    <g:textArea class="span12" name="keywords" data-bind="value:keywords" rows="2"/>
                </div>
            </div>
        </div>
    </div>
</div>


<div class="row-fluid">
    <div class="span6">
        <div class="clearfix control-group">
            <label class="control-label span3" for="plannedStartDate"><g:message code="g.project.plannedStartDate"/>:
            <fc:iconHelp><g:message code="g.project.plannedStartDate.help"/></fc:iconHelp>
            </label>

            <div class="controls span9">
                <fc:datePicker targetField="plannedStartDate.date" name="plannedStartDate"
                               id="plannedStartDate" data-validation-engine="validate[required]"/>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="plannedEndDate"><g:message code="g.project.plannedEndDate"/>:
            <fc:iconHelp><g:message code="g.project.plannedEndDate.help"/></fc:iconHelp>
            </label>

            <div class="controls span9">
                <fc:datePicker targetField="plannedEndDate.date" name="plannedEndDate"
                               id="plannedEndDate" data-validation-engine="validate[future[plannedStartDate]]"/>
            </div>
        </div>
    </div>

</div>

<hr class="clearfix"/>

<div class="row-fluid">
    <div class="span6 control-group"">
        <h4 class="header"><g:message code="project.details.image"/></h4>

        <div class="clearfix control-group">
            <img data-bind="visible:mainImageUrl(),attr:{src:mainImageUrl}">
            <span class="btn fileinput-button pull-right"
                  data-url="${createLink(controller: 'image', action: 'upload')}"
                  data-role="mainImage"
                  data-owner-type="projectId"
                  data-owner-id="${project?.projectId}"
                  data-bind="stagedImageUpload:documents, visible:!mainImageUrl()"><i class="icon-plus"></i> <input
                    id="mainImage" type="file" name="files"><span>Attach Image</span></span>

            <button class="btn main-image-button" data-bind="click:removeMainImage,  visible:mainImageUrl()"><i class="icon-minus"></i> Remove Image</button>
        </div>
    </div>

    <div class="span6">
        <h4 class="header"><g:message code="project.details.access"/></h4>

        <div class="clearfix control-group">
            <label class="control-label span3" for="projectPrivacy"><g:message
                    code="project.details.projectPrivacy"/>:</label>

            <div class="controls span9">
                <select class="span12" id="projectPrivacy"
                        data-bind="value:projectPrivacy,options:['Open','Closed'],optionsCaption: 'Choose...'"
                        data-validation-engine="validate[required]"></select>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="dataSharing"><g:message
                    code="project.details.dataSharing"/>:</label>

            <div class="controls span9">
                <select class="span12" id="dataSharing"
                        data-bind="value:dataSharing,options:['Enabled','Disabled'],optionsCaption: 'Choose...'"
                        data-validation-engine="validate[required]"></select>
            </div>
        </div>

        <div class="clearfix control-group">
            <label class="control-label span3" for="dataSharingLicense"><g:message
                    code="project.details.dataSharingLicense"/>:</label>

            <div class="controls span9">
                <select class="span12" id="dataSharingLicense"
                        data-bind="value:dataSharingLicense,options:transients.dataSharingLicenses,optionsText:'name',optionsValue:'lic',optionsCaption: 'Choose if applicable...'"></select>
            </div>
        </div>
    </div>
</div>


<hr class="clearfix"/>
<h4 class="header"><g:message code="project.details.site"/></h4>
<g:render template="/site/map" model="${pageScope.variables}"/>
<hr class="clearfix"/>


<r:script>
function initViewModel() {

    function ViewModel (data, activityTypes, organisations) {
        var self = this;
        $.extend(self, new ProjectViewModel(data, true, organisations));
        self.actualEndDate = ko.observable(data.actualEndDate).extend({simpleDate: false});
        self.actualStartDate = ko.observable(data.actualStartDate).extend({simpleDate: false});
        self.orgIdGrantee = ko.observable(data.orgIdGrantee);
        self.orgIdSponsor = ko.observable(data.orgIdSponsor);
        self.orgIdSvcProvider = ko.observable(data.orgIdSvcProvider);
        self.selectedActivities = ko.observableArray();
        self.transients.activitySource = ko.observable('program');
        self.transients.activityTypes = activityTypes;
        self.transients.subprogramsToDisplay = ko.computed(function () {
            return self.transients.subprograms[self.associatedProgram()];
        });

        self.save = function () {
            if ($('#validation-container').validationEngine('validate')) {
                if (self.transients.activitySource() === 'program') {
                    self.selectedActivities([]);
                }

                var jsData = ko.mapping.toJS(self, {ignore:['mainImageUrl', 'transients', 'dataSharing']});
                jsData.urlAndroid = fixUrl(jsData.urlAndroid);
                jsData.urlITunes  = fixUrl(jsData.urlITunes);
                jsData.urlWeb = fixUrl(jsData.urlWeb);
                jsData.isDataSharing = self.dataSharing() === 'Enabled';
                if (!jsData.dataSharingLicense) jsData.dataSharingLicense = 'other';
                var json = JSON.stringify(jsData);
                var id = "${project?.projectId ? '/' + project.projectId : ''}";
                $.ajax({
                    url: "${createLink(action: 'ajaxUpdate')}" + id,
                    type: 'POST',
                    data: json,
                    contentType: 'application/json',
                    success: function (data) {
                        if (data.error) {
                            alert(data.detail + ' \n' + data.error);
                        } else {
                            var projectId = "${project?.projectId}" || data.projectId;
                            document.location.href = "${createLink(action: 'index')}/" + projectId;

                        }
                    },
                    error: function (data) {
                        alert('An unhandled error occurred: ' + data.status);
                    }
                });
            }
        }
    }

    function fixUrl(url) {
        return typeof url == 'string' && url.indexOf("://") < 0? ("http://" + url): url;
    }

    var programsModel = <fc:modelAsJavascript model="${programs}"/>;
    var organisations = <fc:modelAsJavascript model="${organisations?:[]}"/>;
    var activityTypes = JSON.parse('${(activityTypes as grails.converters.JSON).toString().encodeAsJavaScript()}');
    var viewModel =  new ViewModel(${project ?: [:]}, activityTypes, organisations);
    viewModel.loadPrograms(programsModel);
    return viewModel;
}
</r:script>
