package au.org.ala.fieldcapture
import grails.converters.JSON
import org.joda.time.DateTime

class ProjectController {

    def projectService, metadataService, organisationService, commonService, activityService, userService, webService, roleService, grailsApplication
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
                user.metaClass.isAdmin = projectService.isUserAdminForProject(user.userId, id)?:false
                user.metaClass.isCaseManager = projectService.isUserCaseManagerForProject(user.userId, id)?:false
                user.metaClass.isEditor = projectService.canUserEditProject(user.userId, id)?:false
                user.metaClass.hasViewAccess = projectService.canUserViewProject(user.userId, id)?:false
            }
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
             organisations: organisationService.list().list,
             programs: projectService.programsModel(),
             today:DateUtils.format(new DateTime()),
             themes:metadataService.getThemesForProject(project)
            ]

            render view:projectView(project), model:model
        }
    }

    private String projectView(project) {
        if (project.isExternal) {
            if (project.isCitizenScience) {
                return 'externalCitizenScienceProjectTemplate'
            }
        }
        return project.projectType == 'survey'?'citizenScienceProjectTemplate':'index'
    }

    @PreAuthorise
    def edit(String id) {
        def project = projectService.get(id, 'all')

        def organisations = organisationService.list()
        if (project) {
            def siteInfo = siteService.getRaw(project.projectSiteId)
            [project: project,
             siteDocuments: siteInfo.documents?:'[]',
             site: siteInfo.site,
             organisations: organisations.list,
             programs: metadataService.programsModel()]
        } else {
            forward(action: 'list', model: [error: 'no such id'])
        }
    }

    def create() {
        boolean allowAdHocOrgNameOnCreate = true; // set true for MERIT
        if (!allowAdHocOrgNameOnCreate && !params.organisationId)
            render status: 400, text: 'Either an organisation id or ad hoc organisation name must be active'

        [
                citizenScience: params.citizenScience,
                organisationId: params.organisationId,
                allowAdHocOrgNameOnCreate: allowAdHocOrgNameOnCreate,
                siteDocuments: '[]',
                organisations: organisationService.list().list,
                programs: projectService.programsModel(),
                activityTypes: metadataService.activityTypesList()
        ]
    }

    def citizenScience() {
        def user = userService.getUser()
        def userId = user?.userId
        [user: user,
         projects: projectService.list(false, true).collect {
             def imgUrl;
             it.documents.each { doc ->
                 if (doc.isPrimaryProjectImage) imgUrl = doc.url
             }
             // pass array instead of object to reduce size
             [it.projectId,
              it.coverage ?: '',
              it.description,
              userId && projectService.canUserEditProject(userId, it.projectId) ? 'y' : '',
              it.name,
              it.organisationName?:metadataService.getOrganisationName(it.organisationId),
              it.status,
              it.urlAndroid,
              it.urlITunes,
              it.urlWeb,
              imgUrl,
              it.organisationId
             ]
         }];
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
