
<g:if test="${metadata.projects.size() > 1}">
<p>
Data presented in this dashboard has been extracted from grant recipient progress reports approved by the Department of the Environment. While efforts are made to ensure the accuracy of the ecological information contained in MERIT, for confirmation of authoritative data please contact the <g:createLink controller="home" action="contacts">Department of Environment</g:createLink>. Also note that dashboard data for Biodiversity Fund Round One are incomplete due to legacy issues.
</p>
<div class="accordion" id="reports">
    <g:each in="${categories}" var="category" status="i">

        <g:set var="categoryContent" value="category_${i}"/>
        <div class="accordion-group">
            <div class="accordion-heading header">
                <a class="accordion-toggle" data-toggle="collapse" data-parent="#reports" href="#${categoryContent}">
                    ${category} <g:if test="${!scores[category]}"><span class="pull-right" style="font-weight:normal">[no data available]</span></g:if>

                </a>
            </div>
            <div id="${categoryContent}" class="outputData accordian-body collapse" data-category="${category}">
                <div class="accordian-inner row-fluid">
                    <r:img width="50" height="50" dir="images" file="loading.gif" alt="saving icon"/> Loading...
                </div>
            </div>

        </div>
    </g:each>

        <div id="metadata">
            results include approved reported data from ${metadata.projects.size()} projects, ${metadata.sites} sites and ${metadata.activities} activities

        </div>

</div>

</g:if>
<g:else>
    <div class="alert alert-error">
        Not enough data was returned to display summary data for your facet selection.
    </div>
</g:else>

<script type="text/javascript">


    $(function() {
        var loadingTemplate = '<div class="accordian-inner row-fluid">'+
            '<r:img width="50" height="50" dir="images" file="loading.gif" alt="saving icon"/> Loading...'+
            '</div>';
        $('.collapse').on('show', function() {
            var $div = $(this);

            var category = $div.data('category');
            var url = fcConfig.dashboardCategoryUrl;
            $.ajax({url:url, data:{report:'dashboard', category:category}}).done(function(data) {

                $div.html(data);
            });
        }).on('hidden', function() {
            var $div = $(this);
            $div.empty().append(loadingTemplate);
        });
    });

</script>

