package au.org.ala.fieldcapture

import grails.converters.JSON

/**
 * Generates web page content for metadata-driven dynamic data entry and display.
 */
class ModelTagLib {

    static namespace = "md"

    private final static INDENT = "    "
    private final static String QUOTE = "\"";
    private final static String SPACE = " ";
    private final static String EQUALS = "=";
    private final static String DEFERRED_TEMPLATES_KEY = "deferredTemplates"

    private final static int LAYOUT_COLUMNS = 12 // Bootstrap scaffolding uses a 12 column layout.

    /** Context for view layout (rows, columns etc). */
    class LayoutRenderContext {
        String parentView
        String dataContext
        int span
    }

    /*---------------------------------------------------*/
    /*------------ HTML for dynamic content -------------*/
    /*---------------------------------------------------*/

    /**
     * Main tag to insert html
     * @attrs model the data and view models
     * @attrs edit if true the html will support the editing of values
     */
    def modelView = { attrs ->

        LayoutRenderContext ctx = new LayoutRenderContext(parentView:'', dataContext: 'data', span:LAYOUT_COLUMNS)

        viewModelItems(out, attrs, attrs.model?.viewModel, ctx)

        renderDeferredTemplates out
    }

    def viewModelItems(out, Map attrs, List items, LayoutRenderContext ctx) {

        items?.eachWithIndex { mod, index ->
            switch (mod.type) {
                case 'table':
                    table out, attrs, mod
                    break
                case 'grid':
                    grid out, attrs, mod
                    break
                case 'section':
                    section out, attrs, mod
                case 'row':
                    row out, attrs, mod, ctx
                    break
                case 'template':
                    out << g.render(template:mod.source, plugin: 'fieldcapture-plugin')
                    break
                case 'repeat':
                    repeatingLayout out, attrs, mod, ctx
                    break
                case 'col':
                    column out, attrs, mod, ctx
                    break
                default:
                    layoutDataItem(out, attrs, mod, ctx)
                    break


            }
        }
    }

    def repeatingLayout(out, attrs, model, LayoutRenderContext ctx) {

        String sourceType = getType(attrs, model.source, null)
        if (sourceType != "list") {
            throw new Exception("Only model elements with a list data type can be the source for a repeating layout")
        }

        LayoutRenderContext childContext = new LayoutRenderContext(parentView:'', dataContext: '', span:ctx.span)

        out << """<div class="repeating-section" data-bind="foreach:data.${model.source}">"""
        viewModelItems(out, attrs, model.items, childContext)
        out << "</div>"
    }

