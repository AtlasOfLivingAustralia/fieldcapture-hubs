package au.org.ala.fieldcapture

class ProjectService {

    def webService, grailsApplication, siteService, activityService, authService, emailService, documentService, userService, metadataService, settingService

    private def mapAttributesToCollectory(props) {
        def mapKeyEcoDataToCollectory = [
            description: 'pubDescription',
            manager: 'email',
            name: 'name',
            dataSharingLicense: '', // ignore this property (mapped to dataResource)
            organisation: '', // ignore this property
            projectId: 'uid',
            urlWeb: 'websiteUrl'
        ]
        def collectoryProps = [
            api_key: grailsApplication.config.api_key
        ]
        def hiddenJSON = [:]
        props.each { k, v ->
            if (v != null) {
                def keyCollectory = mapKeyEcoDataToCollectory[k]
                if (keyCollectory == null) // not mapped to first class collectory property
                    hiddenJSON[k] = v
                else if (keyCollectory != '') // not to be ignored
                    collectoryProps[keyCollectory] = v
            }
        }
        collectoryProps.hiddenJSON = hiddenJSON
        println("collectory hiddenJSON = " + hiddenJSON)
        collectoryProps
    }

    def list(brief = false, citizenScienceOnly = false) {
        def params = brief ? '?brief=true' : ''
        if (citizenScienceOnly) params += (brief ? '&' : '?') + 'citizenScienceOnly=true'
        def resp = webService.getJson(grailsApplication.config.ecodata.baseUrl + 'project/' + params)
        resp.list
    }

    def get(id, levelOfDetail = "", includeDeleted = false) {

        def params = '?'

        params += levelOfDetail ? "view=${levelOfDetail}&" : ''
        params += "includeDeleted=${includeDeleted}"
        webService.getJson(grailsApplication.config.ecodata.baseUrl + 'project/' + id + params)
    }

    def getRich(id) {
        get(id, 'rich')
    }

    def getActivities(project) {
        def list = []
        project.sites.each { site ->
            siteService.get(site.siteId)?.activities?.each { act ->
                list << activityService.constructName(act)
            }
        }
        list
    }

    /**
     * Creates a new project and adds the user as a project admin.
     */
    def create(props) {

        def activities = props.remove('selectedActivities')

        def collectoryProps = mapAttributesToCollectory(props)
        def result = webService.doPost(grailsApplication.config.collectory.baseURL + 'ws/dataProvider/', collectoryProps)
        def dataProviderId = extractCollectoryIdFromHttpHeaders(result?.headers)
        if (dataProviderId) {
            props.dataProviderId = dataProviderId
            collectoryProps.remove('hiddenJSON')
            collectoryProps.dataProvider = [uid: dataProviderId]
            if (props.organisationId) {
                collectoryProps.institution = [uid: props.organisationId]
            }
            collectoryProps.licenseType = props.dataSharingLicense
            result = webService.doPost(grailsApplication.config.collectory.baseURL + 'ws/dataResource/', collectoryProps)
            props.dataResourceId = extractCollectoryIdFromHttpHeaders(result?.headers)
        }

        result = webService.doPost(grailsApplication.config.ecodata.baseUrl + 'project/', props)
        if (result?.resp?.projectId) {
            def projectId = result.resp.projectId
            // Add the user who created the project as an admin of the project
            userService.addUserAsRoleToProject(userService.getUser().userId, projectId, RoleService.PROJECT_ADMIN_ROLE)
            if (activities) {
                settingService.updateProjectSettings(projectId, [allowedActivities: activities])
            }
        }

        result
    }

    private def extractCollectoryIdFromHttpHeaders(headers) {
        return headers?.location?.first().toString().tokenize('/').last()
    }

    def update(id, body) {
        webService.doPost(grailsApplication.config.ecodata.baseUrl + 'project/' + id, body)
        // recreate 'hiddenJSON' in collectory every time (minus some attributes)
        body = getRich(id) as Map
        ['id','dateCreated','documents','lastUpdated','organisationName','projectId','sites'].each { body.remove(it) }
        webService.doPost(grailsApplication.config.collectory.baseURL + 'ws/dataProvider/' + body.dataProviderId, mapAttributesToCollectory(body))
        webService.doPost(grailsApplication.config.collectory.baseURL + 'ws/dataResource/' + body.dataResourceId, [licenseType:body.dataSharingLicense])
    }

    /**
     * This does a 'soft' delete. The record is marked as inactive but not removed from the DB.
     * @param id the record to delete
     * @return the returned status
     */
    def delete(id) {
        webService.doDelete(grailsApplication.config.ecodata.baseUrl + 'project/' + id)
    }

    /**
     * This does a 'hard' delete. The record is removed from the DB.
     * @param id the record to destroy
     * @return the returned status
     */
    def destroy(id) {
        webService.doDelete(grailsApplication.config.ecodata.baseUrl + 'project/' + id + '?destroy=true')
        webService.doDelete(grailsApplication.config.collectory.baseURL + 'ws/dataProvider/' + id)
    }

