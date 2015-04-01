<%@ page import="grails.converters.JSON; au.org.ala.fieldcapture.SettingPageType" contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/html">
<head>
    <meta name="layout" content="${hubConfig.skin}"/>
    <title><g:message code="g.citizenScience"/> | <g:message code="g.fieldCapture"/></title>
    <script type="text/javascript" src="${grailsApplication.config.google.maps.url}"></script>
    <r:script disposition="head">
    var fcConfig = {
        baseUrl: "${grailsApplication.config.grails.serverURL}",
        spatialBaseUrl: "${grailsApplication.config.spatial.baseUrl}",
        spatialWmsCacheUrl: "${grailsApplication.config.spatial.wms.cache.url}",
        spatialWmsUrl: "${grailsApplication.config.spatial.wms.url}",
        sldPolgonDefaultUrl: "${grailsApplication.config.sld.polgon.default.url}",
        sldPolgonHighlightUrl: "${grailsApplication.config.sld.polgon.highlight.url}",
        dashboardUrl: "${g.createLink(controller: 'report', action: 'dashboardReport', params: params)}"
    }
    </r:script>
    <script type="text/javascript" src="//www.google.com/jsapi"></script>
    <r:require modules="knockout,js_iso8601,wmd"/>
</head>

<body>
<div id="wrapper" class="container-fluid">
    <div class="row-fluid">
        <div class="span6" id="heading">
            <h1 class="pull-left"><g:message code="project.citizenScience.heading"/></h1>
        </div>
        <g:if test="${user}">
            <div class="pull-right space-after">
                <button data-bind="click:newPortal" type="button" class="btn"><g:message
                        code="project.citizenScience.portalLink"/></button>
                <a href="${createLink(action: 'create', params: [citizenScience: true])}"
                   class="btn btn-small">
                    <i class="icon-file"></i>&nbsp;<g:message code="project.citizenScience.registerLink"/></a>
            </div>
        </g:if>
    </div>

    <div class="row-fluid"><g:message code="project.citizenScience.preamble"/></div>
    <div id="pt-root" class="span9">
        <div class="well">
            <div class="row-fluid">
                    <span id="pt-resultsReturned"></span>
                    <div class="input-append">
                        <input type="text" name="pt-search" id="pt-search"/>
                        <a href="javascript:void(0);" title="Only show projects which contain the search term" id="pt-search-link" class="btn"><g:message code="g.search" /></a>
                        <a href="javascript:void(0);" title="Remove all filters and sorting options" id="pt-reset" class="btn"><g:message code="g.reset" /></a>
                    </div>
                <a href="#" id="pt-downloadLink" class="btn pull-right"
                   title="Download metadata for projects in JSON format">
                    <i class="icon-download"></i><g:message code="g.download" /></a>
            </div>
            <div id="pt-searchControls">
                <div id="pt-sortWidgets" class="row-fluid">
                    <div class="span4">
                        <label for="pt-per-page"><g:message code="g.projects"/>&nbsp;<g:message code="g.perPage"/></label>
                        <g:select name="pt-per-page" from="[20,50,100,500]"/>
                    </div>
                    <div class="span4">
                        <label for="pt-sort"><g:message code="g.sortBy" /></label>
                        <g:select name="pt-sort" from="['Name','Description','Organisation Name','Status']"  keys="['name','description','organisationName','status']"/>
                    </div>
                    <div class="span4">
                        <label for="pt-dir"><g:message code="g.sortOrder" /></label>
                        <g:select name="pt-dir" from="['Ascending','Descending']" keys="[1,-1]"/>
                    </div>
                </div>
            </div><!--drop downs-->
        </div>

        <table class="table table-bordered table-hover" id="projectTable">
            <tbody>
            </tbody>
        </table>
        %{-- template for jQuery DOM injection --}%
        <table id="projectRowTempl" class="hide">
            <tr class="clearfix">
                <td class="td1">
                    <a href="#" class="projectTitle" id="a_" data-id="" title="click to show/hide details">
                        <span class="showHideCaret">&#9658;</span> <span class="projectTitleName">$name</span></a>
                    <div class="projectInfo" id="proj_$id" style="position:relative;margin-left:130px">
                        <img style="position:absolute; left:-128px; width:120px; height:120px">
                        <div style="float:left;width:1px;height:120px"></div>
                        <div class="homeLine">
                            <i class="icon-home"></i>
                            <a href="">View project page</a>
                        </div>
                        <div class="orgLine">
                            <i class="icon-user"></i>
                        </div>
                        <div class="descLine">
                        </div>
                        <div class="linksLine">
                            <i class="icon-info-sign"></i>
                        </div>
                    </div>
                </td>
                <td class="td2" style="width:10px"><a><i class="icon-edit"></i></a></td>
            </tr>
        </table>

        <div id="pt-searchNavBar" class="clearfix">
            <div id="pt-navLinks"></div>
        </div>
    </div>
