package au.org.ala.fieldcapture

import au.org.ala.fieldcapture.hub.HubSettings
import grails.converters.JSON
import groovy.text.GStringTemplateEngine
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestAttributes

class SettingService {

    private static def ThreadLocal localHubConfig = new ThreadLocal()
    private static final String HUB_CONFIG_KEY_SUFFIX = '.hub.configuration'
    public static final String HUB_CONFIG_ATTRIBUTE_NAME = 'hubConfig'

    public static void setHubConfig(HubSettings hubSettings) {
        localHubConfig.set(hubSettings)
        GrailsWebRequest.lookup()?.setAttribute(HUB_CONFIG_ATTRIBUTE_NAME, hubSettings, RequestAttributes.SCOPE_REQUEST)
    }

    public static void clearHubConfig() {
        localHubConfig.remove()
        GrailsWebRequest.lookup()?.setAttribute(HUB_CONFIG_ATTRIBUTE_NAME, null, RequestAttributes.SCOPE_REQUEST)
    }

    public static HubSettings getHubConfig() {
        return localHubConfig.get()
    }

    def webService, cacheService
    def grailsApplication

    /**
     * Checks if there is a configuration defined for the specified hub.
     * Side effect:  if the supplied hub is invalid, the default hub will be loaded.  This is done as
     * the URL mappings validation is done in a servlet filter and if the validation returns false the
     * 404 page will be rendered without our hub filter being invoked.
     */
    boolean isValidHub(hub) {
        def result = (getHubSettings(hub) != null)
        if (!result) {
            loadHubConfig(null) // Load the default hub so the 404 page has access to theme information
        }
        result
    }

    def loadHubConfig(hub) {
        def settings = getHubSettings(hub)
        if (!settings) {
            log.warn("no settings returned!")
            settings = new HubSettings(
                    title:'Default',
                    skin:HubSettings.Skin.ALA2,
                    id:grailsApplication.config.app.default.hub?:'default',
                    availableFacets: ['status', 'organisationFacet','associatedProgramFacet','associatedSubProgramFacet','mainThemeFacet','stateFacet','nrmFacet','lgaFacet','mvgFacet','ibraFacet','imcra4_pbFacet','otherFacet', 'gerSubRegionFacet','electFacet'],
                    adminFacets: ['electFacet'],
                    availableMapFacets: ['status', 'organisationFacet','associatedProgramFacet','associatedSubProgramFacet','stateFacet','nrmFacet','lgaFacet','mvgFacet','ibraFacet','imcra4_pbFacet','electFacet']
            )
        }

        SettingService.setHubConfig(settings)


        // Lookup portal from database?  Or simply parse the URL?  Or access request?
//        def hubConfig = [
//                bannerUrl            : 'http://www.greateasternranges.org.au/templates/rt_chapelco/images/main/bg-header-mt.png',
//                logoUrl              : 'http://www.greateasternranges.org.au/wp-content/themes/ger/images/ger-logo-205x150px.png',
//                title                : 'Great Eastern Ranges',
//                settingsPageKeyPrefix: 'ger.',
//                availableFacets      : "organisationFacet,gerSubRegionFacet,associatedProgramFacet,mainThemeFacet,stateFacet,lgaFacet,mvgFacet",
//                defaultFacetQuery    : ['otherFacet:Great Eastern Ranges Initiative']
//        ]


    }

    def getSettingText(SettingPageType type) {
        def key = localHubConfig.get().id + type.key

        get(key)

    }

    def setSettingText(SettingPageType type, String content) {
        def key = localHubConfig.get().id + type.key

        set(key, content)
    }

    private def get(key) {
        String url = grailsApplication.config.ecodata.baseUrl + "setting/ajaxGetSettingTextForKey?key=${key}"
        def res = cacheService.get(key,{ webService.getJson(url) })
        return res?.settingText?:""
    }

    private def getJson(key) {
        cacheService.get(key, {
            def settings = get(key)
            return settings ? JSON.parse(settings) : [:]
        })
    }

    private def set(key, settings) {
        cacheService.clear(key)
        String url = grailsApplication.config.ecodata.baseUrl + "setting/ajaxSetSettingText/${key}"
        webService.doPost(url, [settingText: settings, key: key])
    }

    /**
     * Allows for basic GString style substitution into a Settings page.  If the saved template text includes
     * ${}, these will be substituted for values in the supplied model
     * @param type identifies the settings page to return.
     * @param substitutionModel values to substitute into the page.
     * @return the settings page after substitutions have been made.
     */
    def getSettingText(SettingPageType type, substitutionModel) {
        String templateText = getSettingText(type)
        GStringTemplateEngine templateEngine = new GStringTemplateEngine();
        return templateEngine.createTemplate(templateText).make(substitutionModel).toString()
    }

    private def projectSettingsKey(projectId) {
        return projectId+'.settings'
    }

    def getProjectSettings(projectId) {
        getJson(projectSettingsKey(projectId))
    }

    def updateProjectSettings(projectId, settings) {
        def key = projectSettingsKey(projectId)
        set(key, (settings as JSON).toString())
    }

    private def hubSettingsKey(hub) {
        if (!hub) {
            throw new IllegalArgumentException("the hub parameter must not be null")
        }
        return hub+HUB_CONFIG_KEY_SUFFIX
    }

    HubSettings getHubSettings(hub) {
        if (!hub) { hub = grailsApplication.config.app.default.hub?:'default' }
        def json = getJson(hubSettingsKey(hub))

        json.id ? new HubSettings(new HashMap(json)) : null
    }

    def updateHubSettings(HubSettings settings) {
        set(hubSettingsKey(settings.id), (settings as JSON).toString())
    }

}
