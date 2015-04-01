modules = {

    ala2Skin {
        dependsOn 'defaultSkin, bootstrap, jquery_ui'
    }

    nrmSkin {
        dependsOn 'application,bootstrap_nrm'
        resource url: [dir:'css/nrm/css', file:'screen.css', plugin: 'fieldcapture-plugin'], plugin: 'fieldcapture-plugin', attrs:[media:'screen,print']
        resource url: [dir:'css/', file:'capture.css', plugin: 'fieldcapture-plugin'],  plugin: 'fieldcapture-plugin'
        resource url: [dir:'css/nrm/images/', file:'AustGovt_inline_white_on_transparent.png', plugin: 'fieldcapture-plugin'],  plugin: 'fieldcapture-plugin'
    }

    bootstrap_nrm {
        dependsOn 'app_bootstrap, jquery_ui'
        resource url: [dir:'css/nrm/less', file:'bootstrap.less'],attrs:[rel: "stylesheet/less", type:'css', media:'screen,print'], bundle:'bundle_app_bootstrap'
        resource url: 'css/empty.css'
    }

    app_bootstrap_responsive {
        dependsOn 'app_bootstrap'
        resource url: 'css/nrm/less/responsive.less' ,attrs:[rel: "stylesheet/less", type:'css', media:'screen,print'], bundle:'bundle_app_bootstrap_responsive'
        resource url: 'css/empty.css' // needed for less-resources plugin ?
    }
}
