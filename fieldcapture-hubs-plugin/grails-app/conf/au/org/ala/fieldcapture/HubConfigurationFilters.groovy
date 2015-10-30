package au.org.ala.fieldcapture

class HubConfigurationFilters {

    def settingService

    def filters = {
        all(controller: '*', action: '*') {
            before = {
                request.containerType = 'container' // Default to fixed width for most pages.
                settingService.loadHubConfig(params.hub)
            }
            after = { Map model ->

            }
            afterView = { Exception e ->
                // The settings are cleared in a servlet filter so they are available during page rendering.
            }
        }
    }
}
