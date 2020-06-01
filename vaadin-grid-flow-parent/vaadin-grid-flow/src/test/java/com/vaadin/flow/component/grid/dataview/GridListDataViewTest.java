/*
 * Copyright 2000-2020 Vaadin Ltd.
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
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.AbstractListDataViewListenerTest;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.flow.data.provider.ListDataProvider;

public class GridListDataViewTest extends AbstractListDataViewListenerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void dataViewWithItem_rowOutsideSetRequested_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index 7 is outside of the accepted range '0 - 3'");

        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid
                .setDataProvider("one", "two", "three", "four");

        dataView.getItemOnRow(7);
    }

    @Test
    public void dataViewWithItem_negativeRowRequested_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index -7 is outside of the accepted range '0 - 3'");

        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid
                .setDataProvider("one", "two", "three", "four");

        dataView.getItemOnRow(-7);
    }

    @Test
    public void dataViewWithoutItems_exceptionThrown() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage("Requested index 5 on empty data.");

        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid
                .setDataProvider(new ListDataProvider<>(new ArrayList<>()));

        dataView.getItemOnRow(5);
    }

    @Test
    public void dataProviderOnSet_exceptionThrownForGetItems() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException
                .expectMessage("DataProvider collection is not a list.");

        Set<String> items = new HashSet<>();
        items.add("item1");
        items.add("item2");

        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid
                .setDataProvider(new ListDataProvider<>(items));

        dataView.getItems();
    }

    @Test
    public void dataViewWithItems_returnsExpectedItemsForMethods() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setDataProvider(items);

        // Test getItemOnRow returns correct item
        Assert.assertEquals("Wrong item returned for row", items[2],
                dataView.getItemOnRow(2));

        // Test hasNext-/-PreviousItem
        Assert.assertTrue("Item in middle should have next item",
                dataView.hasNextItem(items[2]));
        Assert.assertTrue("Item in middle should have previous item",
                dataView.hasPreviousItem(items[1]));

        Assert.assertTrue("First item should have next item",
                dataView.hasNextItem(items[0]));
        Assert.assertTrue("Last item should have previous item",
                dataView.hasPreviousItem(items[3]));

        Assert.assertFalse("No next item for last item should be available",
                dataView.hasNextItem(items[3]));
        Assert.assertFalse(
                "No previous item for first item should be available",
                dataView.hasPreviousItem(items[0]));

        // Test getNext-/-PreviousItem
        Assert.assertEquals("Faulty next item", items[3],
                dataView.getNextItem(items[2]));
        Assert.assertEquals("Faulty previous item", items[1],
                dataView.getPreviousItem(items[2]));

        Assert.assertNull("Got next item for last item",
                dataView.getNextItem(items[3]));
        Assert.assertNull("Got previous item for first index",
                dataView.getPreviousItem(items[0]));

        // Test getSize
        Assert.assertEquals("Unexpected size for data", items.length,
                dataView.getDataSize());

        // Test containsItem
        Assert.assertTrue("Set item was not found in the data",
                dataView.isItemPresent(items[3]));
        Assert.assertFalse("Non existent item found in data",
                dataView.isItemPresent("item6"));
    }

    @Test
    public void dataView_withFilter_methodsUseFilteredData() {
        String[] items = new String[] { "item1", "item2", "item3", "item4" };
        Grid<String> grid = new Grid<>();
        GridListDataView<String> dataView = grid.setDataProvider(items);

        dataView.withFilter(s -> s.endsWith("4"));

        Assert.assertEquals("Filter was not applied to data size", 1,
                dataView.getDataSize());

        Assert.assertTrue("Expected item is missing from filtered data",
                dataView.isItemPresent(items[3]));
        Assert.assertFalse(
                "Item that should be filtered out is available in the data",
                dataView.isItemPresent(items[1]));

        Assert.assertEquals("Wrong item on row for filtered data.", items[3],
                dataView.getItemOnRow(0));
    }

    @Override
    protected HasListDataView<String, ? extends AbstractListDataView<String>> getComponent() {
        return new Grid<>();
    }
}
