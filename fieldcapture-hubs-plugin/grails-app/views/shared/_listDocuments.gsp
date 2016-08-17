<style type="text/css">
    #docs-table th {
        white-space: normal;
    }
    #docs-table .media-object {
        width:32px;
        min-width: 32px;
        height: 32px;
    }
    #filter-by-stage {
        margin-bottom: 5px;
    }

</style>
<div class="row-fluid row-eq-height" id="${containerId}">
    <div class="span4">
        <div class="row-fluid">
            <div id="filter-by-stage" class="btn-group pull-right">
                <a class="btn dropdown-toggle" href="#">
                    <i class="fa fa-filter"></i> Filter by stage
                    <span class="caret"></span>
                </a>
                <ul class="dropdown-menu" data-bind="foreach:distinctDocumentProperty('stage')">
                    <li><a href="#"><label class="checkbox"> <input name="stage-filter" class="checkbox" type="checkbox" data-bind="attr:{value:$data}"> Stage <span data-bind="text:$data"></span></label></a> </li>
                </ul>

            </div>


        </div>
        <div></div>
        <table class="docs-table table">
            <thead>
            <tr>
                <th></th>
                <th>Name</th>
                <th>Stage</th>
                <th>Date Uploaded</th>
                <th></th>
            </tr>

            </thead>
            <tbody data-bind="foreach: filteredDocuments">
                <tr data-bind="click: $parent.selectDocument">
                    <td><img class="media-object" data-bind="attr:{src:iconImgUrl(), alt:contentType, title:name}" alt="document icon"></td>

                    <td>
                         <span data-bind="text:name"></span>
                    </td>
                    <td>
                        <span data-bind="text:stage"></span>
                    </td>
                    <td>
                        <span data-bind="text:uploadDate.formattedDate()"></span>
                    </td>
                    <td>
                        <a data-bind="attr:{href:url}" target="_blank">
                            <i class="fa fa-download"></i>
                        </a>
                    </td>
                </tr>
            </tbody>
        </table>

    </div>
    <div class="fc-resource-preview-container span8" data-bind="{ template: { name: previewTemplate } }"></div>
</div>

<script id="iframeViewer" type="text/html">
<div class="well fc-resource-preview-well">
    <iframe class="fc-resource-preview" data-bind="attr: {src: selectedDocumentFrameUrl}">
        <p>Your browser does not support iframes <i class="fa fa-frown-o"></i>.</p>
    </iframe>
</div>
</script>

<script id="xssViewer" type="text/html">
<div class="well fc-resource-preview-well" data-bind="html: selectedDocument().embeddedVideo"></div>
</script>

<script id="noPreviewViewer" type="text/html">
<div class="well fc-resource-preview-well">
    <p>There is no preview available for this file.</p>
</div>
</script>

<script id="noViewer" type="text/html">
<div class="well fc-resource-preview-well">
    <p>Select a document to preview it here.</p>
</div>
</script>

<g:render template="/shared/documentTemplate"></g:render>
<r:script>
    var imageLocation = "${imageUrl}",
        useExistingModel = ${useExistingModel};

    $(function () {

        if (!useExistingModel) {

            var docListViewModel = new DocListViewModel(${documents ?: []});
            ko.applyBindings(docListViewModel, document.getElementById('${containerId}'));
        }
    });

</r:script>