    /**
     * Generates an element for display, depending on viewContext. Currently
     * @parma attrs the attributes passed to the tag library.  Used to access site id.
     * @param model of the data element
     * @param context the dot notation path to the data
     * @param editable if the html element is an input
     * @param elementAttributes any additional html attributes to output as a AttributeMap
     * @param databindAttrs additional clauses to add to the data binding
     * @return the markup
     */
    def dataTag(attrs, model, context, editable, elementAttributes, databindAttrs, labelAttributes) {
        ModelWidgetRenderer renderer


        def validate = validationAttribute(attrs, model, editable)

        if (attrs.printable) {
            if (attrs.printable == 'pdf') {
                renderer = new PDFModelWidgetRenderer()
            }
            else {
                renderer = new PrintModelWidgetRenderer()
            }
        } else {
            def toEdit = editable && !model.computed && !model.noEdit
            if (toEdit) {
                renderer = new EditModelWidgetRenderer()
            } else {
                renderer = new ViewModelWidgetRenderer()
            }
        }

        def renderContext = new WidgetRenderContext(model, context, validate, databindAttrs, elementAttributes, labelAttributes, g, attrs)

        // The data model item we are rendering the view for.
        Map source = getAttribute(attrs.model.dataModel, model.source)
        if (source?.behaviour) {
            source.behaviour.each { constraint ->
                println constraint
                renderContext.databindAttrs.add "constraint", "{${constraint.type}:${constraint.condition}}"
            }
        }

        if (model.visibility) {
            renderContext.databindAttrs.add "visible", evalDependency(model.visibility)
        }
        if (model.enabled) {
            renderContext.databindAttrs.add "enable", evalDependency(model.enabled)
        }
        if (model.readonly) {
            renderContext.attributes.add "readonly", "readonly"
        }

        switch (model.type) {
            case 'literal':
                renderer.renderLiteral(renderContext)
                break
            case 'text':
                renderer.renderText(renderContext)
                break;
            case 'number':
                renderer.renderNumber(renderContext)
                break
            case 'boolean':
                renderer.renderBoolean(renderContext)
                break
            case 'textarea':
                renderer.renderTextArea(renderContext)
                break
            case 'simpleDate':
                renderer.renderSimpleDate(renderContext)
                break
            case 'selectOne':
                renderer.renderSelectOne(renderContext)
                break;
            case 'selectMany':
                renderer.renderSelectMany(renderContext)
                break
            case 'image':
                renderer.renderImage(renderContext)
                break
            case 'embeddedImage':
                renderer.renderEmbeddedImage(renderContext)
                break
            case 'embeddedImages':
                renderer.renderEmbeddedImages(renderContext)
                break
            case 'autocomplete':
                renderer.renderAutocomplete(renderContext)
                break
            case 'photopoint':
                renderer.renderPhotoPoint(renderContext)
                break
            case 'link':
                renderer.renderLink(renderContext)
                break
            case 'date':
                renderer.renderDate(renderContext)
                break
            case 'document':
                renderer.renderDocument(renderContext)
                break
            default:
                log.warn("Unhandled widget type: ${model.type}")
                break
        }

        def result = renderContext.writer.toString()

        // make sure to remember any deferred templates
        renderContext.deferredTemplates?.each {
            addDeferredTemplate(it)
        }

        result = renderWithLabel(model, labelAttributes, attrs, editable, result)
        return result
    }

    private String renderWithLabel(Map model, AttributeMap labelAttributes, attrs, editable, String dataTag) {

        String result = dataTag
        if (model.preLabel) {
            labelAttributes.addClass 'preLabel'

            if (isRequired(attrs, model, editable)) {
                labelAttributes.addClass 'required'
            }

            String labelPlainText
            if (model.preLabel instanceof Map) {
                labelPlainText = "<span data-bind=\"expression:'${model.preLabel.computed}'\"></span>"
            }
            else {
                labelPlainText = model.preLabel
            }
            result = "<span ${labelAttributes.toString()}><label>${labelText(attrs, model, labelPlainText)}</label></span>" + dataTag
        }

        if (model.postLabel) {
            String postLabel
            labelAttributes.addClass 'postLabel'
            if (model.postLabel instanceof Map) {
                postLabel = "<span data-bind=\"expression:'\"${model.preLabel.computed}\"'\"></span>"
            }
            else {
                postLabel = model.postLabel
            }
            result = dataTag + "<span ${labelAttributes.toString()}>${postLabel}</span>"
        }


        result
    }




    /**
     * Generates the contents of a label, including help text if it is available in the model.
     * The attribute "helpText" on the view model is used first, if that does not exist, the dataModel "description"
     * attribute is used a fallback.  If that doesn't exist either, no help is added to the label.
     * @param attrs the taglib attributes, includes the full model.
     * @param model the current viewModel item being processed.
     * @param label text to use for the label.  Will also be used as a title for the help test.
     * @return a generated html string to use to render the label.
     */
    def labelText(attrs, model, label) {

        if (attrs.printable) {
            return label
        }

        def helpText = model.helpText

        if (!helpText) {

            if (model.source) {
                // Get the description from the data model and use that as the help text.
                def attr = getAttribute(attrs.model.dataModel, model.source)
                if (!attr) {
                    println "Attribute ${model.source} not found"
                }
                helpText = attr?.description
            }
        }
        helpText = helpText?fc.iconHelp([title:''], helpText):''
        return "${label}${helpText}"

    }

