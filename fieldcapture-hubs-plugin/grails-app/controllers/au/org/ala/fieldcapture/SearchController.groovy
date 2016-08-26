package au.org.ala.fieldcapture
import grails.converters.JSON
import org.apache.commons.lang.StringUtils

class SearchController {
    def searchService, webService, speciesService, grailsApplication, commonService, documentService, reportService

    /**
     * Main search page that takes its input from the search bar in the header
     * @param query
     * @return resp
     */
    def index(String query) {
        params.facets = StringUtils.join(SettingService.getHubConfig().availableFacets, ',')+',className'
        [facetsList: params.facets.tokenize(","), results: searchService.fulltextSearch(params)]
    }

    /**
     * Handles queries to support autocomplete for species fields.
     * @param q the typed query.
     * @param limit the maximum number of results to return
     * @return
     */
    def species(String q, Integer limit) {

        render speciesService.searchForSpecies(q, limit, params.listId) as JSON

    }

    def searchSpeciesList(String sort, Integer max, Integer offset){
        render speciesService.searchSpeciesList(sort, max, offset) as JSON
    }

    @PreAuthorise(accessLevel = 'siteReadOnly', redirectController ='home', redirectAction = 'index')
    def downloadSearchResults() {
        def path = 'search/downloadSearchResults'
        if (params.view == 'xlsx') {
             path += ".xlsx"
        }
        def facets = []
        facets.addAll(params.getList("fq"))
        facets << "className:au.org.ala.ecodata.Project"
        params.put("fq", facets)
        searchService.addDefaultFacetQuery(params)
        def url = grailsApplication.config.ecodata.baseUrl + path +  commonService.buildUrlParamsFromMap(params)
        webService.proxyGetRequest(response, url, true, true)
    }

    @PreAuthorise(accessLevel = 'siteAdmin', redirectController ='home', redirectAction = 'index')
    def downloadAllData() {

        params.query = "docType:project"
        def path = "search/downloadAllData"

        if (params.view == 'xlsx' || params.view == 'json') {
            path += ".${params.view}"
        }else{
            path += ".json"
        }

        def facets = []
        facets.addAll(params.getList("fq"))
        facets << "className:au.org.ala.ecodata.Project"
        params.put("fq", facets)
        params.put("downloadUrl", g.createLink(controller:'document', action:'downloadProjectDataFile', absolute: true)+'/')
        params.put("systemEmail", grailsApplication.config.fieldcapture.system.email.address)
        params.put("senderEmail", grailsApplication.config.fieldcapture.system.email.address)
        searchService.addDefaultFacetQuery(params)
        def url = grailsApplication.config.ecodata.baseUrl + path +  commonService.buildUrlParamsFromMap(params)
        def response = webService.doPostWithParams(url, [:]) // POST because the URL can get long.

        render response as JSON
    }

    @PreAuthorise(accessLevel = 'siteAdmin', redirectController ='home', redirectAction = 'index')
    def downloadOrganisationData() {

        params.query = "docType:organisation"
        def path = "search/downloadOrganisationData"

        def facets = []
        facets.addAll(params.getList("fq"))
        facets << "className:au.org.ala.ecodata.Organisation"
        params.put("fq", facets)
        params.put("downloadUrl", g.createLink(controller:'document', action:'downloadProjectDataFile', absolute: true)+'/')
        params.put("systemEmail", grailsApplication.config.fieldcapture.system.email.address)
        params.put("senderEmail", grailsApplication.config.fieldcapture.system.email.address)
        searchService.addDefaultFacetQuery(params)
        def url = grailsApplication.config.ecodata.baseUrl + path +  commonService.buildUrlParamsFromMap(params)
        def response = webService.doPostWithParams(url, [:]) // POST because the URL can get long.

        render response as JSON
    }



    @PreAuthorise(accessLevel = 'siteAdmin', redirectController ='home', redirectAction = 'index')
    def downloadSummaryData() {
        params.query = "docType:project"
        def path = "search/downloadSummaryData"

        if (params.view == 'xlsx' || params.view == 'json') {
            path += ".${params.view}"
        }else{
            path += ".json"
        }

        searchService.addDefaultFacetQuery(params)
        def url = grailsApplication.config.ecodata.baseUrl + path + commonService.buildUrlParamsFromMap(params)
        webService.proxyGetRequest(response, url, true, true,960000)
    }

    @PreAuthorise(accessLevel = 'siteAdmin', redirectController ='home', redirectAction = 'index')
    def downloadShapefile() {
        params.query = "docType:project"
        def path = "search/downloadShapefile"

        searchService.addDefaultFacetQuery(params)
        def url = grailsApplication.config.ecodata.baseUrl + path + commonService.buildUrlParamsFromMap(params)
        def resp = webService.proxyGetRequest(response, url, true, true,960000)
        if (resp.status != 200) {
            render view:'/error', model:[error:resp.error]
        }
    }

    Map findPotentialHomePageImages() {
        Integer max = params.max as Integer
        Integer offset = params.offset as Integer

        Map result = reportService.findPotentialHomePageImages(max, offset)
        result.documents = result.documents?.collect{it + [ref:g.createLink(controller: 'project', action:'index', id:it.projectId)]}
        render result as JSON
    }

    def findHomePageNominatedProjects() {
        Integer max = params.max as Integer
        Integer offset = params.offset as Integer
        def projects = reportService.findHomePageNominatedProjects(max, offset)

        projects
    }

}
