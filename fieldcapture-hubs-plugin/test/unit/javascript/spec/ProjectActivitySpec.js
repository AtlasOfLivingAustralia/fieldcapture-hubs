describe("ProjectActivityViewModel Spec", function () {

    it("should be able to be initialised from an object literal", function () {

        var pActivities = []
        var pActivityForms = [];
        var projectId = "abcd";
        var projectActivity = new ProjectActivitiesViewModel(pActivities, pActivityForms, projectId);

        expect(projectActivity.projectId()).toEqual(projectId);
        expect(projectActivity.speciesOptions.length).toBeGreaterThan(0)
        expect(projectActivity.projectActivities.length).toEqual(0)

    });

    it("Survey should have a default survey name", function () {
        var pActivity = new ProjectActivity([],[],[],[]);
        expect(pActivity.name()).toEqual("Survey name");
    });

    it("default survey status should be active", function () {
        var pActivity = new ProjectActivity([],[],[],[]);
        expect(pActivity.status()).toEqual("active");
    });

});

describe("SpeciesConstraintViewModel Spec", function () {

    it("should be able to be initialised from an object literal", function () {
        var speciesVM = new SpeciesConstraintViewModel();
        expect(speciesVM.transients.allowedListTypes.length).toBeGreaterThan(0);
    });

    it("by default both add lists and pick lists should not be visible", function () {
        var speciesVM = new SpeciesConstraintViewModel();
        expect(speciesVM.transients.showAddSpeciesLists()).toBe(false);
        expect(speciesVM.transients.showExistingSpeciesLists()).toBe(false);
    });

    it("single species selection should make single species option visible", function () {
        var speciesVM = new SpeciesConstraintViewModel();
        speciesVM.type("SINGLE_SPECIES");
        expect(speciesVM.singleInfoVisible()).toBe(true);
        speciesVM.type("GROUP_OF_SPECIES");
        expect(speciesVM.groupInfoVisible()).toBe(true);
    });

    it("invalid species options should return empty map", function () {
        var speciesVM = new SpeciesConstraintViewModel();
        speciesVM.type("XYZ");
        var map = {};
        expect(speciesVM.asJson()).toEqual(map);

        speciesVM.type("SINGLE_SPECIES");
        var map = {};
        expect(speciesVM.asJson()).not.toEqual(map);
    });

});

describe("SpeciesListsViewModel Spec", function () {

    it("default offset value should be 0", function () {
        var listsVM = new SpeciesListsViewModel();
        expect(listsVM.offset()).toEqual(0);
    });

    it("previous selection should be available when offset is greater than 0", function () {
        var listsVM = new SpeciesListsViewModel();
        listsVM.offset(0);
        expect(listsVM.isPrevious ()).toEqual(false);
        listsVM.offset(2);
        expect(listsVM.isPrevious ()).toEqual(true);
    });

    it("next selection should not be available when total species count is 0", function () {
        var listsVM = new SpeciesListsViewModel();
        listsVM.listCount(0);
        expect(listsVM.isNext ()).toEqual(false);
    });
});