    def evalDependency(dependency) {
        if (dependency.source) {
            if (dependency.values) {
                return "jQuery.inArray(${dependency.source}(), ${dependency.values as JSON}) >= 0"
            }
            else if (dependency.value) {
                return "${dependency.source}() === ${dependency.value}"
            }
            return "${dependency.source}()"
        }
    }

    // convenience method for the above
    def dataTag(attrs, model, context, editable, at) {
        dataTag(attrs, model, context, editable, at, null, null)
    }

    // convenience method for the above
    def dataTag(attrs, model, context, editable) {
        dataTag(attrs, model, context, editable, null, null, null)
    }

    def specialProperties(attrs, properties) {
        return properties.collectEntries { entry ->
            switch (entry.getValue()) {
                case "#siteId":
                    entry.setValue(attrs?.site?.siteId)
                default:
                    return entry
            }
        }
    }

    // -------- validation declarations --------------------
    def getValidationCriteria(attrs, model, edit) {
        //log.debug "checking validation for ${model}, edit = ${edit}"
        if (!edit) { return []}  // don't bother if the user can't change it

        def validationCriteria = model.validate
        def dataModel = getAttribute(attrs.model.dataModel, model.source)

        if (!validationCriteria) {
            // Try the data model.
            validationCriteria = dataModel?.validate
        } // no criteria

        def criteria = []
        if (validationCriteria) {
            criteria = validationCriteria.tokenize(',')
            criteria = criteria.collect {
                def rule = it.trim()
                // Wrap these rules in "custom[]" to keep jquery-validation-engine happy and avoid having to
                // specify "custom" in the json.
                if (rule in ['number', 'integer', 'url', 'date', 'phone']) {
                    rule = "custom[${rule}]"
                }
                rule
            }
        }

        // Add implied numeric validation to numeric data types
        if (dataModel?.dataType == 'number') {
            if (!criteria.contains('custom[number]') && !criteria.contains('custom[integer]')) {
                criteria << 'custom[number]'
            }
            if (!criteria.find{it.startsWith('min')}) {
                criteria << 'min[0]'
            }
        }


        return criteria
    }

    def isRequired(attrs, model, edit) {
        def criteria = getValidationCriteria(attrs, model, edit)
        return criteria.contains("required")
    }

    def validationAttribute(attrs, model, edit) {
        def criteria = getValidationCriteria(attrs, model, edit)
        if (criteria.isEmpty()) {
            return ""
        }

        def values = []
        criteria.each {
            switch (it) {
                case 'required':
                    if (model.type == 'selectMany') {
                        values << 'minCheckbox[1]'
                    }
                    else {
                        values << it
                    }
                    break
                case 'number':
                    values << 'custom[number]'
                    break
                case it.startsWith('min:'):
                    values << it
                    break
                default:
                    values << it
            }
        }
        //log.debug " data-validation-engine='validate[${values.join(',')}]'"
        return " data-validation-engine='validate[${values.join(',')}]'"
    }

    // form section
    def section(out, attrs, model) {

        if (model.title) {
            out << "<h4>${model.title}</h4>"
        }
        out << "<div class=\"row-fluid space-after output-section\">\n"

        viewModelItems(attrs, out, model.items)

        out << "</div>"
    }

    // row model
    def row(out, attrs, model, ctx) {

        def span = (ctx.span / model.items.size())

        LayoutRenderContext childCtx = new LayoutRenderContext(parentView: 'row', dataContext: ctx.dataContext, span: span)

        def extraClassAttrs = model.class ?: ""
        def databindAttrs = model.visibility ? "data-bind=\"visible:${model.visibility}\"" : ""

        out << "<div class=\"row-fluid space-after ${extraClassAttrs}\" ${databindAttrs}>\n"
        if (model.align == 'right') {
            out << "<div class=\"pull-right\">\n"
        }
        viewModelItems(out, attrs, model.items, childCtx)
        if (model.align == 'right') {
            out << "</div>\n"
        }
        out << "</div>\n"
    }

