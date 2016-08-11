package au.org.ala.fieldcapture

import grails.converters.JSON
import groovyx.net.http.HTTPBuilder

import org.apache.http.impl.client.AbstractHttpClient
import org.apache.http.params.BasicHttpParams

import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET


class ResourceController {

    def grailsApplication, webService, commonService

    def viewer() {}

    def imageviewer() {}

    def videoviewer() {}

    def audioviewer() {}

    def error() {}

    // proxy this request to work around browsers (firefox) that don't follow redirects properly :(
    def pdfUrl() {

        String url = grailsApplication.config.pdfgen.baseURL+'api/pdf'+commonService.buildUrlParamsFromMap(docUrl:params.file, cacheable:false)
        Map result
        try {
            result = webService.proxyGetRequest(response, url, false, false, 10*60*1000)
        }
        catch (Exception e) {
            log.error("Error generating a PDF", e)
            result = [error:"Error generating a PDF"]
        }
        if (result.error) {
            render view:'error'
        }

    }
}
