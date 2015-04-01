package au.org.ala.fieldcapture.hub

/**
 * The configuration for a hub.
 */
class HubSettings {

    /** Identifies this hub - should be a short string as it is used in the URL path */
    String id

    /** URL pointing to an image that will displayed as a background image header block of all pages in the hub */
    def bannerUrl

    /** URL pointing to an image that will displayed as a logo in the top left of the header block of all pages in the hub */
    def logoUrl

    /** The title of the hub - currently displayed on the home page */
    String title

    /** The (ordered) list of facets that will be displayed on the home and search pages */
    List<String> availableFacets

    /** Admin only facets */
    List<String> adminFacets

    /** All searches made in this hub will automatically include this (facet) query.  Should be of the form <facetName>:<value> */
    List<String> defaultFacetQuery

    /** Projects created within this hub will only be able to select from the programs configured here */
    List<String> supportedPrograms

    /** Home page map facets */
    List<String> availableMapFacets

    String skin = "ala2"

    /**
     * Allows the property to be set using a JSONArray which has an implementation of join which is
     * incompatible with how this is used by the SearchService.
     */
    def setAvailableFacets(List<String> facets) {
        this.availableFacets = new ArrayList<String>(facets)
    }
    def setAvailableMapFacets(List<String> facets) {
        this.availableMapFacets = new ArrayList<String>(facets)
    }

    def setSupportedPrograms(List<String> supportedPrograms) {
        this.supportedPrograms = new ArrayList<String>(supportedPrograms)
    }



}