    def column(out, attrs, model, LayoutRenderContext ctx) {

        LayoutRenderContext childCtx = new LayoutRenderContext(parentView: 'col', dataContext: ctx.dataContext, span: LAYOUT_COLUMNS)

        out << "<div class=\"span${ctx.span}\">\n"
        viewModelItems(out, attrs, model.items, childCtx)
        out << "</div>"
    }

    def layoutDataItem(out, attrs, model, LayoutRenderContext layoutContext) {

        AttributeMap at = new AttributeMap()
        at.addClass(model.css)
        // inject computed from data model

        model.computed = model.computed ?: getComputed(attrs, model.source, '')

        // Wrap data elements in rows to reset the bootstrap indentation on subsequent spans to save the
        // model definition from needing to do so.
        def labelAttributes = new AttributeMap()
        def elementAttributes = new AttributeMap()
        if (layoutContext.parentView == 'col') {
            out << "<div class=\"row-fluid\">"
            labelAttributes.addClass 'span4'
            if (model.type != "number") {
                elementAttributes.addClass 'span8'
            }
        } else {
            at.addSpan("span${layoutContext.span}")
            out << "<span${at.toString()}>"
            if (model.type != "number") {
                elementAttributes.addClass 'span12'
            }
        }

        out << INDENT << dataTag(attrs, model, layoutContext.dataContext, attrs.edit, elementAttributes, null, labelAttributes)

        if (layoutContext.parentView == 'col') {
            out << "</div>"
        }
        else {
            out << "</span>"
        }
    }

    def grid(out, attrs, model) {
        out << "<div class=\"row-fluid\">\n"
        out << INDENT*3 << "<table class=\"table table-bordered ${model.source}\">\n"
        gridHeader out, attrs, model
        if (attrs.edit) {
            gridBodyEdit out, attrs, model
        } else {
            gridBodyView out, attrs, model
        }
        footer out, attrs, model
        out << INDENT*3 << "</table>\n"
        out << INDENT*2 << "</div>\n"
    }

    def gridHeader(out, attrs, model) {
        out << INDENT*4 << "<thead><tr>"
        model.columns.each { col ->
            out << "<th>"
            out << col.title
            if (col.pleaseSpecify) {
                def ref = col.pleaseSpecify.source
                // $ means top-level of data
                if (ref.startsWith('$')) { ref = 'data.' + ref[1..-1] }
                if (attrs.edit) {
                    out << " (<span data-bind='clickToEdit:${ref}' data-input-class='input-mini' data-prompt='specify'></span>)"
                } else {
                    out << " (<span data-bind='text:${ref}'></span>)"
                }
            }
            out << "</th>"
        }
        out << '\n' << INDENT*4 << "</tr></thead>\n"
    }

    def gridBodyEdit(out, attrs, model) {
        out << INDENT*4 << "<tbody>\n"
        model.rows.eachWithIndex { row, rowIndex ->

            // >>> output the row heading cell
            AttributeMap at = new AttributeMap()
            at.addClass('shaded')  // shade the row heading
            if (row.strong) { at.addClass('strong') } // bold the heading if so specified
            // row and td tags
            out << INDENT*5 << "<tr>" << "<td${at.toString()}>"
            out << row.title
            if (row.pleaseSpecify) { //handles any requirement to allow the user to specify the row heading
                def ref = row.pleaseSpecify.source
                // $ means top-level of data
                if (ref.startsWith('$')) { ref = 'data.' + ref[1..-1] }
                out << " (<span data-bind='clickToEdit:${ref}' data-input-class='input-small' data-prompt='specify'></span>)"
            }
            // close td
            out << "</td>" << "\n"

            // find out if the cells in this row are computed
            def isComputed = getComputed(attrs, row.source, model.source)
            // >>> output each cell in the row
            model.columns[1..-1].eachWithIndex { col, colIndex ->
                out << INDENT*5 << "<td>"
                if (isComputed) {
                    out << "<span data-bind='text:data.${model.source}.get(${rowIndex},${colIndex})'></span>"
                } else {
                    out << "<span data-bind='ticks:data.${model.source}.get(${rowIndex},${colIndex})'></span>"
                    //out << "<input class='input-mini' data-bind='value:data.${model.source}.get(${rowIndex},${colIndex})'/>"
                }
                out << "</td>" << "\n"
            }

            out << INDENT*5 << "</tr>\n"
        }
        out << INDENT*4 << "</tr></tbody>\n"
    }

