<div class="row-fluid row-eq-height" id="${containerId}">
    <div class="span4">
        %{--<div class="btn-toolbar">--}%
            %{--<div class="input-prepend input-append text-left">--}%
                %{--<span class="add-on"><i class="fa fa-filter"></i></span>--}%
                %{--<input type="text" class="input-xlarge" placeholder="Filter documents..." data-bind="textInput: documentFilter">--}%
                %{--<div class="btn-group">--}%
                    %{--<button type="button" class="btn dropdown-toggle" style="padding-bottom:3px" data-toggle="dropdown">--}%
                        %{--<span data-bind="text: documentFilterField().label"></span>--}%
                        %{--<span class="caret"></span>--}%
                    %{--</button>--}%
                    %{--<ul class="dropdown-menu" data-bind="foreach: documentFilterFieldOptions">--}%
                        %{--<li><a data-bind="{ text: $data.label, click: $parent.documentFilterField }"></a></li>--}%
                    %{--</ul>--}%
                %{--</div>--}%
            %{--</div>--}%
        %{--</div>--}%

        %{--<div class="well well-small fc-docs-list-well">--}%
            %{--<ul class="nav nav-list fc-docs-list" data-bind="foreach: { data: filteredDocuments, afterAdd: showListItem, beforeRemove: hideListItem }">--}%
                %{--<li class="pointer" data-bind="{ if: (role() == '${filterBy}' || 'all' == '${filterBy}') && role() != '${ignore}' && role() != 'variation', click: $parent.selectDocument, css: { active: $parent.selectedDocument() == $data } }">--}%
                    %{--<div class="clearfix space-after media" data-bind="template:ko.utils.unwrapObservable(type) === 'image' ? 'imageDocTmpl' : 'objDocTmpl'"></div>--}%
                %{--</li>--}%
            %{--</ul>--}%
        %{--</div>--}%

        <table id="docs-table" class="table">
            <thead>
                <th></th>
                <th>Name</th>
                <th>Stage</th>
                <th>Date</th>
                <th></th>
            </thead>
            <tbody data-bind="foreach: filteredDocuments">
                <tr data-bind="click: $parent.selectDocument">
                    <td><img class="media-object" data-bind="attr:{src:iconImgUrl(), alt:contentType, title:name}" alt="document icon" style="width:32px; height:32px;"></td>

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
            $("#docs-table").DataTable();
        }
    });

</r:script>