import grails.util.Environment

grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: false, // [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    // configure settings for the run-app JVM
    run: false, // [maxMemory: 768, minMemory: 512, debug: true, maxPerm: 256],
    // configure settings for the run-war JVM
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    // configure settings for the Console UI JVM
    console: false// [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

def inlinePluginAvailable = new File('../fieldcapture-hubs-plugin/application.properties').exists()

// settings for the inline fieldcapture-plugin
if (Environment.current == Environment.DEVELOPMENT && inlinePluginAvailable) {
    grails.plugin.location.'fieldcapture-plugin' = '../fieldcapture-hubs-plugin'
    println('Using inline fieldcapture-plugin...')
}
grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {

    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve true // needs to be true for inline plugin whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        mavenLocal()
        mavenRepo "http://nexus.ala.org.au/content/groups/public/"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
        // runtime 'mysql:mysql-connector-java:5.1.27'
        // runtime 'org.postgresql:postgresql:9.3-1100-jdbc41'
        //test "org.grails:grails-datastore-test-support:1.0-grails-2.3"
        //compile "org.elasticsearch:elasticsearch:0.90.12"
    }

    plugins {
        // plugins for the build system only
        build ":tomcat:7.0.55"

        // plugins for the compile step
        //compile ":scaffolding:2.0.3"
        //compile ':cache:1.1.6'

        // plugins needed at runtime but not for compilation
        runtime ":jquery:1.11.1"
        //compile ':asset-pipeline:1.8.3'
        compile ":ala-bootstrap2:1.2"

        if (Environment.current != Environment.DEVELOPMENT || !inlinePluginAvailable) {
            compile ":fieldcapture-plugin:1.0-SNAPSHOT"
        }

        build ":release:3.0.1"

        // Uncomment these (or add new ones) to enable additional resources capabilities
        //runtime ":zipped-resources:1.0.1"
        //runtime ":cached-resources:1.1"
        //runtime ":yui-minify-resources:0.1.5"

        // An alternative to the default resources plugin is the asset-pipeline plugin
        //compile ":asset-pipeline:1.6.1"

        // Uncomment these to enable additional asset-pipeline capabilities
        //compile ":sass-asset-pipeline:1.5.5"
        //compile ":less-asset-pipeline:1.5.3"
        //compile ":coffee-asset-pipeline:1.5.0"
        //compile ":handlebars-asset-pipeline:1.3.0.1"
    }
}
