/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider.HierarchyFormat;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

@ParameterizedClass
@EnumSource(HierarchyFormat.class)
class AbstractGridMultiSelectionModelWithHierarchicalDataProviderTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    @Parameter
    HierarchyFormat hierarchyFormat;

    private TreeGrid<String> treeGrid;

    @BeforeEach
    void setup() {
        TreeData<String> treeData = new TreeData<>();
        treeData.addRootItems("Item 0", "Item 1");
        treeData.addItems("Item 0", "Item 0-0", "Item 0-1");
        treeData.addItems("Item 0-0", "Item 0-0-0");

        treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(
                new TreeDataProvider<>(treeData, hierarchyFormat));
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        ui.add(treeGrid);
    }

    @Test
    void select_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // select first
        treeGrid.getSelectionModel().select("Item 0");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select remaining, which equals all selected
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.getSelectionModel().select("Item 0-0");
        treeGrid.getSelectionModel().select("Item 0-0-0");
        treeGrid.getSelectionModel().select("Item 0-1");
        treeGrid.getSelectionModel().select("Item 1");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void selectFromClient_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // select first
        treeGrid.getSelectionModel().selectFromClient("Item 0");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select remaining, which equals all selected
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.getSelectionModel().selectFromClient("Item 0-0");
        treeGrid.getSelectionModel().selectFromClient("Item 0-0-0");
        treeGrid.getSelectionModel().selectFromClient("Item 0-1");
        treeGrid.getSelectionModel().selectFromClient("Item 1");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void deselect_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect first
        treeGrid.getSelectionModel().deselect("Item 0");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect remaining, which equals none selected
        treeGrid.getSelectionModel().deselect("Item 0-0");
        treeGrid.getSelectionModel().deselect("Item 0-0-0");
        treeGrid.getSelectionModel().deselect("Item 0-1");
        treeGrid.getSelectionModel().deselect("Item 1");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void deselectFromClient_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect first
        treeGrid.getSelectionModel().deselectFromClient("Item 0");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect remaining, which equals none selected
        treeGrid.getSelectionModel().deselectFromClient("Item 0-0");
        treeGrid.getSelectionModel().deselectFromClient("Item 0-0-0");
        treeGrid.getSelectionModel().deselectFromClient("Item 0-1");
        treeGrid.getSelectionModel().deselectFromClient("Item 1");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void clientSelectAll_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void deselectAll_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        treeGrid.getSelectionModel().deselectAll();
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void clientDeselectAll_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientDeselectAll();
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void updateSelection_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // Select all
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.asMultiSelect().updateSelection(Set.of("Item 0", "Item 0-0",
                "Item 0-0-0", "Item 0-1", "Item 1"), Set.of());
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect single
        treeGrid.asMultiSelect().updateSelection(Set.of(), Set.of("Item 0"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect remaining, which equals none selected
        treeGrid.asMultiSelect().updateSelection(Set.of(),
                Set.of("Item 0-0", "Item 0-0-0", "Item 0-1", "Item 1"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    private <T> GridSelectionColumn getGridSelectionColumn(Grid<T> grid) {
        Component child = grid.getChildren().findFirst().orElseThrow(
                () -> new IllegalStateException("Grid does not have a child"));
        if (!(child instanceof GridSelectionColumn)) {
            throw new IllegalStateException(
                    "First Grid child is not a GridSelectionColumn");
        }
        return (GridSelectionColumn) child;
    }
}
