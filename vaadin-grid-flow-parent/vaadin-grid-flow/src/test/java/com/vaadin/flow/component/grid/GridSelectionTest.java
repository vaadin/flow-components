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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.data.provider.BackEndDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.selection.MultiSelect;
import com.vaadin.flow.data.selection.SingleSelect;

/**
 * Unit tests for Grid selection.
 *
 * @author Vaadin Ltd.
 */
class GridSelectionTest {

    @Test
    void singleSelection_selectCurrent_noEvent() {
        Grid<String> grid = new Grid<>();
        grid.setItems("one", "two");

        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();
        singleSelect.setValue("one");

        singleSelect.addValueChangeListener(event -> Assertions
                .fail("Selection change should not be triggered"));

        singleSelect.setValue(singleSelect.getValue());
    }

    @Test
    void multiSelectionListeners() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);

        Set<String> oldSelection = new LinkedHashSet<>();
        Set<String> selection = new LinkedHashSet<>(
                Arrays.asList("0", "1", "2"));

        AtomicInteger selectionListenerCalled = new AtomicInteger();
        grid.addSelectionListener(event -> {
            selectionListenerCalled.incrementAndGet();
            Assertions.assertEquals(selection, event.getAllSelectedItems());
        });

        AtomicInteger valueChangeListenerCalled = new AtomicInteger();
        grid.asMultiSelect().addValueChangeListener(event -> {
            valueChangeListenerCalled.incrementAndGet();
            Assertions.assertEquals(oldSelection, event.getOldValue());
            Assertions.assertEquals(selection, event.getValue());
        });

        AtomicInteger multiSelectionListenerCalled = new AtomicInteger();
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .addMultiSelectionListener(event -> {
                    multiSelectionListenerCalled.incrementAndGet();
                    Assertions.assertEquals(oldSelection,
                            event.getOldSelection());
                    Assertions.assertEquals(selection, event.getValue());

                    Set<String> oldCopy = new LinkedHashSet<>(oldSelection);
                    Set<String> copy = new LinkedHashSet<>(selection);
                    oldCopy.removeAll(copy);
                    Assertions.assertEquals(oldCopy,
                            event.getRemovedSelection());
                    oldCopy = new LinkedHashSet<>(oldSelection);
                    copy.removeAll(oldCopy);
                    Assertions.assertEquals(copy, event.getAddedSelection());
                });

        grid.asMultiSelect().setValue(selection);

        oldSelection.addAll(selection);
        selection.clear();
        selection.addAll(Arrays.asList("10", "1", "5"));
        grid.asMultiSelect().setValue(selection);

        Assertions.assertEquals(2, selectionListenerCalled.get());
        Assertions.assertEquals(2, valueChangeListenerCalled.get());
        Assertions.assertEquals(2, multiSelectionListenerCalled.get());
    }

    @Test
    void multiSelect_discardSelectionOnDataChange_noExtraChangeEventsFired() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        multiSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assertions.assertTrue(grid.getSelectedItems().isEmpty());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void multiSelect_preserveExistingSelectionOnDataChange_noExtraChangeEventsFired() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        multiSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assertions.assertTrue(grid.getSelectedItems().isEmpty());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void multiSelect_preserveAllSelectionOnDataChange_noExtraChangeEventsFired() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.MULTI);
        MultiSelect<Grid<String>, String> multiSelect = grid.asMultiSelect();

        List<HasValue.ValueChangeEvent<Set<String>>> events = new ArrayList<>();
        multiSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_ALL);

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());
    }

    @Test
    void singleSelect_discardSelectionOnDataChange_noExtraChangeEventsFired() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.SINGLE);
        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        singleSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assertions.assertTrue(grid.getSelectedItems().isEmpty());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void singleSelect_preserveExistingSelectionOnDataChange_noExtraChangeEventsFired() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.SINGLE);
        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        singleSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assertions.assertTrue(grid.getSelectedItems().isEmpty());
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void singleSelect_preserveAllSelectionOnDataChange_noExtraChangeEventsFired() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionMode(SelectionMode.SINGLE);
        SingleSelect<Grid<String>, String> singleSelect = grid.asSingleSelect();

        List<HasValue.ValueChangeEvent<String>> events = new ArrayList<>();
        singleSelect.addValueChangeListener(events::add);

        List<String> items = new ArrayList<>(
                Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(items);

        grid.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_ALL);

        String selectedItem = items.get(0);
        grid.select(selectedItem);
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertEquals(1, events.size());
        events.clear();

        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(items.get(1));
        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());

        items.remove(selectedItem);
        grid.getDataProvider().refreshAll();
        Assertions.assertEquals(Set.of(selectedItem), grid.getSelectedItems());
        Assertions.assertTrue(events.isEmpty());
    }

    @Test
    void setLazyDataProvider_setPreserveExisting_throwsUnsupportedOperationException() {
        Grid<String> grid = new Grid<>();
        // Set another selection preservation mode first
        grid.setSelectionPreservationMode(SelectionPreservationMode.DISCARD);
        setLazyDataProvider(grid);
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> grid.setSelectionPreservationMode(
                        SelectionPreservationMode.PRESERVE_EXISTING));
    }

    @Test
    void setPreserveExisting_setLazyDataProvider_throwsUnsupportedOperationException() {
        Grid<String> grid = new Grid<>();
        grid.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> setLazyDataProvider(grid));
    }

    private static void setLazyDataProvider(Grid<String> grid) {
        BackEndDataProvider<String, Void> dataProvider = DataProvider
                .fromCallbacks(query -> {
                    query.getOffset();
                    query.getLimit();
                    return Stream.of("foo", "bar", "baz");
                }, query -> 3);
        grid.setDataProvider(dataProvider);
    }
}