</div>
<r:script>
/* holds full list of projects */
function moreOrLess(dom, more) {
    dom = $(dom).closest("DIV");
    dom.hide();
    if (more)
        dom.next().show();
    else
        dom.prev().show();
}

$(document).ready(function () {
    function moreless(text, maxLength) {
        if (text.length < maxLength) return text;
        return '<div>' + text.substring(0, maxLength - 4)
            + '... <a onclick="moreOrLess(this, true)"><g:message code="g.more"/></a></div><div style="display:none">'
            + text + '<a onclick="moreOrLess(this)"><g:message code="g.less"/></a></div>';
    }
    var markdown = new Showdown.converter();
    var ProjectVM = function (props) {
        var dl = this.download = {
            projectId: this.projectId = props[0],
            name: this.name = props[4],
            description: props[2],
            orgName: this.organisationName = props[5],
            status: this.status = props[6],
            urlWeb: props[9],
            urlAndroid: props[7],
            urlITunes: props[8],
            urlImage: this.mainImageUrl = props[10]
        }
        this.coverage = props[1];
        this.description = markdown.makeHtml(dl.description);
        this.descriptionTrimmed = moreless(this.description, 100);
        this.editable = props[3];
        this.links = (dl.urlWeb? '<a href="' + dl.urlWeb + '">Website</a> ': '')
                   + (dl.urlAndroid? '<a href="' + dl.urlAndroid + '">Android</a> ': '')
                   + (dl.urlITunes? '<a href="' + dl.urlITunes + '">ITunes</a>': '');
        this.searchText = (this.name + ' ' + this.description + ' ' + this.organisationName).toLowerCase();
    }

    var allProjects = [
    <g:each var="p" in="${projects}">new ProjectVM(${p as JSON}),</g:each>
    ];

    /* holds current filtered list */
    var projects;

    /* pagination offset into the record set */
    var offset = 0;

    /* size of current filtered list */
    var total = 0;

    var searchTerm = '', perPage = 20, sortBy = 'name', sortOrder = 1;

    /* the base url of the home server */
    var baseUrl = fcConfig.baseUrl;

    /** load projects and show first page **/
    allProjects.sort(comparator); // full list is sorted by name
    projects = allProjects;
    updateTotal();
    populateTable();

    /*************************************************\
     *  Filter projects by search term
     \*************************************************/
    function doSearch() {
        var val = $('#pt-search').val().toLowerCase();
        if (val == searchTerm) return;
        searchTerm = val;
        projects = [];
        for (var i = 0; i < allProjects.length; i++) {
            var item = allProjects[i];
            if (item && item.searchText.indexOf(searchTerm) >= 0)
                projects.push(item);
        }
        offset = 0;
        updateTotal();
        populateTable();
    }

    /*************************************************\
     *  Show filtered projects on current page
     \*************************************************/
    function populateTable() {
        var editPrefix = "${createLink(action: 'edit')}/";
        var max = offset + perPage;
        if (max > projects.length) max = projects.length;
        $('#projectTable tbody').empty();
        $.each(projects.slice(offset, max), function(i, src) {
            var id = src.projectId;
            var $tr = $('#projectRowTempl tr').clone(); // template
            $tr.find('.td1 > a').attr("id", "a_" + id).data("id", id);
            $tr.find('.td1 .projectTitleName').text(src.name); // projectTitleName
            $tr.find('.projectInfo').attr("id", "proj_" + id);
            $tr.find('.homeLine a').attr("href", "${createLink()}/" + id);
            $tr.find('a.zoom-in').data("id", id);
            $tr.find('a.zoom-out').data("id", id);
            $tr.find('.orgLine').append(src.organisationName);
            $tr.find('.descLine').append(src.description);
            if (src.links)
                $tr.find('.linksLine').append(src.links);
            else
                $tr.find('.linksLine').hide();
            if (src.mainImageUrl)
                $tr.find('.projectInfo img').attr("src", src.mainImageUrl);
            else
                $tr.find('.projectInfo img').hide();
            if (src.editable)
                $tr.find('.td2 a').attr("href", editPrefix + id);
            else
                $tr.find('.td2 a').hide();
            $('#projectTable tbody').append($tr);
        });
    }

    var prevFeatureId;
    $('#projectTable').on("click", ".projectTitle", function(el) {
        el.preventDefault();
        var thisEl = this;
        var fId = $(this).data("id");
        prevFeatureId = fId; // always toggle - no previous feature
        if (!prevFeatureId) {
            $("#proj_" + fId).slideToggle();
            $(thisEl).find(".showHideCaret").html("&#9660;");
        } else if (prevFeatureId != fId) {
            if ($("#proj_" + prevFeatureId).is(":visible")) {
                // hide prev selected, show this
                $("#proj_" + prevFeatureId).slideUp();
                $("#a_" + prevFeatureId).find(".showHideCaret").html("&#9658;");
                $("#proj_" + fId).slideDown();
                $("#a_" + fId).find(".showHideCaret").html("&#9660;");
            } else {
                //show this, hide others
                $("#proj_" + fId).slideToggle();
                $(thisEl).find(".showHideCaret").html("&#9660;");
                $("#proj_" + prevFeatureId).slideUp();
                $("#a_" + prevFeatureId).find(".showHideCaret").html("&#9658;");
            }
        } else {
            $("#proj_" + fId).slideToggle();
            if ($("#proj_" + fId).is(':visible')) {
                $(thisEl).find(".showHideCaret").html("&#9658;");
            } else {
                $(thisEl).find(".showHideCaret").html("&#9660;");
            }
        }
        prevFeatureId = fId;
    });


    /** display the current size of the filtered list **/
    function updateTotal() {
        total = projects.length;
        $('#pt-resultsReturned').html("Found <strong>" + total + "</strong> " + (total == 1 ? 'project.' : 'projects.'));
    }

    /*************************************************\
     *  Pagination
     \*************************************************/
    /** build and append the pagination widget **/
    function showPaginator() {
        if (total <= perPage) {
            // no pagination required
            $('div#navLinks').html("");
            return;
        }
        var currentPage = Math.floor(offset / perPage) + 1;
        var maxPage = Math.ceil(total / perPage);
        var $pago = $("<div class='pagination'></div>");
        // add prev
        if (offset > 0)
            $pago.append('<a href="javascript:prevPage();">« Previous</a></li>');
        else
            $pago.append('« Previous</span>');
        for (var i = 1; i <= maxPage && i < 20; i++) {
            if (i == currentPage)
                $pago.append('<span class="currentPage disabled">' + i + '</span>');
            else
                $pago.append('<a href="javascript:gotoPage(' + i + ');">' + i + '</a>');
        }
        // add next
        if ((offset + perPage) < total)
            $pago.append('<a href="javascript:nextPage();">Next »</a>');
        else
            $pago.append('Next »');

        $('div#navLinks').html($pago);
    }

    /** show the specified page **/
    function gotoPage(pageNum) {
        // calculate new offset
        offset = (pageNum - 1) * perPage;
        populateTable();
    }

    /** show the previous page **/
    function prevPage() {
        offset -= perPage;
        populateTable();
    }

    /** show the next page **/
    function nextPage() {
        offset += perPage;
        populateTable();
    }

    /* comparator for data projects */
    function comparator(a, b) {
        var va = a[sortBy], vb = b[sortBy];
        va = va? va.toLowerCase(): '';
        vb = vb? vb.toLowerCase(): '';
        if (va == vb && sortBy != 'name') { // sort on name
            va = a.name.toLowerCase();
            vb = b.name.toLowerCase();
        }
        return (va < vb ? -1 : (va > vb ? 1 : 0)) * sortOrder;
    }

    $('#pt-per-page').change(function () {
        perPage = $('#pt-per-page').val();
        offset = 0;
        populateTable();
    });
    $('#pt-sort').change(function () {
        sortBy = $('#pt-sort').val();
        projects.sort(comparator);
        populateTable();
    });
    $('#pt-dir').change(function () {
        sortOrder = $('#pt-dir').val();
        projects.sort(comparator);
        populateTable();
    });
    $('#pt-search-link').click(function () {
        doSearch();
    });
    $('#pt-search').keypress(function (event) {
        if (event.which == 13) {
            event.preventDefault();
            doSearch();
        }
    });
    $('#pt-downloadLink').click(function () {
        $('body').html(JSON.stringify($.map(projects, function(v) {
            return v.download;
        })));
    });
    $('#pt-reset').click(function () {
        projects = allProjects;
        $('#pt-per-page').val(perPage = 20);
        $('#pt-sort').val(sortBy = 'name');
        $('#pt-dir').val(sortOrder = 1);
        offset = 0;
        updateTotal();
        populateTable();
    });
});
</r:script>
</body>
</html>