    def gridBodyView(out, attrs, model) {
        out << INDENT*4 << "<tbody>\n"
        model.rows.eachWithIndex { row, rowIndex ->

            // >>> output the row heading cell
            AttributeMap at = new AttributeMap()
            at.addClass('shaded')
            if (row.strong) { at.addClass('strong')}
            // row and td tags
            out << INDENT*5 << "<tr>" << "<td${at.toString()}>"
            out << row.title
            if (row.pleaseSpecify) { //handles any requirement to allow the user to specify the row heading
                def ref = row.pleaseSpecify.source
                // $ means top-level of data
                if (ref.startsWith('$')) { ref = 'data.' + ref[1..-1] }
                out << " (<span data-bind='text:${ref}'></span>)"
            }
            // close td
            out << "</td>" << "\n"

            // >>> output each cell in the row
            model.columns[1..-1].eachWithIndex { col, colIndex ->
                out << INDENT*5 << "<td>" <<
                    "<span data-bind='text:data.${model.source}.get(${rowIndex},${colIndex})'></span>" <<
                    "</td>" << "\n"
            }

            out << INDENT*5 << "</tr>\n"
        }
        out << INDENT*4 << "</tr></tbody>\n"
    }

    def table(out, attrs, model) {

        def isprintblankform = attrs.printable && attrs.printable != 'pdf'

        def extraClassAttrs = model.class ?: ""
        def tableClass = isprintblankform ? "printed-form-table" : ""
        def validation = model.editableRows && model.source ? "data-bind=\"independentlyValidated:data.${model.source}\"":""
        out << "<div class=\"row-fluid ${extraClassAttrs}\">\n"
        out << INDENT*3 << "<table class=\"table table-bordered ${model.source} ${tableClass}\" ${validation}>\n"
        tableHeader out, attrs, model
        if (isprintblankform) {
            tableBodyPrint out, attrs, model
        } else {
            tableBodyEdit out, attrs, model
            footer out, attrs, model
        }

        out << INDENT*3 << "</table>\n"
        out << INDENT*2 << "</div>\n"
    }

    def tableHeader(out, attrs, table) {


        out << INDENT*4 << "<thead><tr>"
        table.columns.eachWithIndex { col, i ->
            if (isRequired(attrs, col, attrs.edit)) {
                out << "<th class=\"required\">" + labelText(attrs, col, col.title) + "</th>"
            } else {
                out << "<th>" + labelText(attrs, col, col.title) + "</th>"
            }

        }
        if (table.source && attrs.edit && !attrs.printable) {
            out << "<th></th>"
        }
        out << '\n' << INDENT*4 << "</tr></thead>\n"
    }

    def tableBodyView (out, attrs, table) {
        if (!table.source) {
            out << INDENT*4 << "<tbody><tr>\n"
        }
        else {
            out << INDENT*4 << "<tbody data-bind=\"foreach: data.${table.source}\"><tr>\n"
        }
        table.columns.eachWithIndex { col, i ->
            col.type = col.type ?: getType(attrs, col.source, table.source)
            out << INDENT*5 << "<td>" << dataTag(attrs, col, '', false) << "</td>" << "\n"
        }
        out << INDENT*4 << "</tr></tbody>\n"
    }

