package com.vaadin.flow.component.combobox.test.data;

import com.vaadin.flow.component.combobox.test.entity.Project;

import java.util.ArrayList;
import java.util.List;

public class ProjectData {
    private final List<Project> PROJECT_LIST = createProjectList();

    private List<Project> createProjectList() {
        List<Project> projectList = new ArrayList<>();
        projectList.add(new Project(1, "Apollo"));
        projectList.add(new Project(2, "Aquarius"));
        projectList.add(new Project(3, "Polar"));

        return projectList;
    }

    public List<Project> getProjects() {
        return PROJECT_LIST;
    }

    public Project addProject(String name) {
        int id = PROJECT_LIST.size() + 1;
        Project newProject = new Project(id, name);
        PROJECT_LIST.add(newProject);
        return newProject;
    }
}
