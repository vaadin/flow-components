/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.PersonWithLevel;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/tree-grid-drag-and-drop")
public class TreeGridDragAndDropPage extends Div {

    public TreeGridDragAndDropPage() {
        PeopleGenerator peopleGenerator = new PeopleGenerator();
        List<PersonWithLevel> people = peopleGenerator
                .generatePeopleWithLevels(1000, 0);

        TreeGrid<PersonWithLevel> grid = new TreeGrid<>();
        grid.setHeight("700px");
        grid.addHierarchyColumn(person -> person.getFirstName())
                .setHeader("First name");

        TreeData<PersonWithLevel> data = new TreeData<>();
        data.addItems(people, person -> {
            if (person.getLevel() == 0) {
                return peopleGenerator.generatePeopleWithLevels(5, 1);
            }

            return Collections.emptyList();
        });
        grid.setDataProvider(new TreeDataProvider<>(data));
        grid.setRowsDraggable(true);
        grid.setDragFilter((person) -> person.getLevel() > 0);
        grid.setDropFilter((person) -> person.getLevel() > 0);

        grid.addDragStartListener(e -> {
            grid.setDropMode(GridDropMode.ON_TOP);
        });

        grid.addDropListener(e -> {
            grid.getDataProvider().refreshAll();
        });

        grid.addDragEndListener(e -> {
            grid.setDropMode(null);
        });

        add(grid);
    }

}