    def tableBodyPrint (out, attrs, table) {

        def numRows = table.printRows ?: 10

        out << INDENT * 4 << "<tbody>\n"
        for (int rowIndex = 0; rowIndex < numRows; ++rowIndex) {
            out << INDENT * 5 << "<tr>"
            table.columns.eachWithIndex { col, i ->
                out << INDENT * 6 << "<td></td>\n"
            }

            out << INDENT * 5 << "</tr>"
        }
        out << INDENT * 4 << "</tbody>\n"
    }

    def tableBodyEdit (out, attrs, table) {
        // body elements for main rows
        if (attrs.edit) {

            def dataBind
            if (table.source) {
                def templateName = table.editableRows ? "${table.source}templateToUse" : "'${table.source}viewTmpl'"
                dataBind = "template:{name:${templateName}, foreach: data.${table.source}}"
            }
            else {
                def count = getUnnamedTableCount(true)
                def templateName = table.editableRows ? "${count}templateToUse" : "'${count}viewTmpl'"
                dataBind = "template:{name:${templateName}, data: data}"
            }

            out << INDENT*4 << "<tbody data-bind=\"${dataBind}\"></tbody>\n"
            if (table.editableRows) {
                // write the view template
                tableViewTemplate(out, attrs, table, false)
                // write the edit template
                tableEditTemplate(out, attrs, table)
            } else {
                // write the view template
                tableViewTemplate(out, attrs, table, attrs.edit)
            }
        } else {
            out << INDENT*4 << "<tbody data-bind=\"foreach: data.${table.source}\"><tr>\n"
            table.columns.eachWithIndex { col, i ->
                col.type = col.type ?: getType(attrs, col.source, table.source)
                out << INDENT*5 << "<td>" << dataTag(attrs, col, '', false) << "</td>" << "\n"
            }
            out << INDENT*4 << "</tr></tbody>\n"
        }

        // body elements for additional rows (usually summary rows)
        if (table.rows) {
            out << INDENT*4 << "<tbody>\n"
            table.rows.each { tot ->
                def at = new AttributeMap()
                if (tot.showPercentSymbol) { at.addClass('percent') }
                out << INDENT*4 << "<tr>\n"
                table.columns.eachWithIndex { col, i ->
                    if (i == 0) {
                        out << INDENT*4 << "<td>${tot.title}</td>\n"
                    } else {
                        // assume they are all computed for now
                        out << INDENT*5 << "<td>" <<
                          "<span${at.toString()} data-bind='text:data.frequencyTotals().${col.source}.${tot.source}'></span>" <<
                          "</td>" << "\n"
                    }
                }
                if (attrs.edit) {
                    out << INDENT*5 << "<td></td>\n"
                }
                out << INDENT*4 << "</tr>\n"
            }
            out << INDENT*4 << "</tbody>\n"
        }
    }

    def tableViewTemplate(out, attrs, model, edit) {
        def templateName = model.source ? "${model.source}viewTmpl" : "${getUnnamedTableCount(false)}viewTmpl"
        out << INDENT*4 << "<script id=\"${templateName}\" type=\"text/html\"><tr>\n"
        model.columns.eachWithIndex { col, i ->
            col.type = col.type ?: getType(attrs, col.source, model.source)
            def colEdit = edit && !col.readOnly
            String data = dataTag(attrs, col, '', colEdit)
            out << INDENT*5 << "<td>"
            if (col.type == 'boolean') {
                out << "<label style=\"table-checkbox-label\">" << data << "</label>"
            }
            else {
                out << data
            }
            out << "</td>" << "\n"
        }
        if (model.editableRows) {
                out << INDENT*5 << "<td>\n"
                out << INDENT*6 << "<button class='btn btn-mini' data-bind='click:\$root.edit${model.source}Row, enable:!\$root.${model.source}Editing()' title='edit'><i class='icon-edit'></i> Edit</button>\n"
                out << INDENT*6 << "<button class='btn btn-mini' data-bind='click:\$root.transients.${model.source}Support.removeRow, enable:!\$root.${model.source}Editing()' title='remove'><i class='icon-trash'></i> Remove</button>\n"
                out << INDENT*5 << "</td>\n"
        } else {
            if (edit && model.source) {
                out << INDENT*5 << "<td><i data-bind='click:\$root.transients.${model.source}Support.removeRow' class='icon-remove'></i></td>\n"
            }
        }
        out << INDENT*4 << "</tr></script>\n"
    }

