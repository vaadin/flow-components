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
package com.vaadin.flow.component.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.shared.SelectionOnDataChange;
import com.vaadin.flow.data.selection.MultiSelect;
import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.data.selection.SingleSelect;

/**
 * Unit tests for Grid selection.
 *
 * @author Vaadin Ltd.
 */
public class GridSelectionTest {

    @Test
    public void singleSelection_selectCurrent_noEvent() {
        Grid<String> grid = new Grid<>();
        grid.setItems("one", "two");

        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();
        singleSelect.setValue("one");

        singleSelect.addValueChangeListener(event -> Assert
                .fail("Selection change should not be triggered"));

        singleSelect.setValue(singleSelect.getValue());
    }

    @Test
    public void multiSelectionListeners() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);

        Set<String> oldSelection = new LinkedHashSet<>();
        Set<String> selection = new LinkedHashSet<>(
                Arrays.asList("0", "1", "2"));

        AtomicInteger selectionListenerCalled = new AtomicInteger();
        grid.addSelectionListener(event -> {
            selectionListenerCalled.incrementAndGet();
            Assert.assertEquals(selection, event.getAllSelectedItems());
        });

        AtomicInteger valueChangeListenerCalled = new AtomicInteger();
        grid.asMultiSelect().addValueChangeListener(event -> {
            valueChangeListenerCalled.incrementAndGet();
            Assert.assertEquals(oldSelection, event.getOldValue());
            Assert.assertEquals(selection, event.getValue());
        });

        AtomicInteger multiSelectionListenerCalled = new AtomicInteger();
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .addMultiSelectionListener(event -> {
                    multiSelectionListenerCalled.incrementAndGet();
                    Assert.assertEquals(oldSelection, event.getOldSelection());
                    Assert.assertEquals(selection, event.getValue());

                    Set<String> oldCopy = new LinkedHashSet<>(oldSelection);
                    Set<String> copy = new LinkedHashSet<>(selection);
                    oldCopy.removeAll(copy);
                    Assert.assertEquals(oldCopy, event.getRemovedSelection());
                    oldCopy = new LinkedHashSet<>(oldSelection);
                    copy.removeAll(oldCopy);
                    Assert.assertEquals(copy, event.getAddedSelection());
                });

        grid.asMultiSelect().setValue(selection);

        oldSelection.addAll(selection);
        selection.clear();
        selection.addAll(Arrays.asList("10", "1", "5"));
        grid.asMultiSelect().setValue(selection);

        Assert.assertEquals(2, selectionListenerCalled.get());
        Assert.assertEquals(2, valueChangeListenerCalled.get());
        Assert.assertEquals(2, multiSelectionListenerCalled.get());
    }

    @Test
    public void multiSelectGrid_discard_changeEvent() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        multiSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionOnDataChange(SelectionOnDataChange.DISCARD);

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertTrue(events.isEmpty());

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertEquals(1, events.size());
    }

    @Test
    public void multiSelectGrid_preserveExistent_changeEvent() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        multiSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionOnDataChange(SelectionOnDataChange.PRESERVE_EXISTENT);

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertTrue(events.isEmpty());

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        // TODO fix, 0 events
        Assert.assertEquals(1, events.size());
    }

    @Test
    public void multiSelectGrid_preserveAll_changeEvent() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        multiSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionOnDataChange(SelectionOnDataChange.PRESERVE_ALL);

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertTrue(events.isEmpty());

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());
    }

    @Test
    public void singleSelectGrid_discard_changeEvent() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.SINGLE);
        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        singleSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionOnDataChange(SelectionOnDataChange.DISCARD);

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertTrue(events.isEmpty());

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertEquals(1, events.size());
    }

    @Test
    public void singleSelectGrid_preserveExistent_changeEvent() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.SINGLE);
        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        singleSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionOnDataChange(SelectionOnDataChange.PRESERVE_EXISTENT);

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertTrue(events.isEmpty());

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertEquals(1, events.size());
    }

    @Test
    public void singleSelectGrid_preserveAll_changeEvent() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.SINGLE);
        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        singleSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionOnDataChange(SelectionOnDataChange.PRESERVE_ALL);

        grid.getDataProvider().refreshAll();
        Assert.assertTrue(grid.getSelectedItems().isEmpty());
        Assert.assertTrue(events.isEmpty());

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assert.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assert.assertTrue(events.isEmpty());
    }
}
