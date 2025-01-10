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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.PersonWithLevel;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/tree-grid-basic")
public class TreeGridBasicPage extends Div {

    public TreeGridBasicPage() {
        PeopleGenerator peopleGenerator = new PeopleGenerator();
        List<PersonWithLevel> people = peopleGenerator
                .generatePeopleWithLevels(1000, 0);

        TreeGrid<PersonWithLevel> grid = new TreeGrid<>();
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

        NativeButton expandAll = new NativeButton("Expand all",
                e -> grid.expandRecursively(data.getRootItems(), 3));
        expandAll.setId("expand-all");

        NativeButton refreshItem = new NativeButton("Refresh item", e -> {
            PersonWithLevel person = data.getChildren(people.get(0)).get(1);
            person.setFirstName("Updated");
            grid.getDataProvider().refreshItem(person);
        });

        add(grid, expandAll, refreshItem);
    }

}
