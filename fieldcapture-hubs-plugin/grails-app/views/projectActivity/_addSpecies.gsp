<span data-bind="if: species.transients.showAddSpeciesLists">

<div class="well">

    <div class="row-fluid">
        <div class="span4 text-left">
            <label>Species lists name:</label>
            <input style="width:100%;" data-bind="value: species.newSpeciesLists.listName" type="text" data-validation-engine="validate[required]" />
        </div>
        <div class="span4 text-left">
            <label>List Type</label>
            <select style="width:100%;" data-validation-engine="validate[required]" data-bind="options: species.transients.allowedListTypes, optionsText:'name', optionsValue:'id', value: species.newSpeciesLists.listType, optionsCaption: 'Please select'"></select>
        </div>
    </div>


    <div class="row-fluid">
        <div class="span8 text-left">
            <label>Description</label>
            <textarea style="width:100%;" data-bind="value: species.newSpeciesLists.description" data-validation-engine="validate[required]"></textarea>
        </div>
    </div>

    <div class="row-fluid">
        <div class="span8 text-left">
            <label>Species name (Comma separated)</label>
            <textarea rows="10" style="width:100%;" data-bind="value: species.newSpeciesLists.transients.bulkSpeciesNames" data-validation-engine="validate[required]"></textarea>
        </div>
    </div>

    <!-- ko foreach: species.newSpeciesLists.allSpecies -->
    <div class="row-fluid">

        <div class="span6 text-left">
            <span data-bind="if: guid">
                <a data-bind="attr:{href: transients.bioProfileUrl}" target="_blank"><small data-bind="text: transients.textFieldValue"></small></a>
            </span>
            </br>
            <input style="width:80%;" type="text" placeholder="Species name"
                   data-bind="value: transients.textFieldValue,
                                            event:{focusout: focusLost},
                                            autocomplete:{
                                                url:'http://devt.ala.org.au:8088/fieldcapture-hub/search/species',
                                                render: renderItem,
                                                listId: list,
                                                result: speciesSelected,
                                                valueChangeCallback: textFieldChanged
                                            }" data-validation-engine="validate[required]">
            <button class="btn btn-small" data-bind="click: $parent.species.newSpeciesLists.removeNewSpeciesName"> X</button>


        </div>


    </div>
    <!-- /ko -->
    </br>
    <div class="row-fluid">
        <div class="span8 text-left">
            <button class="btn btn-small" data-bind="click: species.newSpeciesLists.addNewSpeciesName"> <i class="icon icon-search"></i> Search and add species name</button>
        </div>
    </div>
    </br>
    <div class="row-fluid">
        <div class="span8 text-left">
            <span style="display:none;" id="addNewSpecies-status">
                <r:img dir="images"  width="23" height="23" file="loading-1.gif" alt="saving icon"/>
            </span>
            <button class="btn btn-small btn-primary" data-bind="click: species.saveNewSpeciesName">  Add New Species Lists</button>
            <button class="btn btn-small" data-bind="click: species.transients.toggleShowAddSpeciesLists">  Close</button>
        </div>
    </div>

</div>

</span>