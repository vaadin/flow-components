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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.shared.SelectionPreservationMode;
import com.vaadin.flow.data.selection.SelectionListener;

class GridSelectionWithIdentifierProviderTest {

    // Record equals compares all components, so two instances with the same
    // ID but a different name are only the same item by identifier
    private record Item(long id, String name) {
    }

    private List<Item> items;
    private Grid<Item> grid;
    private AtomicInteger selectionEventCount;

    @BeforeEach
    void setup() {
        items = new ArrayList<>(List.of(new Item(1, "A"), new Item(2, "B")));
        grid = new Grid<>();
        selectionEventCount = new AtomicInteger();
    }

    @Test
    void singleSelect_listDataView_instanceWithSameId_isSelected() {
        grid.setItems(items).setIdentifierProvider(Item::id);
        grid.select(items.get(0));

        assertTrue(grid.getSelectionModel()
                .isSelected(new Item(1, "A (reloaded)")));
        assertFalse(grid.getSelectionModel()
                .isSelected(new Item(2, "A (reloaded)")));
    }

    @Test
    void singleSelect_lazyDataView_instanceWithSameId_isSelected() {
        grid.setItems(query -> items.stream().skip(query.getOffset())
                .limit(query.getLimit())).setIdentifierProvider(Item::id);
        grid.select(items.get(0));

        assertTrue(grid.getSelectionModel()
                .isSelected(new Item(1, "A (reloaded)")));
    }

    @Test
    void singleSelect_selectInstanceWithSameId_noSelectionEvent() {
        grid.setItems(items).setIdentifierProvider(Item::id);
        grid.select(items.get(0));
        addSelectionListener();

        grid.select(new Item(1, "A (reloaded)"));
        grid.getSelectionModel().selectFromClient(new Item(1, "A (reloaded)"));

        assertEquals(0, selectionEventCount.get());
    }

    @Test
    void singleSelect_deselectInstanceWithSameId_nothingSelected() {
        grid.setItems(items).setIdentifierProvider(Item::id);
        grid.select(items.get(0));

        grid.deselect(new Item(1, "A (reloaded)"));

        assertTrue(grid.getSelectedItems().isEmpty());
    }

    @Test
    void multiSelect_selectInstanceWithSameId_oneItemSelected() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setItems(items).setIdentifierProvider(Item::id);
        grid.select(items.get(0));
        addSelectionListener();

        grid.select(new Item(1, "A (reloaded)"));
        grid.getSelectionModel().selectFromClient(new Item(1, "A (reloaded)"));

        assertEquals(1, grid.getSelectedItems().size());
        assertTrue(grid.getSelectionModel()
                .isSelected(new Item(1, "A (reloaded)")));
        assertEquals(0, selectionEventCount.get());
    }

    @Test
    void multiSelect_deselectInstanceWithSameId_nothingSelected() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setItems(items).setIdentifierProvider(Item::id);
        grid.select(items.get(0));

        grid.deselect(new Item(1, "A (reloaded)"));

        assertTrue(grid.getSelectedItems().isEmpty());
    }

    @Test
    void multiSelect_setIdentifierProviderAfterSelection_selectionKept() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setItems(items);
        grid.select(items.get(0));

        grid.getListDataView().setIdentifierProvider(Item::id);

        assertEquals(1, grid.getSelectedItems().size());
        assertSame(items.get(0), grid.getSelectedItems().iterator().next());
        assertTrue(grid.getSelectionModel()
                .isSelected(new Item(1, "A (reloaded)")));
        grid.deselect(new Item(1, "A (reloaded)"));
        assertTrue(grid.getSelectedItems().isEmpty());
    }

    @Test
    void preserveExisting_refreshWithNewInstances_selectionKept() {
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setSelectionPreservationMode(
                SelectionPreservationMode.PRESERVE_EXISTING);
        grid.setItems(items).setIdentifierProvider(Item::id);
        grid.select(items.get(0));

        items.set(0, new Item(1, "A (reloaded)"));
        items.set(1, new Item(2, "B (reloaded)"));
        grid.getDataProvider().refreshAll();

        assertEquals(1, grid.getSelectedItems().size());
        assertTrue(grid.getSelectionModel().isSelected(items.get(0)));
    }

    @Test
    void noIdentifierProvider_instanceWithSameId_notSelected() {
        grid.setItems(items);
        grid.select(items.get(0));

        assertFalse(grid.getSelectionModel()
                .isSelected(new Item(1, "A (reloaded)")));
    }

    private void addSelectionListener() {
        SelectionListener<Grid<Item>, Item> listener = e -> selectionEventCount
                .incrementAndGet();
        grid.addSelectionListener(listener);
    }
}
