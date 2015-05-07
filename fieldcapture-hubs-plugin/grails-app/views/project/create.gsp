<%@ page import="net.sf.json.JSON; org.codehaus.groovy.grails.web.json.JSONArray" contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="${hubConfig.skin}"/>
    <title><g:message code="g.new"/> | <g:message code="g.projects"/> | <g:message code="g.fieldCapture"/></title>
    <r:script disposition="head">
    var fcConfig = {
        organisationLinkBaseUrl: "${createLink(controller: 'organisation', action: 'index')}",
        returnTo: "${createLink(controller: 'project', action: 'index', id: project?.projectId)}"
        },
        here = window.location.href;

    </r:script>
    <r:require modules="knockout,jqueryValidationEngine,datepicker,wizard,amplify,drawmap,jQueryFileUpload,projects"/>
</head>

<body>
<div class="container-fluid fuelux">

<ul class="breadcrumb">
    <li><g:link controller="home"><g:message code="g.home"/></g:link> <span class="divider">/</span></li>
    <li class="active"><g:message code="project.create.crumb"/></li>
</ul>

<div class="row-fluid wizard" data-initialize="wizard" id="newProjectWizard">
    <ul class="steps">
        <li data-step="1" class="active"><span class="badge">1</span><g:message code="g.project.type"/><span class="chevron"></span></li>
        <li data-step="2" class="active"><span class="badge">2</span><g:message code="g.project.details"/><span class="chevron"></span></li>
        <li data-step="3" class="active"><span class="badge">3</span><g:message code="g.settings"/><span class="chevron"></span></li>
    </ul>

    <form id="projectDetails" class="form-horizontal">
    <div class="step-content">

        <div class="step-pane active validationEngineContainer" data-step="1">

            <div class="row-fluid">
                <h3><g:message code="project.create.type.heading"/>:</h3>

                <div class="control-group span12">
                    <h4>
                        Organisation:
                        <a data-bind="visible:organisationName()&&organisationId(),text:organisationName,attr:{href:fcConfig.organisationLinkBaseUrl + organisationId()}"></a>
                        <g:if test="${allowAdHocOrgNameOnCreate}">
                            <g:textField class="span8" name="adHocOrgName" data-bind="visible:!organisationId(),value:adHocOrgName" data-validation-engine="validate[required]"/>
                        </g:if>
                    </h4>
                </div>

                <div class="clearfix control-group span12">
                    <g:checkBox name="isCitizenScience" data-bind="checked:isCitizenScience"/><strong><g:message code="project.create.isCitizenScience"/></strong>
                </div>

                <div class="clearfix control-group">
                    <span><label for="works" class="control-label"><input type="radio" name="projectType" data-bind="checked:projectType"
                                                                          value="works" id="works"
                                                                          data-validation-engine="validate[minCheckbox[1]]"> <strong><g:message code="g.project.type.works"/></strong>
                    </label></span><g:message code="project.create.type.works"/>
                    <span><label for="survey" class="control-label"><input type="radio" name="projectType" data-bind="checked:projectType"
                                                                           value="survey" id="survey"
                                                                           data-validation-engine="validate[minCheckbox[1]]"> <strong><g:message code="g.project.type.survey"/></strong>
                    </label></span><g:message code="project.create.type.survey"/>
                </div>

                <p>
                    <strong><g:message code="g.note"/>:&nbsp;</strong><g:message code="project.create.type.note"/>
                </p>
            </div>
        </div>

        <div class="step-pane validationEngineContainer" data-step="2">
            <g:render template="details" model="${pageScope.variables}"/>
        </div>

        <div class="step-pane validationEngineContainer" data-step="3">
            <h3><g:message code="g.project.activities"/></h3><g:message code="project.create.settings.heading"/>
            <label for="program-activities"><input id="program-activities" type="radio" name="activitySource"
                                                   value="program"
                                                   data-bind="checked:transients.activitySource"><g:message code="project.create.settings.program"/>
            </label>
            <label for="project-activities"><input id="project-activities" type="radio" name="activitySource"
                                                   value="project"
                                                   data-bind="checked:transients.activitySource"><g:message code="project.create.settings.project"/>
            </label>

            <div data-bind="visible:transients.activitySource() == 'project'">
                <div><input type="checkbox" id="selectAll"><g:message code="g.selectAll"/></div>
                <div data-bind="foreach:transients.activityTypes" id="activityTypes">
                    <strong><span data-bind="text:name"></span></strong>
                    <ul class="unstyled" data-bind="foreach:list">
                        <li><input type="checkbox" name="activity"
                                   data-bind="checked:$root.selectedActivities,value:name,attr:{id:'activity'+$index()}"
                                   data-validation-engine="validate[minCheckbox[1]]"> <span
                                data-bind="text:name"></span></li>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    </form>

    <div class="form-actions" style="clear:both;">
        <button class="btn btn-default btn-prev"><span class="glyphicon glyphicon-arrow-left"></span><g:message code="g.prev"/></button>
        <button class="btn btn-default btn-next" data-last="Save"><g:message code="g.next"/><span
                class="glyphicon glyphicon-arrow-right"></span></button>
    </div>
</div>

<g:if env="development">
    <hr/>

    <div class="expandable-debug">
        <h3>Debug</h3>

        <div>
            <h4>KO model</h4>
            <pre data-bind="text:ko.toJSON($root,null,2)"></pre>
            <h4>Project</h4>
            <pre>${project.encodeAsHTML()}</pre>
            <label class="control-label" for="currentStage">Current stage of project</label>
            <!-- later this will be a dropdown to pick from activity stages -->
            <g:textField class="" name="currentStage" data-bind="value:currentStage"/>
        </div>
    </div>
</g:if>
</div>
<r:script>

$(function(){

    $('.validationEngineContainer').validationEngine();
    $('.helphover').popover({animation: true, trigger:'hover'});

    $('#selectAll').change(function() {
        var checked = $('#selectAll').prop('checked');
        $('#activityTypes input[type=checkbox]').prop('checked', checked);
    });

    var viewModel = initViewModel();
<g:if test="${organisationId}">
    viewModel.organisationId("${organisationId}");
</g:if>
<g:if test="${citizenScience}">
    $("#isCitizenScience").prop("disabled", true);
    viewModel.isCitizenScience(true);
</g:if>
    ko.applyBindings(viewModel, document.getElementById("projectDetails"));
    $('.wizard').on('finished.fu.wizard', function() {
        viewModel.save();
    }).on('changed.fu.wizard', function(e) {
        var selected = $(this).wizard('selectedItem');
        if (selected.step == 2 && !viewModel.projectSite) {
            viewModel.projectSite = initSiteViewModel();
            viewModel.projectSite.type("projectArea");
            $("#siteType").prop("disabled", 'disabled');
        }
    }).on('actionclicked.fu.wizard', function(e) {
        var selected = $(this).wizard('selectedItem');
        if (!$('.step-pane[data-step='+selected.step+']').validationEngine('validate')) {
            e.preventDefault();
        }
    });
});
</r:script>

</body>
</html>