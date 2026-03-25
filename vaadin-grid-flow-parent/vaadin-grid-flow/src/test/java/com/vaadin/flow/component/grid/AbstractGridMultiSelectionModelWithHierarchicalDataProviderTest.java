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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.dom.Element;
import com.vaadin.tests.MockUIExtension;

class AbstractGridMultiSelectionModelWithHierarchicalDataProviderTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private TreeGrid<String> treeGrid;

    @BeforeEach
    void setup() {
        treeGrid = new TreeGrid<>();
        // Data provider with only two root items, we don't need any nested
        // items for the test cases (so far)
        treeGrid.setItems(Arrays.asList("foo", "bar"),
                root -> Collections.emptyList());
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        ui.add(treeGrid);
    }

    @Test
    void select_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // select first
        treeGrid.getSelectionModel().select("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select second, which equals all selected
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.getSelectionModel().select("bar");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));
    }

    @Test
    void selectFromClient_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // select first
        treeGrid.getSelectionModel().selectFromClient("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // select second, which equals all selected
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.getSelectionModel().selectFromClient("bar");
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
        treeGrid.getSelectionModel().deselect("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect second, which equals none selected
        treeGrid.getSelectionModel().deselect("bar");
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
        treeGrid.getSelectionModel().deselectFromClient("foo");
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // deselect second, which equals none selected
        treeGrid.getSelectionModel().deselectFromClient("bar");
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
        treeGrid.asMultiSelect().updateSelection(Set.of("foo", "bar"),
                Set.of());
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect single
        treeGrid.asMultiSelect().updateSelection(Set.of(), Set.of("foo"));
        Assertions.assertFalse(
                (boolean) columnElement.getPropertyRaw("selectAll"));
        Assertions.assertTrue(
                (boolean) columnElement.getPropertyRaw("_indeterminate"));

        // Deselect all
        treeGrid.asMultiSelect().updateSelection(Set.of(), Set.of("bar"));
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
