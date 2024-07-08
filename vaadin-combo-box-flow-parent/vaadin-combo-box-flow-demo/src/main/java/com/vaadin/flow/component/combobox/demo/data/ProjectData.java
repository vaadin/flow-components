/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.demo.data;

import com.vaadin.flow.component.combobox.demo.entity.Project;

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
