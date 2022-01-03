/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.AbstractListDataViewListenerTest;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GridListDataViewTest extends AbstractListDataViewListenerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void dataViewWithItem_rowOutsideSetRequested_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index 7 is outside of the accepted range '0 - 3'");

        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("one", "two", "three",
                "four");

        dataView.getItem(7);
    }

    @Test
    public void dataViewWithItem_negativeRowRequested_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index -7 is outside of the accepted range '0 - 3'");

        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("one", "two", "three",
                "four");

        dataView.getItem(-7);
    }

    @Test
    public void dataViewWithoutItems_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage("Requested index 5 on empty data.");

        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid
                .setItems(new ListDataProvider<>(new ArrayList<>()));

        dataView.getItem(5);
    }

    @Test
    public void dataViewWithItems_returnsExpectedItemsForMethods() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems(items);

        // Test getItem returns correct item
        Assert.assertEquals("Wrong item returned for row", items[2],
                dataView.getItem(2));

        // Test getNext-/-PreviousItem
        Assert.assertEquals("Faulty next item", items[3],
                dataView.getNextItem(items[2]).get());
        Assert.assertEquals("Faulty previous item", items[1],
                dataView.getPreviousItem(items[2]).get());

        Assert.assertFalse("Got next item for last item",
                dataView.getNextItem(items[3]).isPresent());
        Assert.assertFalse("Got previous item for first index",
                dataView.getPreviousItem(items[0]).isPresent());

        // Test getItemCount
        Assert.assertEquals("Unexpected size for data", items.length,
                dataView.getItemCount());

        // Test containsItem
        Assert.assertTrue("Set item was not found in the data",
                dataView.contains(items[3]));
        Assert.assertFalse("Non existent item found in data",
                dataView.contains("item6"));

        // Test getItems
        Stream<String> allItems = dataView.getItems();
        Assert.assertArrayEquals("Unexpected data set", items,
                allItems.toArray());
    }

    @Test
    public void dataView_setFilter_methodsUseFilteredData() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems(items);

        dataView.setFilter(s -> s.endsWith("4"));

        Assert.assertEquals("Filter was not applied to data size", 1,
                dataView.getItemCount());

        Assert.assertTrue("Expected item is missing from filtered data",
                dataView.contains(items[3]));
        Assert.assertFalse(
                "Item that should be filtered out is available in the data",
                dataView.contains(items[1]));

        Assert.assertEquals("Wrong item on row for filtered data.", items[3],
                dataView.getItem(0));
    }

    @Test
    public void dataViewWithItems_contains_returnsCorrectItems() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("first", "middle",
                "last");

        Assert.assertTrue("Returned false for item that should exist",
                dataView.contains("middle"));
    }

    @Test
    public void contains_itemPresentedInDataSet_itemFound() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("first", "middle",
                "last");

        Assert.assertTrue("The item was not found in the data",
                dataView.contains("first"));

        dataView.setIdentifierProvider(item -> item.substring(0, 1));

        Assert.assertTrue("The item was not found in the data",
                dataView.contains("fourth"));
    }

    @Test
    public void contains_itemNotPresentedInDataSet_itemNotFound() {
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setItems("first", "middle",
                "last");
        Assert.assertFalse("Non existent item found in data",
                dataView.contains("absent item"));
    }

    @Override
    protected HasListDataView<String, ? extends AbstractListDataView<String>> getComponent() {
        return new Grid<>();
    }
}
