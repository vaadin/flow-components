/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.dom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class AbstractGridMultiSelectionModelWithHierarchicalDataProviderTest {
    private TreeGrid<String> treeGrid;
    private MockUI ui;

    @Before
    public void init() {
        treeGrid = new TreeGrid<>();
        // Data provider with only two root items, we don't need any nested
        // items for the test cases (so far)
        treeGrid.setItems(Arrays.asList("foo", "bar"),
                root -> Collections.emptyList());
        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);

        ui = new MockUI();
        ui.add(treeGrid);
    }

    @Test
    public void select_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // select first
        treeGrid.getSelectionModel().select("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // select second, which equals all selected
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.getSelectionModel().select("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void selectFromClient_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // select first
        treeGrid.getSelectionModel().selectFromClient("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // select second, which equals all selected
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.getSelectionModel().selectFromClient("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void deselect_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect first
        treeGrid.getSelectionModel().deselect("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect second, which equals none selected
        treeGrid.getSelectionModel().deselect("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void deselectFromClient_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect first
        treeGrid.getSelectionModel().deselectFromClient("foo");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // deselect second, which equals none selected
        treeGrid.getSelectionModel().deselectFromClient("bar");
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void clientSelectAll_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void deselectAll_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        treeGrid.getSelectionModel().deselectAll();
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void clientDeselectAll_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // start with all selected
        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientSelectAll();
        Assert.assertTrue((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        ((AbstractGridMultiSelectionModel<String>) treeGrid.getSelectionModel())
                .clientDeselectAll();
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
    }

    @Test
    public void updateSelection_updatesCheckboxStates() {
        Element columnElement = getGridSelectionColumn(treeGrid).getElement();

        // Select all
        // with hierarchical data provider we can not detect whether all are
        // selected, so should still be indeterminate
        treeGrid.asMultiSelect().updateSelection(
                new HashSet<>(Arrays.asList("foo", "bar")), new HashSet<>());
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // Deselect single
        treeGrid.asMultiSelect().updateSelection(new HashSet<>(),
                new HashSet<>(Collections.singletonList("foo")));
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertTrue(
                (boolean) columnElement.getPropertyRaw("indeterminate"));

        // Deselect all
        treeGrid.asMultiSelect().updateSelection(new HashSet<>(),
                new HashSet<>(Collections.singletonList("bar")));
        Assert.assertFalse((boolean) columnElement.getPropertyRaw("selectAll"));
        Assert.assertFalse(
                (boolean) columnElement.getPropertyRaw("indeterminate"));
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
