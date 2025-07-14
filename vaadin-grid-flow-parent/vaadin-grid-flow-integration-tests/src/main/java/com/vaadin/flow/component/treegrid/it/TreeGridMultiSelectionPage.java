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

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridMultiSelectionModel.SelectAllCheckboxVisibility;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.bean.PeopleGenerator;
import com.vaadin.flow.data.bean.PersonWithLevel;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/treegrid-multi-selection")
public class TreeGridMultiSelectionPage extends Div {
    public TreeGridMultiSelectionPage() {
        TreeGrid<PersonWithLevel> treeGrid = new TreeGrid<>();
        treeGrid.addHierarchyColumn(
                (person) -> person.getFirstName()).setHeader("Name");
        treeGrid.setSelectionMode(SelectionMode.MULTI);

        ((GridMultiSelectionModel<PersonWithLevel>) treeGrid
                .getSelectionModel()).setSelectAllCheckboxVisibility(
                        SelectAllCheckboxVisibility.VISIBLE);

        PeopleGenerator peopleGenerator = new PeopleGenerator();
        TreeData<PersonWithLevel> treeData = new TreeData<>();
        treeData.addItems(peopleGenerator.generatePeopleWithLevels(3, 0),
                person -> {
                    if (person.getLevel() <= 1) {
                        return peopleGenerator.generatePeopleWithLevels(3,
                                person.getLevel() + 1);
                    }

                    return Collections.emptyList();
                });
        treeGrid.setTreeData(treeData);

        add(treeGrid);
    }
}
