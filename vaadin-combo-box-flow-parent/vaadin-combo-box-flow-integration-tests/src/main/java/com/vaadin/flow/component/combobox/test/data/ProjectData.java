/*
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.combobox.test.data;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.combobox.test.entity.Project;

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
