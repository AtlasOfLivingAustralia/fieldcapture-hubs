// Karma configuration
// Generated on Thu May 21 2015 09:01:47 GMT+1000 (AEST)

module.exports = function (config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',

        plugins: [
            'karma-jquery',
            'karma-chrome-launcher',
            'karma-jasmine',
            'karma-jasmine-jquery',
            'karma-coverage',
            'karma-firefox-launcher',
            'karma-phantomjs-launcher'
        ],


        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: [
            'jquery-1.11.0', // Because we are using a grails plugin for jquery it is not easily available via a project path.
            'jasmine-jquery',
            'jasmine'],


        // list of files / patterns to load in the browser
        files: [
            'test/js/util/**/*.js',
            'web-app/vendor/underscorejs/1.8.3/underscore.js',
            'web-app/vendor/knockout/3.4.0/knockout-3.4.0.js',
            'web-app/js/knockout.mapping-latest.js',
            'web-app/js/knockout-dates.js',
            'web-app/wmd/showdown.js',
            'web-app/wmd/wmd.js',
            'web-app/js/document.js',
            'web-app/js/fuse.min.js',
            'web-app/js/fieldcapture-application.js',
            'web-app/js/projects.js',
            'web-app/vendor/momentjs/moment.min.js',
            'web-app/vendor/momentjs/moment-timezone-with-data.min.js',
            'web-app/js/organisation.js',
            'web-app/js/sites.js',
            'web-app/vendor/typeahead/0.11.1/typeahead.bundle.js',
            'web-app/vendor/select2/4.0.3/js/select2.full.js',
            'web-app/js/speciesModel.js',
            'web-app/js/projectActivity.js',
            'web-app/js/outputs.js',
            'test/js/spec/**/*.js'
        ],


        // list of files to exclude
        exclude: [],


        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {},


        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress', 'coverage'],

        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['Chrome','Firefox','PhantomJS'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true
    });
};
