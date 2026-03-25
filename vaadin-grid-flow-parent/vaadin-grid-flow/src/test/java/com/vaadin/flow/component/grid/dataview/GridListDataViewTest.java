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
package com.vaadin.flow.component.grid.dataview;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.tests.dataprovider.AbstractListDataViewListenerTest;

class GridListDataViewTest extends AbstractListDataViewListenerTest {

    @Test
    void dataViewWithItem_rowOutsideSetRequested_exceptionThrown() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("one", "two", "three",
                "four");

        var ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {
                    dataView.getItem(7);
                });
        Assertions.assertTrue(ex.getMessage().contains(
                "Given index 7 is outside of the accepted range '0 - 3'"));
    }

    @Test
    void dataViewWithItem_negativeRowRequested_exceptionThrown() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("one", "two", "three",
                "four");

        var ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {
                    dataView.getItem(-7);
                });
        Assertions.assertTrue(ex.getMessage().contains(
                "Given index -7 is outside of the accepted range '0 - 3'"));
    }

    @Test
    void dataViewWithoutItems_exceptionThrown() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid
                .setItems(new ListDataProvider<>(new ArrayList<>()));

        var ex = Assertions.assertThrows(IndexOutOfBoundsException.class,
                () -> {
                    dataView.getItem(5);
                });
        Assertions.assertTrue(
                ex.getMessage().contains("Requested index 5 on empty data."));
    }

    @Test
    void dataViewWithItems_returnsExpectedItemsForMethods() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems(items);

        // Test getItem returns correct item
        Assertions.assertEquals(items[2], dataView.getItem(2),
                "Wrong item returned for row");

        // Test getNext-/-PreviousItem
        Assertions.assertEquals(items[3], dataView.getNextItem(items[2]).get(),
                "Faulty next item");
        Assertions.assertEquals(items[1],
                dataView.getPreviousItem(items[2]).get(),
                "Faulty previous item");

        Assertions.assertFalse(dataView.getNextItem(items[3]).isPresent(),
                "Got next item for last item");
        Assertions.assertFalse(dataView.getPreviousItem(items[0]).isPresent(),
                "Got previous item for first index");

        // Test getItemCount
        Assertions.assertEquals(items.length, dataView.getItemCount(),
                "Unexpected size for data");

        // Test containsItem
        Assertions.assertTrue(dataView.contains(items[3]),
                "Set item was not found in the data");
        Assertions.assertFalse(dataView.contains("item6"),
                "Non existent item found in data");

        // Test getItems
        Stream<String> allItems = dataView.getItems();
        Assertions.assertArrayEquals(items, allItems.toArray(),
                "Unexpected data set");
    }

    @Test
    void dataView_setFilter_methodsUseFilteredData() {
        AtomicInteger refreshed = new AtomicInteger(0);
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems(items);
        grid.getDataProvider().addDataProviderListener(e -> {
            refreshed.incrementAndGet();
        });

        dataView.setFilter(s -> s.endsWith("4"));
        Assertions.assertEquals(1, refreshed.get(),
                "Filter change did not fire DataChangeEvent");

        Assertions.assertEquals(1, dataView.getItemCount(),
                "Filter was not applied to data size");

        Assertions.assertTrue(dataView.contains(items[3]),
                "Expected item is missing from filtered data");
        Assertions.assertFalse(dataView.contains(items[1]),
                "Item that should be filtered out is available in the data");

        Assertions.assertEquals(items[3], dataView.getItem(0),
                "Wrong item on row for filtered data.");
    }

    @Test
    void dataViewWithItems_contains_returnsCorrectItems() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("first", "middle",
                "last");

        Assertions.assertTrue(dataView.contains("middle"),
                "Returned false for item that should exist");
    }

    @Test
    void contains_itemPresentedInDataSet_itemFound() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("first", "middle",
                "last");

        Assertions.assertTrue(dataView.contains("first"),
                "The item was not found in the data");

        dataView.setIdentifierProvider(item -> item.substring(0, 1));

        Assertions.assertTrue(dataView.contains("fourth"),
                "The item was not found in the data");
    }

    @Test
    void contains_itemNotPresentedInDataSet_itemNotFound() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("first", "middle",
                "last");
        Assertions.assertFalse(dataView.contains("absent item"),
                "Non existent item found in data");
    }

    @Override
    protected HasListDataView<String, ? extends AbstractListDataView<String>> getComponent() {
        return new Grid<>();
    }
}
