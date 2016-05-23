<g:set var="blockId" value="${divId?:(activity.activityId+fc.toSingleWord([name: outputName]))}"/>
<g:set var="output" value="${activity.outputs.find {it.name == outputName}}"/>
<g:if test="${!output}">
    <g:set var="output" value="[name: outputName]"/>
</g:if>
<div class="output-block" id="ko${blockId}">
    <h3>${outputName}</h3>
    <div data-bind="if:outputNotCompleted">
        <label class="checkbox" ><input type="checkbox" disabled="disabled" data-bind="checked:outputNotCompleted"> <span data-bind="text:transients.questionText"></span> </label>
    </div>
    <g:if test="${!output.outputNotCompleted}">
        <!-- add the dynamic components -->
        <md:modelView model="${outputModel}" site="${site}"/>
    </g:if>
    <r:script>
        $(function(){

            var viewModelName = "${fc.toSingleWord(name:outputName)}ViewModel";
            var viewModelInstance = "${blockId}Instance";

            var output = <fc:modelAsJavascript model="${output}"/>;
            var config = ${fc.modelAsJavascript(model:activityModel.outputConfig?.find{it.outputName == outputName}, default:'{}')};

            window[viewModelInstance] = new window[viewModelName](output, site, config);
            window[viewModelInstance].loadData(output.data || {}, <fc:modelAsJavascript model="${activity.documents}"/>);

            ko.applyBindings(window[viewModelInstance], document.getElementById("ko${blockId}"));
        });

    </r:script>
</div>