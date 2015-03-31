import grails.util.Environment

grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.fork.run=false

//grails.plugin.location.'images-client-plugin'='../images-client-plugin'
grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
        excludes 'xercesImpl'
    }

    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        mavenLocal()
        mavenRepo "http://nexus.ala.org.au/content/groups/public/"

    }
    def tomcatVersion = '7.0.55'
    def seleniumVersion = "2.21.0"
    def metadataExtractorVersion = "2.6.2"
    def imgscalrVersion = "4.2"
    def httpmimeVersion = "4.2.1"
    def jtsVersion = "1.8"
    def geoToolsVersion = "11.1"
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.

        compile "com.drewnoakes:metadata-extractor:${metadataExtractorVersion}"
        compile "org.imgscalr:imgscalr-lib:${imgscalrVersion}"
        compile "org.apache.httpcomponents:httpmime:${httpmimeVersion}"
        compile "com.vividsolutions:jts:${jtsVersion}"
        compile "org.geotools.xsd:gt-xsd-kml:${geoToolsVersion}"
        compile "joda-time:joda-time:2.3"
        compile "org.codehaus.groovy.modules.http-builder:http-builder:0.7.1"
        compile "org.apache.httpcomponents:httpcore:4.2.1"
        compile "org.apache.httpcomponents:httpclient:4.2.1"


        compile group: 'au.org.ala',
                name: 'ala-cas-client',
                version:'2.1-SNAPSHOT',
                transitive:false


        // runtime 'mysql:mysql-connector-java:5.1.22'
        test "org.spockframework:spock-grails-support:0.7-groovy-2.0"
        test "org.gebish:geb-spock:0.9.3"
        test("org.seleniumhq.selenium:selenium-htmlunit-driver:$seleniumVersion") {
            exclude "xml-apis"
        }
        test("org.seleniumhq.selenium:selenium-support:$seleniumVersion")
        test("org.seleniumhq.selenium:selenium-chrome-driver:$seleniumVersion")
        test("org.seleniumhq.selenium:selenium-firefox-driver:$seleniumVersion")
    }

    plugins {
        runtime ":jquery:1.11.1"
        //compile ':asset-pipeline:1.9.9'
        // required by the cached-resources plugin
        runtime ":cache-headers:1.1.6"
        if (Environment.current == Environment.PRODUCTION) {
            runtime ":cached-resources:1.0"
        }
        runtime (":rest:0.8") {
            excludes "httpclient", "httpcore"
        }
        compile ":ala-auth:1.0"
        runtime ":csv:0.3.1"
        runtime ":lesscss-resources:1.3.3"
        compile ":markdown:1.1.1"
        compile ":resources:1.2.14"

        build ":tomcat:$tomcatVersion"

        compile ':cache:1.1.8'
        compile ":cache-ehcache:1.0.5-SNAPSHOT"

        compile ":google-visualization:0.6.2"
        compile ":mail:1.0.6"

        compile ":excel-export:0.2.0"
        compile ":excel-import:1.0.1"

        compile (":images-client-plugin:0.2.3") {
            exclude "ala-web-theme"
        }

        test ":geb:0.9.3"
        test (":spock:0.7") {
            exclude "spock-grails-support"
        }
        build ":release:3.0.1"
    }
}
