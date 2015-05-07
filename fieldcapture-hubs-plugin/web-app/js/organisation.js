/**
 * Knockout view model for organisation pages.
 * @param props JSON/javascript representation of the organisation.
 * @constructor
 */
OrganisationViewModel = function (props) {
    var self = $.extend(this, new Documents());
    
    self.organisationId = props.organisationId;
    self.orgType = ko.observable(props.orgType);
    self.name = ko.observable(props.name);
    self.description = ko.observable(props.description).extend({markdown:true});
    self.url = ko.observable(props.url);
    self.newsAndEvents = ko.observable(props.newsAndEvents).extend({markdown:true});;

    self.breadcrumbName = ko.computed(function() {
        return self.name()?self.name():'New Organisation';
    });

    self.detailsTemplate = ko.computed(function() {
        return self.mainImageUrl() ? 'hasMainImageTemplate' : 'noMainImageTemplate';
    });

    self.projects = props.projects;

    self.deleteOrganisation = function() {
        if (window.confirm("Delete this organisation?  Are you sure?")) {
            $.post(fcConfig.organisationDeleteUrl).complete(function() {
                    window.location = fcConfig.organisationListUrl;
                }
            );
        };
    };

    self.editOrganisation = function() {
       window.location = fcConfig.organisationEditUrl;
    };

    self.transients = {};
    self.transients.orgTypes = [
        {orgType:'aquarium', name:'Aquarium'},
        {orgType:'archive', name:'Archive'},
        {orgType:'botanicGarden', name:'Botanic Garden'},
        {orgType:'conservation', name:'Conservation'},
        {orgType:'fieldStation', name:'Field Station'},
        {orgType:'government', name:'Government'},
        {orgType:'governmentDepartment', name:'Government Department'},
        {orgType:'herbarium', name:'Herbarium'},
        {orgType:'historicalSociety', name:'Historical Society'},
        {orgType:'horticulturalInstitution', name:'Horticultural Institution'},
        {orgType:'independentExpert', name:'Independent Expert'},
        {orgType:'industry', name:'Industry'},
        {orgType:'laboratory', name:'Laboratory'},
        {orgType:'library', name:'Library'},
        {orgType:'management', name:'Management'},
        {orgType:'museum', name:'Museum'},
        {orgType:'natureEducationCenter', name:'Nature Education Center'},
        {orgType:'nonUniversityCollege', name:'Non-University College'},
        {orgType:'park', name:'Park'},
        {orgType:'repository', name:'Repository'},
        {orgType:'researchInstitute', name:'Research Institute'},
        {orgType:'school', name:'School'},
        {orgType:'scienceCenter', name:'Science Center'},
        {orgType:'society', name:'Society'},
        {orgType:'university', name:'University'},
        {orgType:'voluntaryObserver', name:'Voluntary Observer'},
        {orgType:'zoo', name:'Zoo'}
    ];

    self.toJS = function() {
        return ko.mapping.toJS(self,
            {ignore:['breadcrumbName', 'mainImageUrl', 'bannerUrl', 'logoUrl', 'detailsTemplate', 'transients']}
        );
    };

    self.save = function() {
        if ($('.validationEngineContainer').validationEngine('validate')) {

            var orgJs = self.toJS();
            var orgData = JSON.stringify(orgJs);
            $.ajax(fcConfig.organisationSaveUrl, {type:'POST', data:orgData, contentType:'application/json'}).done( function(data) {
                if (data.errors) {

                }
                else {
                    var orgId = self.organisationId?self.organisationId:data.organisationId;
                    window.location = fcConfig.organisationViewUrl+'/'+orgId;
                }

            }).fail( function() {

            });
        }
    };

    if (props.documents !== undefined && props.documents.length > 0) {
        $.each(['logo', 'banner', 'mainImage'], function(i, role){
            var document = self.findDocumentByRole(props.documents, role);
            if (document) {
                self.documents.push(document);
            }
        });
    }

    return self;

}