    /**
     * Retrieves a summary of project metrics (including planned output targets)
     * and groups them by output type.
     * @param id the id of the project to get summary information for.
     * @return TODO document this structure.
     */
    def summary(String id) {
        def scores = webService.getJson(grailsApplication.config.ecodata.baseUrl + 'project/projectMetrics/' + id)

        def scoresWithTargetsByOutput = [:]
        def scoresWithoutTargetsByOutputs = [:]
        if (scores && scores instanceof List) {  // If there was an error, it would be returning a map containing the error.
            // There are some targets that have been saved as Strings instead of numbers.
            scoresWithTargetsByOutput = scores.grep{ it.target && it.target != "0" }.groupBy { it.score.outputName }
            scoresWithoutTargetsByOutputs = scores.grep{ it.results && (!it.target || it.target == "0") }.groupBy { it.score.outputName }
        }
        [targets:scoresWithTargetsByOutput, other:scoresWithoutTargetsByOutputs]
    }

    def search(params) {
        webService.doPost(grailsApplication.config.ecodata.baseUrl + 'project/search', params)
    }

    /**
     * Get the list of users (members) who have any level of permission for the requested projectId
     *
     * @param projectId
     * @return
     */
    def getMembersForProjectId(projectId) {
        def url = grailsApplication.config.ecodata.baseUrl + "permissions/getMembersForProject/${projectId}"
        webService.getJson(url)
    }

    /**
     * Does the current user have permission to administer the requested projectId?
     * Checks for the ADMIN role in CAS and then checks the UserPermission
     * lookup in ecodata.
     *
     * @param userId
     * @param projectId
     * @return boolean
     */
    def isUserAdminForProject(userId, projectId) {
        def userIsAdmin

        if (userService.userIsSiteAdmin()) {
            userIsAdmin = true
        } else {
            def url = grailsApplication.config.ecodata.baseUrl + "permissions/isUserAdminForProject?projectId=${projectId}&userId=${userId}"
            userIsAdmin = webService.getJson(url)?.userIsAdmin  // either will be true or false
        }

        userIsAdmin
    }

    /**
     * Does the current user have caseManager permission for the requested projectId?
     *
     * @param userId
     * @param projectId
     * @return
     */
    def isUserCaseManagerForProject(userId, projectId) {
        def url = grailsApplication.config.ecodata.baseUrl + "permissions/isUserCaseManagerForProject?projectId=${projectId}&userId=${userId}"
        webService.getJson(url)?.userIsCaseManager // either will be true or false
    }

    /**
     * Does the current user have permission to edit the requested projectId?
     * Checks for the ADMIN role in CAS and then checks the UserPermission
     * lookup in ecodata.
     *
     * @param userId
     * @param projectId
     * @return boolean
     */
    def canUserEditProject(userId, projectId) {
        def userCanEdit

        if (userService.userIsSiteAdmin()) {
            userCanEdit = true
        } else {
            def url = grailsApplication.config.ecodata.baseUrl + "permissions/canUserEditProject?projectId=${projectId}&userId=${userId}"
            userCanEdit = webService.getJson(url)?.userIsEditor?:false

        }

        // Merit projects are not allowed to be edited.
        if (userCanEdit) {
            def project = get(projectId, 'brief')
            def program = metadataService.programModel(project.associatedProgram)
            userCanEdit = !program?.isMeritProgramme
        }

        userCanEdit
    }

    /**
     * Does the current user have permission to view details of the requested projectId?
     * @param userId the user to test.
     * @param the project to test.
     */
    def canUserViewProject(userId, projectId) {

        def userCanView
        if (userService.userIsSiteAdmin() || userService.userHasReadOnlyAccess()) {
            userCanView = true
        }
        else {
            userCanView = canUserEditProject(userId, projectId)
        }
        userCanView
    }

    /**
     * Returns the programs model for use by a particular project.  At the moment, this method just delegates to the metadataservice,
     * however a per organisation programs model is something being discussed.
     */
    def programsModel() {
        metadataService.programsModel()
    }

    /**
     * Returns a filtered list of activities for use by a project
     */
    public List activityTypesList(String projectId) {
        def projectSettings = settingService.getProjectSettings(projectId)
        def activityTypes = metadataService.activityTypesList()

        def allowedActivities = activityTypes
        if (projectSettings?.allowedActivities) {

            allowedActivities = []
            activityTypes.each { category ->
                def matchingActivities = []
                category.list.each { nameAndDescription ->
                    if (nameAndDescription.name in projectSettings.allowedActivities) {
                        matchingActivities << nameAndDescription
                    }
                }
                if (matchingActivities) {
                    allowedActivities << [name:category.name, list:matchingActivities]
                }
            }

        }

        allowedActivities

    }
}
