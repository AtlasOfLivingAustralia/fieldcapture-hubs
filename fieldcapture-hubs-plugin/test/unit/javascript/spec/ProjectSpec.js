describe("ProjectViewModel Spec", function () {

    it("should be able to be initialised from an object literal", function () {

        var projectData = {
            name:'Name',
            description:'Description'
        };
        var organisations = [];
        var isEditor = true;
        var project = new ProjectViewModel(projectData, isEditor, organisations);

        expect(project.name()).toEqual(projectData.name);
        expect(project.description()).toEqual(projectData.description);
    });

    it("should compute the project type and whether the project is a citizen science project from a single selection", function() {
        var projectData = {
            isCitizenScience:true,
            projectType:'survey'
        };
        var project = new ProjectViewModel(projectData);
        expect(project.transients.kindOfProject()).toBe('citizenScience');

        projectData.projectType = 'works';
        expect(new ProjectViewModel(projectData).transients.kindOfProject()).toBe('citizenScience');


        projectData.isCitizenScience = false;
        expect(new ProjectViewModel(projectData).transients.kindOfProject()).toBe('works');

        projectData.projectType = 'survey';
        expect(new ProjectViewModel(projectData).transients.kindOfProject()).toBe('survey');


        // default type is "works" for MERIT compatibility.
        projectData.projectType = undefined;
        expect(new ProjectViewModel(projectData).transients.kindOfProject()).toBe('works');

        project = new ProjectViewModel({});
        project.transients.kindOfProject('citizenScience');
        expect(project.isCitizenScience()).toBe(true);
        expect(project.projectType()).toBe('survey');


        project.transients.kindOfProject('survey');
        expect(project.isCitizenScience()).toBe(false);
        expect(project.projectType()).toBe('survey');

        project.transients.kindOfProject('works');
        expect(project.isCitizenScience()).toBe(false);
        expect(project.projectType()).toBe('works');


    });

});
