%{-- Not using this tag as we want  a protocol-less import<gvisualization:apiImport/>--}%
<script type="text/javascript" src="//www.google.com/jsapi"></script>

<g:set var="targets" value="${metrics.targets}"/>
<g:set var="other" value="${metrics.other}"/>
<g:if test="${targets || other}">

    <g:if test="${targets}">
    <h3 style="margin-top:0;">Progress against Output Targets</h3>
    <div class="row-fluid">
        <div class="span4">
            <g:set var="count" value="${targets.size()}"/>
            <g:each in="${targets?.entrySet()}" var="metric" status="i">
            %{--This is to stack the output metrics in three columns, the ceil biases uneven amounts to the left--}%
            <g:if test="${i == Math.ceil(count/3) || i == Math.ceil(count/3*2)}">
        </div>
        <div class="span4">
                </g:if>
                <div class="well">
                    <h3>${metric.key}</h3>
                    <g:each in="${metric.value}" var="score">
                        <fc:renderScore score="${score}"></fc:renderScore>
                    </g:each>
                </div>
            </g:each>
        </div>
    </div>
    </g:if>

    <g:if test="${other}">

        <h3>Progress of Outputs without targets</h3>
        <div class="outputs-without-targets">

        <g:each in="${other?.entrySet()}" var="metric" status="i">
            <g:if test="${i%2 == 0}">
                <div class="row-fluid">

            </g:if>

                <div class="span6 well well-small">
                    <h3>${metric.key}</h3>
                    <g:each in="${metric.value}" var="score">
                        <fc:renderScore score="${score}"></fc:renderScore>
                    </g:each>
                </div>
            <g:if test="${i%2 != 0 || i == (other.size()-1)}">

                </div>
            </g:if>

        </g:each>
        </div>
    </g:if>
</g:if>
<g:else>
    <p>No activities or output targets have been defined for this project.</p>
</g:else>

<script>

    $(document).on('dashboardShown', function () {

//        var content = $('.outputs-without-targets');
//        var columnized = content.find('.column').length > 0;
//        if (!columnized){
//            content.columnize({ columns: 2, lastNeverTallest:true, accuracy: 10 });
//        }

        // We need to reinitialise the popovers as the content has been moved by the columnizer.
        $('.helphover').data('popover', null);
        $('.helphover').popover({container:'body', animation: true, trigger:'hover'});
    });
</script>