    def tableEditTemplate(out, attrs, model) {
        def templateName = model.source ? "${model.source}viewTmpl" : "${getUnnamedTableCount(false)}viewTmpl"
        out << INDENT*4 << "<script id=\"${templateName}\" type=\"text/html\"><tr>\n"
        model.columns.eachWithIndex { col, i ->
            def edit = !col['readOnly'];
            // mechanism for additional data binding clauses
            def bindAttrs = new Databindings()
            if (i == 0) {bindAttrs.add 'hasFocus', 'isSelected'}
            // inject type from data model
            col.type = col.type ?: getType(attrs, col.source, model.source)
            // inject computed from data model
            col.computed = col.computed ?: getComputed(attrs, col.source, model.source)
            String data = dataTag(attrs, col, '', edit, null, bindAttrs, null)
            out << INDENT*5 << "<td>"
            if (col.type == 'boolean') {
                out << "<label style=\"table-checkbox-label\">" << data << "</label>"
            }
            else {
                out << data
            }
            out << data << "</td>" << "\n"
        }
        out << INDENT*5 << "<td>\n"
        out << INDENT*6 << "<a class='btn btn-success btn-mini' data-bind='click:\$root.accept${model.source}' href='#' title='save'>Update</a>\n"
        out << INDENT*6 << "<a class='btn btn-mini' data-bind='click:\$root.cancel${model.source}' href='#' title='cancel'>Cancel</a>\n"
        out << INDENT*5 << "</td>\n"
        out << INDENT*4 << "</tr></script>\n"
    }

    /**
     * Common footer output for both tables and grids.
     */
    def footer(out, attrs, model) {

        def colCount = 0
        def containsSpecies = model.columns.find{it.type == 'autocomplete'}
        out << INDENT*4 << "<tfoot>\n"
        model.footer?.rows.each { row ->
            colCount = 0
            out << INDENT*4 << "<tr>\n"
            row.columns.eachWithIndex { col, i ->
                def attributes = new AttributeMap()
                if (getAttribute(attrs, col.source, '', 'primaryResult') == 'true') {
                    attributes.addClass('value');
                }
                colCount += (col.colspan ? col.colspan.toInteger() : 1)
                def colspan = col.colspan ? " colspan='${col.colspan}'" : ''
                // inject type from data model
                col.type = col.type ?: getType(attrs, col.source, '')

                // inject computed from data model
                col.computed = col.computed ?: getComputed(attrs, col.source, '')
                out << INDENT*5 << "<td${colspan}>" << dataTag(attrs, col, 'data', attrs.edit, attributes) << "</td>" << "\n"
            }
            if (model.type == 'table' && attrs.edit) {
                out << INDENT*5 << "<td></td>\n"  // to balance the extra column for actions
                colCount++
            }
            out << INDENT*4 << "</tr>\n"
        }
        colCount = (model.columns?.size()?:0) + 1
        if (attrs.edit && model.userAddedRows) {

            out << INDENT*4 << """<tr><td colspan="${colCount}" style="text-align:left;">
                        <button type="button" class="btn btn-small" data-bind="click:transients.${model.source}Support.addRow"""
            if (model.editableRows) {
                out << ", enable:!\$root.${model.source}Editing()"
            }
            out << """">
                        <i class="icon-plus"></i> Add a row</button>"""
            if (!attrs.disableTableUpload) {
                out << """
                <button type="button" class="btn btn-small" data-bind="click:transients.${model.source}Support.showTableDataUpload"><i class="icon-upload"></i> Upload data for this table</button>


                    </td></tr>\n"""
                out << """<tr data-bind="visible:transients.${model.source}Support.tableDataUploadVisible"><td colspan="${colCount}">"""
                if (containsSpecies) {
                    out << """
                <div class="text-error text-left">
                    Note: Only valid exact scientific names will be matched and populated from the database (indicated by a green tick). Unmatched species will load, but will be indicated by a green <b>?</b>. Please check your uploaded data and correct as required.
                </div>"""
                }
                out << """<div class="text-left" style="margin:5px">
                    <a data-bind="attr:{'href':transients.${model.source}Support.templateDownloadUrl()}" target="${model.source}TemplateDownload" class="btn">Step 1 - Download template (.xlsx)</a>
                </div>

                <div class="text-left" style="margin:5px;">
                    <input type="checkbox" data-bind="checked:transients.${model.source}Support.appendTableRows" style="margin-right:5px">Append uploaded data to table (unticking this checkbox will result in all table rows being replaced)
                </div>

                <div class="btn fileinput-button" style="margin-left:5px">
                        <input id="${
                    model.source
                }TableDataUpload" type="file" name="data" data-bind="fileUploadNoImage:transients.${model.source}Support.tableDataUploadOptions">
                        Step 2 - Upload populated template
                </div>"""
            }
            out<<"""</td></tr>"""
            out << """ <script id="${model.source}template-upload" type="text/x-tmpl">{% %}</script>
                       <script id="${model.source}template-download" type="text/x-tmpl">{% %}</script>"""
        }
        else if (!model.edit && !attrs.printable) {
            out << """<tr><td colspan="${colCount}">
            <div class="text-left" style="margin:5px">
                <a data-bind="click:transients.${model.source}Support.downloadTemplateWithData" class="btn"><i class="fa fa-download"></i> Download the data from this table (.xlsx)</a>
            </div>
            </tr>"""
        }
        out << INDENT*4 << "</tfoot>\n"

    }

