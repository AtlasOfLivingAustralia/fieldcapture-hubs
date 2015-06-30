package au.org.ala.fieldcapture
import grails.converters.JSON
import org.joda.time.DateTime

class ProjectController {

    def projectService, metadataService, organisationService, commonService, activityService, userService, webService, roleService, grailsApplication, projectActivityService
    def siteService, documentService
    static defaultAction = "index"
    static ignore = ['action','controller','id']

    def index(String id) {
        def project = projectService.get(id, 'brief')
        def roles = roleService.getRoles()

        if (!project || project.error) {
            flash.message = "Project not found with id: ${id}"
            if (project?.error) {
                flash.message += "<br/>${project.error}"
                log.warn project.error
            }
            redirect(controller: 'home', model: [error: flash.message])
        } else {
            project.sites?.sort {it.name}
            def user = userService.getUser()
            def members = projectService.getMembersForProjectId(id)
            def admins = members.findAll{ it.role == "admin" }.collect{ it.userName }.join(",") // comma separated list of user email addresses

            if (user) {
                user = user.properties
                user.isAdmin = projectService.isUserAdminForProject(user.userId, id)?:false
                user.isCaseManager = projectService.isUserCaseManagerForProject(user.userId, id)?:false
                user.isEditor = projectService.canUserEditProject(user.userId, id)?:false
                user.hasViewAccess = projectService.canUserViewProject(user.userId, id)?:false
            }
            def programs = projectService.programsModel()
            def content = projectContent(project, user, programs)
            def model = [project: project,
                activities: activityService.activitiesForProject(id),
                mapFeatures: commonService.getMapFeatures(project),
                isProjectStarredByUser: userService.isProjectStarredByUser(user?.userId?:"0", project.projectId)?.isProjectStarredByUser,
                user: user,
                roles: roles,
                admins: admins,
                activityTypes: projectService.activityTypesList(),
                metrics: projectService.summary(id),
                outputTargetMetadata: metadataService.getOutputTargetsByOutputByActivity(),
                organisations: metadataService.organisationList().list,
                programs: programs,
                today:DateUtils.format(new DateTime()),
                themes:metadataService.getThemesForProject(project),
                projectContent:content.model
            ]

            if(project.projectType == 'survey'){
                def pActivityForms = []
                def activityModel = metadataService.activitiesModel().activities.findAll { it.category == "Assessment & monitoring" }
                activityModel.collect{ pActivityForms.add([name: it.name, images: it.images]) }
                model.projectActivities = projectActivityService.getAllByProject(project.projectId)
                model.pActivityForms =  pActivityForms
            }

            render view:content.view, model:model
        }
    }

    protected Map projectContent(project, user, programs) {

        boolean isSurveyProject = (project.projectType == 'survey')
        def model = isSurveyProject?surveyProjectContent(project, user):worksProjectContent(project, user)
        [view:projectView(project), model:model]
    }

    protected String projectView(project) {
        if (project.isExternal) {
            return 'externalCitizenScienceProjectTemplate'
        }
        return project.projectType == 'survey'?'citizenscienceProjectTemplate':'index'
    }

    protected Map surveyProjectContent(project, user) {
        [about:[label:'About', template:'aboutCitizenScienceProject', visible: true, default: true, type:'tab'],
         news:[label:'News', visible: true, type:'tab'],
         documents:[label:'Documents', visible: !project.isExternal, type:'tab'],
         activities:[label:'Surveys', visible:!project.isExternal, disabled:!user?.hasViewAccess, wordForActivity:'Survey',type:'tab'],
         site:[label:'Locations', visible: !project.isExternal, disabled:!user?.hasViewAccess, wordForSite:'Location', editable:user?.isEditor == true, type:'tab'],
         admin:[label:'Admin', visible:(user?.isAdmin || user?.isCaseManager), type:'tab']]
    }

    protected Map worksProjectContent(project, user) {
        [overview:[label:'Overview', visible: true, default: true, type:'tab'],
         documents:[label:'Documents', visible: !project.isExternal, type:'tab'],
         activities:[label:'Activities', visible:!project.isExternal, disabled:!user?.hasViewAccess, wordForActivity:"Activity",type:'tab'],
         site:[label:'Sites', visible: !project.isExternal, disabled:!user?.hasViewAccess, wordForSite:'Site', editable:user?.isEditor == true, type:'tab'],
         dashboard:[label:'Dashboard', visible: !project.isExternal, disabled:!user?.hasViewAccess, type:'tab'],
         admin:[label:'Admin', visible:(user?.isAdmin || user?.isCaseManager), type:'tab']]

    }

    @PreAuthorise
    def edit(String id) {

        def project = projectService.get(id, 'all')
        // This will happen if we are returning from the organisation create page during an edit workflow.
        if (params.organisationId) {
            project.organisationId = params.organisationId
        }
        def user = userService.getUser()
        def groupedOrganisations = groupOrganisationsForUser(user.userId)

        if (project) {
            def siteInfo = siteService.getRaw(project.projectSiteId)
            [project: project,
             siteDocuments: siteInfo.documents?:'[]',
             site: siteInfo.site,
             userOrganisations: groupedOrganisations.user ?: [],
             organisations: groupedOrganisations.other ?: [],
             programs: metadataService.programsModel()]
        } else {
            forward(action: 'list', model: [error: 'no such id'])
        }
    }

