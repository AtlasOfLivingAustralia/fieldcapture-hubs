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
    <r:require modules="knockout,mapWithFeatures,amplify,organisation"/>
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

        <button class="btn btn-success pull-right" data-bind="click:addOrganisation">Register new organisation</button>
    </div>

    <div class="row-fluid">
        <span class="span12">
            <table class="table table-striped" id="organisations">

                <tbody data-bind="foreach:currentPage">
                <tr>
                    <td class="organisation-banner" data-bind="style:{'backgroundImage':bannerUrl}">
                        <h4>
                            <a data-bind="attr:{href:fcConfig.viewOrganisationUrl+'/'+organisationId}"><span
                            data-bind="text:name"></span></a>
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


        var OrganisationsViewModel = function(organisations) {
            var self = this;

            this.allOrganisations = ko.observableArray();
            $.each(organisations, function(i, org) {
                self.allOrganisations.push(new OrganisationViewModel(org));
            });
            this.pageNum = ko.observable(1);
            this.organisationsPerPage = 10;
            this.maxPageButtons = 5;

            this.totalPages = ko.computed(function() {
                var count = self.allOrganisations().length;
                console.log(count);
                var pageCount = Math.floor(count / self.organisationsPerPage);
                console.log(pageCount);

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
        var organisationsViewModel = new OrganisationsViewModel(organisations);

        ko.applyBindings(organisationsViewModel);

});

</r:script>

</body>

</html>