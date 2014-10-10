package au.org.ala.fieldcapture

class MetadataService {

    def grailsApplication, webService, cacheService

    def activitiesModel() {
        return cacheService.get('activity-model',{
            webService.getJson(grailsApplication.config.ecodata.baseUrl +
                'metadata/activitiesModel')
        })
    }

    def annotatedOutputDataModel(type) {
        return cacheService.get('annotated-output-model'+type,{
            webService.getJson(grailsApplication.config.ecodata.baseUrl +
                    'metadata/annotatedOutputDataModel?type='+type.encodeAsURL())
        })
    }

    def updateActivitiesModel(model) {
        def result = webService.doPost(grailsApplication.config.ecodata.baseUrl +
                'metadata/updateActivitiesModel', [model: model])
        cacheService.clear('activity-model')
        result
    }

    def programsModel() {
        return cacheService.get('programs-model',{
            webService.getJson(grailsApplication.config.ecodata.baseUrl +
                'metadata/programsModel')
        })
    }

    def programModel(program) {
        return programsModel().programs.find {it.name == program}
    }

    def updateProgramsModel(model) {
        def result = webService.doPost(grailsApplication.config.ecodata.baseUrl +
                'metadata/updateProgramsModel', [model: model])
        cacheService.clear('programs-model')
        result
    }

    def getThemesForProject(project) {
        def programMD = programsModel().programs.find { it.name == project.associatedProgram }
        if (programMD) {
            def subprogramMD = programMD.subprograms.find {it.name == project.associatedSubProgram }
            if (subprogramMD) {
                return subprogramMD.themes
            }
        }
        return []
    }

    def getActivityModel(name) {
        return activitiesModel().activities.find { it.name == name }
    }

    def getMainScoresForActivity(name) {
        return activitiesModel().find({ it.name = name })?.outputs?.collect { it.scoreName }
    }

    def getDataModelFromOutputName(outputName) {
        def activityName = getActivityModelName(outputName)
        return activityName ? getDataModel(activityName) : null
    }

    def getDataModel(template) {
        return cacheService.get(template + '-model',{
            webService.getJson(grailsApplication.config.ecodata.baseUrl +
                    "metadata/dataModel/${template}")
        })
    }

    def updateOutputDataModel(model, template) {
        log.debug "updating template ${template}"
        //log.debug "model class is ${model.getClass()}"
        def result = webService.doPost(grailsApplication.config.ecodata.baseUrl +
                'metadata/updateOutputDataModel/' + template, [model: model])
        cacheService.clear(template + '-model')
        result
    }

    def getActivityModelName(outputName) {
        return activitiesModel().outputs.find({it.name == outputName})?.template
    }

    def getModelNameFromType(type) {
        log.debug "Getting model name for ${type}"
        log.debug activitiesModel()
        return activitiesModel().activities.find({it.name == type})?.template
    }

    def activityTypesList(program = '') {
        cacheService.get('activitiesSelectList'+program, {
            String url = grailsApplication.config.ecodata.baseUrl + 'metadata/activitiesList'
            if (program) {
                url += 'program='+program.encodeAsURL()
            }
            def activityTypes = webService.getJson(url)
            activityTypes.collect {key, value -> [name:key, list:value]}.sort{it.name}

        })
    }

    /**
     * Returns a Map with key: activityName and value: <list of score definitions for the outputs that make up the activity>
     * Used to support the nomination of project output targets for various activity types.
     */
    def getOutputTargetsByActivity() {
        def activityScores = [:]
        def activitiesModel = activitiesModel()

        activitiesModel.activities.each { activity ->
            def scores = []

            activityScores[activity.name] = scores
            activity.outputs.each { outputName ->
                def matchedOutput = activitiesModel.outputs.find {
                    output -> outputName == output.name
                }
                if (matchedOutput && matchedOutput.scores) {
                    matchedOutput.scores.each {
                        if (it.isOutputTarget) {
                            scores << (it << [outputName : outputName])
                        }
                    }
                }
            }
        }
        return activityScores
    }

    /**
     * Returns a 3 level hierarchy given by:
     *  a map keyed by activityName where each value is:
     *      a map keyed by outputName where each value is:
     *          a list of score definitions that are output targets
     * Only includes activities and outputs if they contain output targets.
     * Used to support the nomination of project output targets for various activity types.
     */
    def getOutputTargetsByOutputByActivity() {
        def outputTargetMetadata = [:]
        def activitiesModel = activitiesModel()

        activitiesModel.activities.each { activity ->
            def outputs = [:]

            activity.outputs.each { outputName ->
                def scores = []

                def matchedOutput = activitiesModel.outputs.find {
                    output -> outputName == output.name
                }
                if (matchedOutput && matchedOutput.scores) {
                    matchedOutput.scores.each {
                        if (it.isOutputTarget && (it.aggregationType == 'SUM' || it.aggregationType == 'AVERAGE')) {
                            scores << it
                        }
                    }
                    // only add the output if it has targets
                    if (scores) {
                        outputs[outputName] = scores
                    }
                }
            }
            // only add the activity if it has outputs that have targets
            if (outputs) {
                outputTargetMetadata[activity.name] = outputs
            }
        }
        return outputTargetMetadata
    }



    def clearEcodataCache() {
        webService.get(grailsApplication.config.ecodata.baseUrl + "admin/clearMetadataCache")
    }

    def outputTypesList() {
        outputTypes
    }

    def getInstitutionName(uid) {
        def institutions = institutionList()
        // The result of the service call will be a JSONArray if it's successful
        return uid ? institutions.find({ it.uid == uid })?.name : ''
    }

    def institutionList() {
        return cacheService.get('institutions',{
            webService.getJson(grailsApplication.config.collectory.baseURL + '/ws/institution')
        })
    }

    def getAccessLevels() {
        return cacheService.get('accessLevels',{
            webService.getJson(grailsApplication.config.ecodata.baseUrl +  "permissions/getAllAccessLevels")
        })
    }

    def getLocationMetadataForPoint(lat, lng) {
        cacheService.get("spatial-point-${lat}-${lng}", {
            webService.getJson(grailsApplication.config.ecodata.baseUrl + "metadata/getLocationMetadataForPoint?lat=${lat}&lng=${lng}")
        })
    }

    def getReportCategories() {
        return cacheService.get('report-categories',{
            def categories = new LinkedHashSet()
            activitiesModel().outputs.each { output ->
                output.scores.each { score ->
                    def cat = score.category?.trim()
                    if (cat) {
                        categories << cat
                    }
                }
            }
            categories
        })
    }

    def getGeographicFacetConfig() {
        cacheService.get("geographic-facets", {

            def results = [:].withDefault{[:]}

            def facetConfig = webService.getJson(grailsApplication.config.ecodata.baseUrl + "metadata/getGeographicFacetConfig")
            facetConfig.grouped.each { k, v ->
                v.each { name, fid ->
                    def objects = webService.getJson(grailsApplication.config.spatial.baseUrl + '/ws/objects/'+fid)
                    results[k] << [(objects[0].fieldname):objects[0]] // Using the fieldname instead of the name for grouped facets is a temp workaround for the GER.
                }

            }

            facetConfig.gridded.each { name, fid ->
                def objects = webService.getJson(grailsApplication.config.spatial.baseUrl + '/ws/objects/'+fid)
                objects.each {
                    results[name] << [(it.name):it]
                }
            }

            results
        })
    }


}