    def create() {
        def user = userService.getUser()
        if (!user) {
            flash.message = "You do not have permission to perform that operation"
            redirect controller: 'home', action: 'index'
            return
        }
        def groupedOrganisations = groupOrganisationsForUser(user.userId)
        // Prepopulate the project as appropriate.
        def project = [:]
        if (params.organisationId) {
            project.organisationId = params.organisationId
        }
        if (params.citizenScience) {
            project.isCitizenScience = true
            project.projectType = 'survey'
        }
        // Default the project organisation if the user is a member of a single organisation.
        if (groupedOrganisations.user?.size() == 1) {
            project.organisationId = groupedOrganisations.user[0].organisationId
        }
        [
                organisationId: params.organisationId,
                siteDocuments: '[]',
                userOrganisations: groupedOrganisations.user ?: [],
                organisations: groupedOrganisations.other ?: [],
                programs: projectService.programsModel(),
                project:project
        ]
    }

    /**
     * Splits the list of organisations into two - one containing organisations that the user is a member of,
     * the other containing the rest.
     * @param the user id to use for the grouping.
     * @return [user:[], other:[]]
     */
    private Map groupOrganisationsForUser(userId) {

        def organisations = metadataService.organisationList().list ?: []
        def userOrgIds = userService.getOrganisationIdsForUserId(userId)

        organisations.groupBy{organisation -> organisation.organisationId in userOrgIds ? "user" : "other"}
    }

    def citizenScience() {
        def today = DateUtils.now()
        def user = userService.getUser()
        def userId = user?.userId
        def projects = projectService.list(false, true).collect {
            def urlImage
            it.documents.each { doc ->
                if (doc.role == documentService.ROLE_LOGO)
                    urlImage = doc.url
                else if (!urlImage && doc.isPrimaryProjectImage)
                    urlImage = doc.url
            }
            // no need to ship the whole link object down to browser
            def trimmedLinks = it.links.collect {
                [
                    role: it.role,
                    url: it.url
                ]
            }
            def startDate = it.plannedStartDate? DateUtils.parse(it.plannedStartDate): null
            def endDate = it.plannedEndDate? DateUtils.parse(it.plannedEndDate): null
            [
                projectId  : it.projectId,
                aim        : it.aim,
                coverage   : it.coverage ?: '',
                daysRemaining: endDate? DateUtils.daysRemaining(today, endDate): -1,
                daysSince: startDate? DateUtils.daysRemaining(startDate, today): -1,
                daysTotal  : startDate && endDate? DateUtils.daysRemaining(startDate, endDate): -1,
                difficulty : it.difficulty,
                hasTeachingMaterials: it.hasTeachingMaterials,
                isDIY      : it.isDIY && true, // force it to boolean
                isEditable : userId && projectService.canUserEditProject(userId, it.projectId),
                isExternal : it.isExternal && true, // force it to boolean
                isNoCost   : !it.hasParticipantCost,
                isSuitableForChildren: it.isSuitableForChildren,
                links      : trimmedLinks,
                name       : it.name,
                organisationId  : it.organisationId,
                organisationName: it.organisationName ?: organisationService.getNameFromId(it.organisationId),
                status     : it.status,
                urlImage   : urlImage,
                urlWeb     : it.urlWeb
            ]
        }
        if (params.download as boolean) {
            response.setHeader("Content-Disposition","attachment; filename=\"projects.json\"");
            // This is returned to the browswer as a text response due to workaround the warning
            // displayed by IE8/9 when JSON is returned from an iframe submit.
            response.setContentType('text/plain;charset=UTF8')
            def resultJson = projects as JSON
            render resultJson.toString()
        } else {
            [
                user: user,
                showTag: params.tag,
                projects: projects.collect {
                    [ // pass array instead of object to reduce JSON size
                      it.projectId,
                      it.aim,
                      it.coverage,
                      it.daysRemaining,
                      it.daysSince,
                      it.daysTotal,
                      it.difficulty,
                      it.hasTeachingMaterials,
                      it.isDIY,
                      it.isEditable,
                      it.isExternal,
                      it.isNoCost,
                      it.isSuitableForChildren,
                      it.links,
                      it.name,
                      it.organisationId,
                      it.organisationName,
                      it.status,
                      it.urlImage,
                      it.urlWeb
                    ]
                }
            ]
        }
    }

