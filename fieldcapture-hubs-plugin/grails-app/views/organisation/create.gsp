<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="${grailsApplication.config.layout.skin?:'main'}"/>
    <title>${organisation.name.encodeAsHTML()} | Field Capture</title>
    <script type="text/javascript" src="${grailsApplication.config.google.maps.url}"></script>
    <r:script disposition="head">
        var fcConfig = {
            serverUrl: "${grailsApplication.config.grails.serverURL}",
            organisationSaveUrl: "${createLink(action:'ajaxUpdate')}",
            organisationViewUrl: "${createLink(action:'index')}",
            documentUpdateUrl: "${createLink(controller:"proxy", action:"documentUpdate")}"
            };
    </r:script>
    <r:require modules="knockout,jqueryValidationEngine,amplify,organisation"/>
</head>
<body>
<div class="container-fluid">
    <ul class="breadcrumb">
        <li>
            <g:link controller="home">Home</g:link> <span class="divider">/</span>
        </li>
        <li class="active"><g:link controller="organisation" action="list">Organisations</g:link> <span class="divider">/</span></li>
        <li class="active" data-bind="text:breadcrumbName"></li>
    </ul>

    <form class="form-horizontal validationEngineContainer">
        <div class="control-group">
            <label class="control-label" for="name">Name</label>
            <div class="controls required">
                <input type="text" id="name" class="input-xxlarge" data-bind="value:name" data-validation-engine="validate[required]" placeholder="Organisation name">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="description">Description</label>
            <div class="controls required">
                <textarea rows="3" class="input-xxlarge" data-bind="value:description" data-validation-engine="validate[required]" id="description" placeholder="A description of the organisation"></textarea>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="url">Web Site URL</label>
            <div class="controls">
                <input type="text" class="input-xxlarge" id="url" data-bind="value:url" data-validation-engine="validate[custom[url]]" placeholder="link to your organisations website">
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="banner">Banner image</label>
            <div class="controls">
                <img data-bind="visible:bannerUrl(), attr:{src:bannerUrl}">
                <span class="btn fileinput-button pull-right"
                      data-url="${createLink(controller: 'image', action:'upload')}"
                      data-role="banner"
                      data-owner-type="organisationId"
                      data-owner-id="${organisation.organisationId}"
                      data-bind="stagedImageUpload:documents, visible:!logoUrl()"><i class="icon-plus"></i> <input id="banner" type="file" name="files"><span>Attach Banner Image</span></span>
            </div>
        </div>

        %{--<div class="control-group">--}%
            %{--<label class="control-label" for="logo">Logo</label>--}%
            %{--<div class="controls">--}%
                %{--<img data-bind="visible:logoUrl(), attr:{src:logoUrl}">--}%
                %{--<span class="btn fileinput-button pull-right"--}%
                      %{--data-url="${createLink(controller: 'image', action:'upload')}"--}%
                      %{--data-role="logo"--}%
                      %{--data-owner-type="organisationId"--}%
                      %{--data-owner-id="${organisation.organisationId}"--}%
                      %{--data-bind="stagedImageUpload:documents, visible:!logoUrl()"><i class="icon-plus"></i> <input id="logo" type="file" name="files"><span>Attach Logo</span></span>--}%

            %{--</div>--}%
        %{--</div>--}%

        <div class="control-group">
            <label class="control-label" for="mainImage">Organisation Image</label>
            <div class="controls">
                <img data-bind="visible:mainImageUrl(), attr:{src:mainImageUrl}">
                <span class="btn fileinput-button pull-right"
                      data-url="${createLink(controller: 'image', action:'upload')}"
                      data-role="mainImage"
                      data-owner-type="organisationId"
                      data-owner-id="${organisation.organisationId}"
                      data-bind="stagedImageUpload:documents, visible:!logoUrl()"><i class="icon-plus"></i> <input id="mainImage" type="file" name="files"><span>Attach Main Organisation Image</span></span>

            </div>
        </div>

        <div class="form-actions">
            <button type="button" id="save" data-bind="click:save" class="btn btn-primary">Create</button>
            <button type="button" id="cancel" class="btn">Cancel</button>
        </div>
    </form>

</div>
<g:render template="/shared/attachDocument"/>
<r:script>

    $(function () {
        var organisation = <fc:modelAsJavascript model="${organisation}"/>;
        var organisationViewModel = new OrganisationViewModel(organisation);

        ko.applyBindings(organisationViewModel);
        $('.validationEngineContainer').validationEngine();

    });


</r:script>

</body>


</html>