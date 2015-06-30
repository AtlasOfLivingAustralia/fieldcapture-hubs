<h4>Project surveys</h4>
<p>Each of your surveys can be configured differently depending on your needs. Project level settings are inherited as the default.</p>
<p>Click on tabs to edit settings required. And new surveys to the project as required.</p>

<div id="pActivities" >

    <div class="tab-pane" id="admin-project-activity">
        <div class="row-fluid">
            <div class="span2 large-space-before">

                <!-- ko  foreach: projectActivities -->
                <button  data-bind="click: $root.setCurrent, css:{'btn btn-link' : true} ">
                   <b><span data-bind="text: name"></span> </b> <span data-bind="if: current"> <i class="icon-chevron-right"></i></span>
                </button>
                </br>
                <!-- /ko -->

                </br></br>
                <div class="btn-group btn-group-vertical">
                    <a class="btn btn-xs btn-default" data-bind="click: addProjectActivity"> <i class="icon-plus"></i> Add Survey</a>
                    <!-- ko if: projectActivities().length > 0 -->
                        <a class="btn btn-xs btn-default" data-bind="click: deleteProjectActivity"> <i class="icon-minus-sign"></i> Delete Survey</a>
                    <!-- /ko -->
                </div>
            </div>

            <div class="span10">

                <!-- ko if: projectActivities().length > 0 -->
                <ul class="nav nav-pills">
                    <li class="active"><a href="#survey-info" data-toggle="pill">Survey Info</a></li>
                    <li><a href="#survey-species" data-toggle="pill">Species</a></li>
                    <li><a href="#survey-form" data-toggle="pill">Surveys Form</a></li>
                    <li><a href="#survey-locations" data-toggle="pill">Locations</a></li>
                    <li><a href="#survey-alerts" data-toggle="pill">Alerts & Actions</a></li>
                </ul>

                <div class="pill-content">
                    <div class="pill-pane active" id="survey-info">
                        <span class="validationEngineContainer" id="project-activities-info-validation">
                            <div id="project-activities-info-result-placeholder"></div>
                            <g:render template="/projectActivity/info"/>
                        </span>
                    </div>
                    <div class="pill-pane" id="survey-species">
                        <span class="validationEngineContainer" id="project-activities-species-validation">
                            <div id="project-activities-species-result-placeholder"></div>
                            <g:render template="/projectActivity/species"/>
                        </span>
                    </div>
                    <div class="pill-pane" id="survey-form">
                        <span class="validationEngineContainer" id="project-activities-form-validation">
                            <div id="project-activities-form-result-placeholder"></div>
                            <g:render template="/projectActivity/survey"/>
                        </span>
                    </div>
                    <div class="pill-pane" id="survey-locations">
                        <span class="validationEngineContainer" id="project-activities-locations-validation">
                            <div id="project-activities-sites-result-placeholder"></div>
                            <!-- Allow user to seelct the existing sites and preview those sites.-->
                            <g:render template="/projectActivity/sites"/>
                        </span>
                    </div>

                    <div class="pill-pane" id="survey-alerts">
                        survey-alerts
                    </div>
                </div>
                <!-- /ko -->

            </div>
        </div>
    </div>

    </span>

</div>

<r:script>
    function initialiseProjectActivities(pActivities, pActivityForms, projectId, sites) {
        var pActivitiesVM = new ProjectActivitiesViewModel(pActivities, pActivityForms, projectId, sites);
        ko.applyBindings(pActivitiesVM, document.getElementById('pActivities'));
    };
</r:script>

