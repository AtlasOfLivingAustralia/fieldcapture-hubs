<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${hubConfig.skin}"/>
    <title>Organisations | Field Capture</title>
    <script type="text/javascript" src="${grailsApplication.config.google.maps.url}"></script>
    <r:script disposition="head">
        var fcConfig = {
            serverUrl: "${grailsApplication.config.grails.serverURL}",
            createOrganisationUrl: "${createLink(controller: 'organisation', action: 'create')}",
            viewOrganisationUrl: "${createLink(controller: 'organisation', action: 'index')}",

            };
    </r:script>
    <r:require modules="knockout,mapWithFeatures,amplify,organisation,fuseSearch"/>
</head>

<body>
<div class="container-fluid">
    <ul class="breadcrumb">
        <li>
            <g:link controller="home">Home</g:link><span class="divider">/</span>
        </li>
        <li class="active">Organisations<span class="divider"></span></li>
    </ul>


    <div>
        <h2 style="display:inline">Registered Organisations</h2>

        <g:if test="${user}">
            <button class="btn btn-success pull-right" data-bind="click:addOrganisation">Register new organisation</button>
        </g:if>
    </div>

    <div class="row-fluid">
        <div class="span6">
            <input id="searchText" class="span12" placeholder="Search..." />
        </div>
        <div class="span2">
            <input id="searchByName" type="checkbox" checked="true" class="fuse-checkbox" />
            <label for="searchByName" style="display:inline">By name</label></p>
        </div>
        <div class="span2">
            <input id="searchByDescription" type="checkbox" class="fuse-checkbox" />
            <label for="searchByDescription" style="display:inline">By description</label></p>
        </div>
        <div class="span2">
            <input id="searchCaseSensitive" type="checkbox" class="fuse-checkbox" />
            <label for="searchCaseSensitive" style="display:inline">Case sensitive</label>
        </div>
    </div>
    <div class="row-fluid">
        <span class="span12">
            <table class="table table-striped" id="organisations">

                <tbody data-bind="foreach:currentPage">
                <tr>
                    <td class="organisation-banner" data-bind="style:{'backgroundImage':bannerUrl}">
                        <h4>
                            <a data-bind="visible:organisationId,attr:{href:fcConfig.viewOrganisationUrl+'/'+organisationId}"><span
                            data-bind="text:name"></span></a>
                            <b data-bind="visible:!organisationId,text:name"></b>
                        </h4>
                        <span data-bind="html:description.markdownToHtml()"></span>
                    </td>
                </tr>
                </tbody>
                <tfoot>
                <tr>
                    <td>
                        <button class="btn" data-bind="click:prev,enable:hasPrev()"><i class="icon-arrow-left"></i> Prev</button>
                        <!-- ko foreach: pageList -->
                        <button class="btn" data-bind="click:$parent.pageNum($data),text:$data,css: {active:$data == $parent.pageNum()}"></button>
                        <!-- /ko -->
                        <button class="btn" data-bind="click:next,enable:hasNext()">Next <i class="icon-arrow-right"></i></button>

                    </td>

                </tr>
                </tfoot>

            </table>
        </span>

    </div>

</div>


<r:script>

    $(function () {

        var userOrgs = [], otherOrgs = [];

        var OrganisationsViewModel = function(organisations, userOrgIds) {
            var self = this, userOrgIdsMap = {};
            $.each(userOrgIds, function(i, id) {
                userOrgIdsMap[id] = true;
            });
            $.each(organisations, function(i, org) {
                var model = new OrganisationViewModel(org);
                model.fuseName = model.name();
                model.fuseDescription = model.description.markdownToHtml();
                if (userOrgIdsMap[org.organisationId])
                    userOrgs.push(model);
                else
                    otherOrgs.push(model);
            });

            this.allOrganisations = ko.observableArray();
            this.pageNum = ko.observable(1);
            this.organisationsPerPage = 10;
            this.maxPageButtons = 5;

            this.totalPages = ko.computed(function() {
                var count = self.allOrganisations().length;
                var pageCount = Math.floor(count / self.organisationsPerPage);
                return count % self.organisationsPerPage > 0 ? pageCount + 1 : pageCount;
            });

            this.currentPage = ko.computed(function() {
                var first = (self.pageNum()-1) * self.organisationsPerPage;
                return self.allOrganisations().slice(first, first+self.organisationsPerPage);
            });

            this.pageList = ko.computed(function() {
                var pages = [];

                for (var i=1; i<=self.totalPages(); i++) {
                    pages.push(i);
                }

                return pages;
            });

            this.hasPrev = ko.computed(function() {
                return self.pageNum() > 1;
            })

            this.hasNext = ko.computed(function() {
                return self.pageNum() < self.totalPages();
            });

            this.next = function() {
                if (self.hasNext()) {
                    self.pageNum(self.pageNum()+1);
                }
            };
            this.prev = function() {
                if (self.hasPrev()) {
                    self.pageNum(self.pageNum()-1);
                }
            };

            this.addOrganisation = function() {
                window.location = fcConfig.createOrganisationUrl;
            }

        };

        var organisations = <fc:modelAsJavascript model="${organisations}" default="${[]}"/>;
        var userOrgIds = <fc:modelAsJavascript model="${userOrgIds}" default="${[]}"/>;
        var organisationsViewModel = new OrganisationsViewModel(organisations, userOrgIds);

        ko.applyBindings(organisationsViewModel);

        (function() {
            var fuseUserOrgs, fuseOtherOrgs, userOrgsBanner = {
                  organisationId: "",
                  bannerUrl: "",
                  description: ko.observable("").extend({markdown:true}),
                  name: "You are a member of the following organisations"
                }, otherOrgsBanner = {
                  organisationId: "",
                  bannerUrl: "",
                  description: ko.observable("").extend({markdown:true}),
                  name: "You are NOT a member of the following organisations"
                };

            function search() {
              var orgs, key = $('#searchText').val();
              organisationsViewModel.allOrganisations.removeAll();
              orgs = fuseUserOrgs && key? fuseUserOrgs.search(key): userOrgs;
              if (orgs.length) {
                organisationsViewModel.allOrganisations.push(userOrgsBanner);
                $.each(orgs , function(i, org) {
                  organisationsViewModel.allOrganisations.push(org);
                });
              }
              orgs = fuseOtherOrgs && key? fuseOtherOrgs.search(key): otherOrgs;
              if (orgs.length) {
                organisationsViewModel.allOrganisations.push(otherOrgsBanner);
                $.each(orgs, function(i, org) {
                  organisationsViewModel.allOrganisations.push(org);
                });
              }
              if (organisationsViewModel.pageNum() === 1)
                organisationsViewModel.pageNum.valueHasMutated();
              else
                organisationsViewModel.pageNum(1);
            }
            function mkfuse() {
              var keys = [];
              if ($('#searchByName').prop('checked')) keys.push('fuseName');
              if ($('#searchByDescription').prop('checked')) keys.push('fuseDescription');
              fuseUserOrgs = userOrgs && keys.length && new Fuse(userOrgs, {
                keys: keys,
                caseSensitive: $('#searchCaseSensitive').prop('checked')
              });
              fuseOtherOrgs = otherOrgs && keys.length && new Fuse(otherOrgs, {
                keys: keys,
                caseSensitive: $('#searchCaseSensitive').prop('checked')
              });
              search();
            }

            $('#searchByName').on('change', mkfuse);
            $('#searchByDescription').on('change', mkfuse);
            $('#searchCaseSensitive').on('change', mkfuse);
            $('#searchText').on('keyup', search);
            mkfuse();
        })();
});

</r:script>

</body>

</html>