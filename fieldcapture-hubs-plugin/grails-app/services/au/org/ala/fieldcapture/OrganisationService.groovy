package au.org.ala.fieldcapture

import org.codehaus.groovy.grails.web.json.JSONArray


class OrganisationService {

    def grailsApplication, webService, projectService, userService


    def get(String id, view = '') {

        def url = "${grailsApplication.config.ecodata.baseUrl}organisation/$id?view=$view"
        def organisation = webService.getJson(url)


        organisation.projects = getProjectsByName(organisation)

        organisation
    }

    private def getProjectsByName(organisation) {
        def projects = new JSONArray()
        if (!organisation) {
            return projects
        }

        def resp = projectService.search([serviceProviderName:organisation.name, view:'enhanced'])

        if (resp?.resp?.projects) {
            projects.addAll(resp.resp.projects)
        }

        resp = projectService.search([organisationName:organisation.name, view:'enhanced'])

        if (resp?.resp?.projects) {
            projects.addAll(resp.resp.projects.findAll{it.serviceProviderName != organisation.name}) // Exclude duplicates.
        }
        projects
    }

    def list() {
        def url = "${grailsApplication.config.ecodata.baseUrl}organisation/"
        webService.getJson(url)
    }

    def update(id, organisation) {

        def url = "${grailsApplication.config.ecodata.baseUrl}organisation/$id"
        webService.doPost(url, organisation)

    }

    def isUserAdminForOrganisation(organisationId) {
        def userIsAdmin

        if (!userService.user) {
            return false
        }
        if (userService.userIsSiteAdmin()) {
            userIsAdmin = true
        } else {
            userIsAdmin = userService.isUserAdminForOrganisation(userService.user.userId, organisationId)
        }

        userIsAdmin
    }

    def isUserGrantManagerForOrganisation(organisationId) {
        def userIsAdmin

        if (!userService.user) {
            return false
        }
        if (userService.userIsSiteAdmin()) {
            userIsAdmin = true
        } else {
            userIsAdmin = userService.isUserGrantManagerForOrganisation(userService.user.userId, organisationId)
        }

        userIsAdmin
    }

    /**
     * Get the list of users (members) who have any level of permission for the requested organisationId
     *
     * @param organisationId the organisationId of interest.
     */
    def getMembersOfOrganisation(organisationId) {
        def url = grailsApplication.config.ecodata.baseUrl + "permissions/getMembersForOrganisation/${organisationId}"
        webService.getJson(url)
    }

    /**
     * Adds a user with the supplied role to the identified organisation.
     * Adds the same user with the same role to all of the organisation's projects.
     *
     * @param userId the id of the user to add permissions for.
     * @param organisationId the organisation to add permissions for.
     * @param role the role to assign to the user.
     */
    def addUserAsRoleToOrganisation(String userId, String organisationId, String role) {

        def organisation = get(organisationId, 'flat')
        userService.addUserAsRoleToOrganisation(userId, organisationId, role)
        organisation.projects.each {
            userService.addUserAsRoleToProject(userId, it.projectId, role)
        }
    }

    /**
     * Removes the user access with the supplied role from the identified organisation.
     * Removes the same user from all of the organisation's projects.
     *
     * @param userId the id of the user to remove permissions for.
     * @param organisationId the organisation to remove permissions for.

     */
    def removeUserWithRoleFromOrganisation(String userId, String organisationId, String role) {
        def organisation = get(organisationId, 'flat')
        userService.removeUserWithRoleFromOrganisation(userId, organisationId, role)
        organisation.projects.each {
            userService.removeUserWithRole(it.projectId, userId, role)
        }
    }

}
