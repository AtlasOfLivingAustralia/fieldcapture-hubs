package au.org.ala.fieldcapture

class AuditService {

    def webService
    def grailsApplication

    def getAuditMessagesForProject(String projectId) {
        String url = grailsApplication.config.ecodata.baseUrl + 'audit/ajaxGetAuditMessagesForProject?projectId=' + projectId
        return webService.getJson(url, 60000)
    }

    def getAuditMessagesForOrganisation(String organisationId) {
        String url = grailsApplication.config.ecodata.baseUrl + 'audit/getAuditMessagesForOrganisation?organisationId=' + organisationId
        return webService.getJson(url, 60000)
    }

    def getAuditMessagesForSettings() {
        String url = grailsApplication.config.ecodata.baseUrl + 'audit/getAuditMessagesForSettings?keyPrefix=merit'
        return webService.getJson(url, 60000)
    }

    def getAuditMessage(String messageId) {
        String url = grailsApplication.config.ecodata.baseUrl + 'audit/ajaxGetAuditMessage/' + messageId
        return webService.getJson(url)
    }

    def getUserDetails(String userId) {
        String url = grailsApplication.config.ecodata.baseUrl + 'audit/ajaxGetUserDetails/' + userId
        return webService.getJson(url)
    }

    Map compareProjectEntity(String projectId, String baselineDate, String beforeDate, String entityPath) {

        Map auditResult = getAuditMessagesForProject(projectId)

        Map baselineEdit = null
        Map comparisonEdit = null

        if (auditResult && auditResult.messages) {

            boolean finished = false
            int i = 0
            while (i < auditResult.messages.size() && !finished) {
                Map message = auditResult.messages[i]
                if (message.entityType == "au.org.ala.ecodata.Project") {

                    if (!baselineEdit && (message.date < baselineDate) && message.entity[entityPath]) {
                        baselineEdit = message
                    }
                    else if (baselineEdit && !comparisonEdit && (message.date < beforeDate)) {
                        if (message.entity[entityPath] != baselineEdit.entity[entityPath]) {
                            comparisonEdit = message
                        }
                    }
                }
                if (baselineEdit != null && comparisonEdit != null) {
                    finished = true
                }
                i++
            }
        }
        [baseline: baselineEdit, comparison:comparisonEdit]
    }

}
