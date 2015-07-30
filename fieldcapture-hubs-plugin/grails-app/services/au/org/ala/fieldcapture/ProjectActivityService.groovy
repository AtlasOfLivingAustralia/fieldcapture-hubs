package au.org.ala.fieldcapture

import grails.transaction.Transactional


class ProjectActivityService {

    def webService, grailsApplication

    def getAllByProject(projectId){
        webService.getJson(grailsApplication.config.ecodata.baseUrl + 'projectActivity/getAllByProject/'+ projectId).list
    }

    def get(projectActivityId){
        webService.getJson(grailsApplication.config.ecodata.baseUrl + 'projectActivity/get/'+ projectActivityId)
    }

    def create(pActivity) {
        update('', pActivity)
    }

    def update(id, body) {
        webService.doPost(grailsApplication.config.ecodata.baseUrl + 'projectActivity/' + id, body)
    }
}
