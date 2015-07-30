modules = {
    application {
        dependsOn 'jquery,knockout'
        resource url: "${grailsApplication.config.ala.baseURL?:'http://www.ala.org.au'}/wp-content/themes/ala2011/images/favicon.ico", attrs:[type:'ico'], disposition: 'head'
        resource url: 'js/html5.js', plugin: "fieldcapture-plugin", wrapper: { s -> "<!--[if lt IE 9]>$s<![endif]-->" }, disposition: 'head'
        resource url: 'js/vkbeautify.0.99.00.beta.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fieldcapture-application.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/jquery.shorten.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/bootbox.min.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/jquery.columnizer.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/jquery.blockUI.js', plugin: 'fieldcapture-plugin'
    }

    defaultSkin {
        dependsOn 'application'
        resource url: 'css/default.skin.css', plugin: 'fieldcapture-plugin'
    }

    nrmSkin {
        dependsOn 'application,app_bootstrap_responsive'
        resource url: [dir:'css/nrm/css', file:'screen.css', plugin: 'fieldcapture-plugin'], plugin: 'fieldcapture-plugin', attrs:[media:'screen,print']
        resource url: [dir:'css/', file:'capture.css', plugin: 'fieldcapture-plugin'],  plugin: 'fieldcapture-plugin'
        resource url: [dir:'css/nrm/images/', file:'AustGovt_inline_white_on_transparent.png', plugin: 'fieldcapture-plugin'],  plugin: 'fieldcapture-plugin'
    }

    wmd {
        resource url:[ dir:'wmd', file:"wmd.css", plugin:'fieldcapture-plugin']
        resource url:[ dir:'wmd', file:"showdown.js", plugin:'fieldcapture-plugin']
        resource url:[ dir:'wmd', file:"wmd.js", plugin:'fieldcapture-plugin']
        resource url:[ dir:'wmd', file:'wmd-buttons.png', plugin:'fieldcapture-plugin']

    }

    nrmPrintSkin {
        dependsOn 'nrmSkin'
        resource url: 'css/print.css', plugin: 'fieldcapture-plugin', attrs:[media:'screen,print']
    }

    gmap3 {
        resource url: 'js/gmap3.min.js', plugin: 'fieldcapture-plugin'
    }

    projectsMap {
        resource url: 'js/projects-map.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/wms.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/keydragzoom.js', plugin: 'fieldcapture-plugin'
    }

    mapWithFeatures {
        resource url: 'js/wms.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/mapWithFeatures.js', plugin: 'fieldcapture-plugin'
    }

    knockout {
        resource url:'js/knockout-3.3.0.debug.js', plugin: 'fieldcapture-plugin'
        resource url:'js/knockout.mapping-latest.js', plugin: 'fieldcapture-plugin'
        resource url:'js/knockout-custom-bindings.js', plugin: 'fieldcapture-plugin'
        resource url:'js/knockout-dates.js', plugin: 'fieldcapture-plugin'
        resource url:'js/outputs.js', plugin: 'fieldcapture-plugin'
    }

    knockout_sortable {
        dependsOn 'knockout'
        resource url:'js/knockout-sortable.min.js', plugin: 'fieldcapture-plugin'
    }

    jqueryValidationEngine {
        resource url: 'js/jquery.validationEngine.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/jquery.validationEngine-en.js', plugin: 'fieldcapture-plugin'
        resource url: 'css/validationEngine.jquery.css', plugin: 'fieldcapture-plugin'
    }

    datepicker {
        resource url: 'bootstrap-datepicker/js/bootstrap-datepicker.js', plugin: 'fieldcapture-plugin'
        resource url: 'bootstrap-datepicker/css/datepicker.css', plugin: 'fieldcapture-plugin'
    }

    app_bootstrap {
        dependsOn 'application'
        resource url: 'bootstrap/js/bootstrap.min.js', plugin: 'fieldcapture-plugin'
        // The less css resources plugin (1.3.3, resources plugin 1.2.14) is unable to resolve less files in a plugin so apps that use this plugin must supply their own bootstrap styles.
        // However, commenting this section
        resource url: [dir:'bootstrap/less/', file:'bootstrap.less', plugin: 'fieldcapture-plugin'],attrs:[rel: "stylesheet/less", type:'css', media:'screen,print'], bundle:'bundle_app_bootstrap'
        resource url: 'bootstrap/img/glyphicons-halflings-white.png', plugin: 'fieldcapture-plugin'
        resource url: 'bootstrap/img/glyphicons-halflings.png', plugin: 'fieldcapture-plugin'
        resource url: 'css/empty.css' , plugin: 'fieldcapture-plugin'// needed for less-resources plugin ?
        resource url: 'js/bootstrap-combobox.js', plugin: 'fieldcapture-plugin'
        resource url: 'css/bootstrap-combobox.css', plugin: 'fieldcapture-plugin'
    }

    app_bootstrap_responsive {
        dependsOn 'app_bootstrap'
        resource url: 'bootstrap/less/responsive.less', plugin: 'fieldcapture-plugin',attrs:[rel: "stylesheet/less", type:'css', media:'screen,print'], bundle:'bundle_app_bootstrap_responsive'
        resource url: 'css/empty.css', plugin: 'fieldcapture-plugin' // needed for less-resources plugin ?
    }

    amplify {
        defaultBundle 'application'
        resource url: 'js/amplify.min.js', plugin: 'fieldcapture-plugin'
    }

    jstimezonedetect {
        resource url:'js/jstz.min.js', plugin: 'fieldcapture-plugin'
    }

    js_iso8601 {
        resource url:'js/js-iso8601.min.js', plugin: 'fieldcapture-plugin'
    }

    jquery_ui {
        dependsOn 'jquery'
        resource url:'js/jquery-ui-1.9.2.custom.min.js', plugin: 'fieldcapture-plugin'
        resource url:'css/smoothness/jquery-ui-1.9.2.custom.min.css', plugin: 'fieldcapture-plugin'
        resource url:'css/jquery-autocomplete.css', plugin: 'fieldcapture-plugin'
        resource url:'js/jquery.appear.js', plugin: 'fieldcapture-plugin'
    }

    jquery_bootstrap_datatable {
        resource url:'js/jquery.dataTables.js', plugin: 'fieldcapture-plugin'
        resource url:'js/jquery.dataTables.bootstrap.js', plugin: 'fieldcapture-plugin'
        resource url:'js/dataTables.tableTools.min.js', plugin: 'fieldcapture-plugin'
        resource url:'css/dataTables.bootstrap.css', plugin: 'fieldcapture-plugin'
        resource url:'css/dataTables.tableTools.min.css', plugin: 'fieldcapture-plugin'
        resource url:'images/sort_asc.png', plugin: 'fieldcapture-plugin'
        resource url:[dir:'images', file:'sort_asc_disabled.png', plugin: 'fieldcapture-plugin']
        resource url:[dir:'images', file:'sort_both.png', plugin: 'fieldcapture-plugin']
        resource url:[dir:'images', file:'sort_desc.png', plugin: 'fieldcapture-plugin']
        resource url:[dir:'images', file:'sort_desc_disabled.png', plugin: 'fieldcapture-plugin']

    }

    drawmap {
        defaultBundle true
        resource url:'js/keydragzoom.js', plugin: 'fieldcapture-plugin'
        resource url:'js/wms.js', plugin: 'fieldcapture-plugin'
        resource url:'js/selection-map.js', plugin: 'fieldcapture-plugin'
    }

    jQueryFileUpload {
        dependsOn 'jquery_ui'
        resource url: 'css/jquery.fileupload-ui.css', plugin: 'fieldcapture-plugin', disposition: 'head'

        resource url: 'js/fileupload-9.0.0/load-image.min.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/jquery.fileupload.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/jquery.fileupload-process.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/jquery.fileupload-image.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/jquery.fileupload-video.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/jquery.fileupload-validate.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/jquery.fileupload-audio.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/jquery.iframe-transport.js', plugin: 'fieldcapture-plugin'

        resource url: 'js/locale.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/cors/jquery.xdr-transport.js', plugin: 'fieldcapture-plugin',
                wrapper: { s -> "<!--[if gte IE 8]>$s<![endif]-->" }
    }

    jQueryFileUploadUI {
        dependsOn 'jQueryFileUpload'

        resource url: 'js/fileupload-9.0.0/jquery.fileupload-ui.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/fileupload-9.0.0/tmpl.js', plugin: 'fieldcapture-plugin'

    }
    jQueryFileDownload{
        resource url: 'js/jQuery.fileDownload.js', plugin: 'fieldcapture-plugin'
    }

    attachDocuments {
        defaultBundle 'application'
        dependsOn 'jQueryFileUpload'
        resource url: 'js/document.js', plugin: 'fieldcapture-plugin'
    }

    activity {
        defaultBundle 'application'
        dependsOn 'knockout'
        resource url:'js/outputs.js', plugin: 'fieldcapture-plugin'
        resource url:'js/parser.js', plugin: 'fieldcapture-plugin'
    }

    jqueryGantt {
        resource url:[dir:'jquery-gantt/css/', file:'style.css', plugin: 'fieldcapture-plugin']
        resource url:'css/gantt.css', plugin: 'fieldcapture-plugin'
        resource url:[dir:'jquery-gantt/js/', file:'jquery.fn.gantt.js', plugin: 'fieldcapture-plugin']
        resource url:[dir:'jquery-gantt/img/', file:'grid.png', plugin: 'fieldcapture-plugin']
        resource url:[dir:'jquery-gantt/img/', file:'icon_sprite.png', plugin: 'fieldcapture-plugin']
        resource url:[dir:'jquery-gantt/img/', file:'slider_handle.png', plugin: 'fieldcapture-plugin']

    }

    projects {
        defaultBundle 'application'
        dependsOn 'knockout','attachDocuments','wmd'
        resource url:'js/projects.js', plugin: 'fieldcapture-plugin'
        resource url:'js/sites.js', plugin: 'fieldcapture-plugin'
        resource url:'js/moment.min.js', plugin: 'fieldcapture-plugin'
    }

    jquery_cookie {
        defaultBundle 'application'
        dependsOn 'jquery'
        resource url:'js/jquery.cookie.js', plugin: 'fieldcapture-plugin'
    }

    projectActivity {
        defaultBundle 'application'
        dependsOn 'knockout'
        resource url:'js/projectActivity.js', plugin: 'fieldcapture-plugin'
    }

    species {
        defaultBundle 'application'
        dependsOn 'knockout'
        resource url:'js/speciesModel.js', plugin: 'fieldcapture-plugin'
    }

    imageViewer {
        dependsOn 'viewer', 'jquery'
        resource 'fancybox/jquery.fancybox.js'
        resource 'fancybox/jquery.fancybox.css?v=2.1.5'
        resource url:'fancybox/fancybox_overlay.png', plugin: 'fieldcapture-plugin'
        resource url:'fancybox/fancybox_sprite.png', plugin: 'fieldcapture-plugin'
        resource url:'fancybox/fancybox_sprite@2x.png', plugin: 'fieldcapture-plugin'
        resource url:'fancybox/blank.gif', plugin: 'fieldcapture-plugin'
        resource url:'fancybox/fancybox_loading@2x.gif', plugin: 'fieldcapture-plugin'
    }

    fuelux {
        dependsOn 'app_bootstrap_responsive'
        resource 'fuelux/js/fuelux.min.js'
        resource 'fuelux/css/fuelux.min.css'

    }

    fuseSearch {
        dependsOn 'jquery'
        resource url: 'js/fuse.min.js', plugin: 'fieldcapture-plugin'
    }

    wizard {
        dependsOn 'app_bootstrap_responsive'
        resource 'fuelux/js/wizard.js'
        resource 'fuelux/css/fuelux.min.css'
    }

    organisation {
        defaultBundle 'application'
        dependsOn 'jquery', 'knockout','wmd'
        resource 'js/organisation.js'
    }

    slickgrid {
        dependsOn 'jquery', 'jquery_ui'
        resource 'slickgrid/slick.grid.css'
        //resource 'slickgrid/slick-default-theme.css'
        //resource 'slickgrid/css/smoothness/jquery-ui-1.8.16.custom.css'
        //resource 'slickgrid/examples.css'

        resource 'slickgrid/lib/jquery.event.drag-2.2.js'
        resource 'slickgrid/lib/jquery.event.drop-2.2.js'

        resource 'slickgrid/slick.core.js'
        resource 'slickgrid/slick.dataview.js'
        //resource 'slickgrid/plugins/slick.cellcopymanager.js'
        //resource 'slickgrid/plugins/slick.cellrangedecorator.js'
        //resource 'slickgrid/plugins/slick.cellrangeselector.js'
        //resource 'slickgrid/plugins/slick.cellselectionmodel.js'


        resource 'slickgrid/slick.formatters.js'
        resource 'slickgrid/slick.editors.js'

        resource 'slickgrid/slick.grid.js'

        resource 'js/slickgrid.support.js'

        resource url:'slickgrid/images/header-columns-bg.gif', plugin:'fieldcapture-plugin'
        resource url:'slickgrid/images/header-columns-over-bg.gif', plugin:'fieldcapture-plugin'


    }

    pretty_text_diff{
        resource url: 'js/prettytextdiff/jquery.pretty-text-diff.min.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/prettytextdiff/diff_match_patch.js', plugin: 'fieldcapture-plugin'
        resource url: 'js/prettytextdiff/pretty_text_diff_basic.css', plugin: 'fieldcapture-plugin'
    }

    sliderpro {
        dependsOn 'jquery'
        resource url: 'slider-pro-master/js/jquery.sliderPro.min.js', plugin: 'fieldcapture-plugin'
        resource url: 'slider-pro-master/css/slider-pro.min.css', plugin: 'fieldcapture-plugin'
        resource url: 'slider-pro-master/css/images/blank.gif', plugin: 'fieldcapture-plugin'

    }

}
