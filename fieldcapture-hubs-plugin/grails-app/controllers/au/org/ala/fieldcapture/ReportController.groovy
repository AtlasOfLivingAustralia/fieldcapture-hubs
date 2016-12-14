package au.org.ala.fieldcapture

class ReportController {

    static defaultAction = "dashboard"
    def webService, cacheService, searchService, metadataService

    def loadReport() {
        forward action: params.report+'Report', params:params
    }

    def dashboardReport() {
        render view:'_dashboard', model:processActivityOutputs(params)
    }

    def activityOutputs() {

        def selectedCategory = params.remove('category')
        def model = processActivityOutputs(params)
        model.category = selectedCategory
        render view:'_activityOutputs', model:model
    }

    def processActivityOutputs(params) {

        def defaultCategory = "Not categorized"
        def categories = metadataService.getReportCategories()

        // The _ parameter is appended by jquery ajax calls and will stop the report contents from being cached.
        params.remove("_")
        params.remove('action') // We want the same parameters as the original call so we can use the cache.

        def results = searchService.dashboardReport(params)
        def scores = results.outputData

        def scoresByCategory = scores.groupBy{
            (it.category?:defaultCategory)
        }

        def doubleGroupedScores = [:]
        // Split the scores up into 2 columms for display.
        scoresByCategory.each { category, categoryScores ->

            categoryScores.sort{it.outputType}
            def previousOutput = ""
            def runningHeights = categoryScores.collect {
                def height = DashboardTagLib.estimateHeight(it)
                if (it.outputType != previousOutput) {
                    height += 60 // Account for the output name header, padding etc.
                    previousOutput = it.outputType
                }
                height
            }
            def totalHeight = runningHeights.sum()
            def columns = [[], []]

            def runningHeight = 0
            // Iterating backwards to bias the left hand column.
            for (int i=0 ; i<categoryScores.size(); i++) {

                def idx = runningHeight <= (totalHeight/2) ? 0 : 1
                runningHeight += runningHeights[i]

                columns[idx] << categoryScores[i]
            }


            def columnsGroupedByOutput = [columns[0].groupBy{it.outputType}, columns[1].groupBy{it.outputType}]
            doubleGroupedScores.put(category, columnsGroupedByOutput)
        }
        if (scoresByCategory.keySet().contains(defaultCategory)) {
            categories << defaultCategory
        }

        def sortedCategories = []
        sortedCategories.addAll(categories)
        sortedCategories.sort()

        [categories:categories.sort(), scores:doubleGroupedScores, metadata:results.metadata]

    }

}