    def addDeferredTemplate(name) {
        def templates = pageScope.getVariable(DEFERRED_TEMPLATES_KEY);
        if (!templates) {
            templates = []
            pageScope.setVariable(DEFERRED_TEMPLATES_KEY, templates);
        }
        templates.add(name)
    }

    def renderDeferredTemplates(out) {

        // some templates need to be rendered after the rest of the view code as it was causing problems when they were
        // embedded inside table view/edit templates. (as happened if an image type was included in a table row).
        def templates = pageScope.getVariable(DEFERRED_TEMPLATES_KEY)
        templates?.each {
            out << g.render(template: it, plugin:'fieldcapture-plugin')
        }
        pageScope.setVariable(DEFERRED_TEMPLATES_KEY, null)
    }

    /*------------ methods to look up attributes in the data model -------------*/

    static String getType(attrs, name, context) {
        getAttribute(attrs, name, context, 'dataType')
    }

    static String getComputed(attrs, name, context) {
        getAttribute(attrs, name, context, 'computed')
    }

    static String getAttribute(attrs, name, context, attribute) {
        def dataModel = attrs.model.dataModel
        def level = dataModel.find {it.name == context}
        level = level ?: dataModel
        def target
        if (level.dataType in ['list','matrix', 'photoPoints']) {
            target = level.columns.find {it.name == name}
            if (!target) {
                target = level.rows.find {it.name == name}
            }
        }
        else {
            target = dataModel.find {it.name == name}
        }
        return target ? target[attribute] : null
    }

    def getAttribute(model, name) {
        return model.findResult( {

            if (it?.dataType == 'list') {
                return getAttribute(it.columns, name)
            }
            else {
                return (it.name == name)?it:null
            }

        })
    }

    /**
     * Uses a page scoped variable to track the number of unnamed tables on the page so each can have a unquie
     * rendering template.
     * @param increment true if the value should be incremented (the pre-incremented value will be returned)
     * @return
     */
    private int getUnnamedTableCount(boolean increment = false) {
        def name = 'unnamedTableCount'
        def count = pageScope.getVariable(name) ?: 0

        if (increment) {
            count++
        }
        pageScope.setVariable(name, count)

        return count
    }

}