    /**
     * Updates existing or creates new output.
     *
     * If id is blank, a new project will be created
     *
     * @param id projectId
     * @return
     */
    def ajaxUpdate(String id) {
        def postBody = request.JSON
        log.debug "Body: ${postBody}"
        log.debug "Params: ${params}"
        def values = [:]
        // filter params to remove keys in the ignore list
        postBody.each { k, v ->
            if (!(k in ignore)) {
                values[k] = v
            }
        }

        // The rule currently is that anyone is allowed to create a project so we only do these checks for
        // existing projects.
        def userId = userService.getUser()?.userId
        if (id) {
            if (!projectService.canUserEditProject(userId, id)) {
                render status:401, text: "User ${userId} does not have edit permissions for project ${id}"
                log.debug "user not caseManager"
                return
            }

            if (values.containsKey("planStatus") && values.planStatus =~ /approved/) {
                // check to see if user has caseManager permissions
                if (!projectService.isUserCaseManagerForProject(userId, id)) {
                    render status:401, text: "User does not have caseManager permissions for project"
                    log.warn "User ${userId} who is not a caseManager attempting to change planStatus for project ${id}"
                    return
                }
            }
        } else if (!userId) {
            render status: 401, text: 'You do not have permission to create a project'
        }


        log.debug "json=" + (values as JSON).toString()
        log.debug "id=${id} class=${id?.getClass()}"
        def projectSite = values.remove("projectSite")
        def documents = values.remove('documents')
        def links = values.remove('links')
        def result = id? projectService.update(id, values): projectService.create(values)
        log.debug "result is " + result
        if (documents && !result.error) {
            if (!id) id = result.resp.projectId
            documents.each { doc ->
                doc.projectId = id
                doc.isPrimaryProjectImage = doc.role == 'mainImage'
                if (doc.isPrimaryProjectImage) doc.public = true
                documentService.saveStagedImageDocument(doc)
            }
        }
        if (links && !result.error) {
            if (!id) id = result.resp.projectId
            links.each { link ->
                link.projectId = id
                documentService.saveLink(link)
            }
        }
        if (projectSite && !result.error) {
            if (!id) id = result.resp.projectId
            if (!projectSite.projects)
                projectSite.projects = [id]
            else if (!projectSite.projects.contains(id))
                projectSite.projects += id
            def siteResult = siteService.updateRaw(values.projectSiteId, projectSite)
            if (siteResult.status == 'error')
                result = [error:'SiteService failed']
            else if (siteResult.status == 'created') {
                def updateResult = projectService.update(id, [projectSiteId: siteResult.id])
                if (updateResult.error) result = updateResult
            }
        }
        if (result.error) {
            render result as JSON
        } else {
            //println "json result is " + (result as JSON)
            render result.resp as JSON
        }
    }




    @PreAuthorise
    def update(String id) {
        //params.each { println it }
        projectService.update(id, params)
        chain action: 'index', id: id
    }

    @PreAuthorise(accessLevel = 'admin')
    def delete(String id) {
        projectService.delete(id)
        forward(controller: 'home')
    }

    def list() {
        // will show a list of projects
        // but for now just go home
        forward(controller: 'home')
    }

    def species(String id) {
        def project = projectService.get(id, 'brief')
        def activityTypes = metadataService.activityTypesList();
        render view:'/species/select', model: [project:project, activityTypes:activityTypes]
    }

    /**
     * Star or unstar a project for a user
     * Action is determined by the URI endpoint, either: /add | /remove
     *
     * @return
     */
    def starProject() {
        String act = params.id?.toLowerCase() // rest path starProject/add or starProject/remove
        String userId = params.userId
        String projectId = params.projectId

        if (act && userId && projectId) {
            if (act == "add") {
                render userService.addStarProjectForUser(userId, projectId) as JSON
            } else if (act == "remove") {
                render userService.removeStarProjectForUser(userId, projectId) as JSON
            } else {
                render status:400, text: 'Required endpoint (path) must be one of: add | remove'
            }
        } else {
            render status:400, text: 'Required params not provided: userId, projectId'
        }
    }

    def getMembersForProjectId() {
        String projectId = params.id
        def adminUserId = userService.getCurrentUserId()

        if (projectId && adminUserId) {
            if (projectService.isUserAdminForProject(adminUserId, projectId) || projectService.isUserCaseManagerForProject(adminUserId, projectId)) {
                render projectService.getMembersForProjectId(projectId) as JSON
            } else {
                render status:403, text: 'Permission denied'
            }
        } else if (adminUserId) {
            render status:400, text: 'Required params not provided: id'
        } else if (projectId) {
            render status:403, text: 'User not logged-in or does not have permission'
        } else {
            render status:500, text: 'Unexpected error'
        }
    }

    @PreAuthorise(accessLevel = 'siteAdmin', redirectController ='home', redirectAction = 'index')
    def downloadProjectData() {
        String projectId = params.id

        if (!projectId) {
            render status:400, text: 'Required params not provided: id'
        }
        else{
            def path = "project/downloadProjectData/${projectId}"

            if (params.view == 'xlsx' || params.view == 'json') {
                path += ".${params.view}"
            }else{
                path += ".json"
            }
            def url = grailsApplication.config.ecodata.baseUrl + path
            webService.proxyGetRequest(response, url, true, true,120000)
        }
    }
}
