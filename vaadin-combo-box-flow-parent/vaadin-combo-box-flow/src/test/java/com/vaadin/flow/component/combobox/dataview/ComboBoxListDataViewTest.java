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

package com.vaadin.flow.component.combobox.dataview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.AbstractListDataViewListenerTest;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.HasListDataView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ComboBoxListDataViewTest extends AbstractListDataViewListenerTest {

    private List<String> items;
    private ComboBoxListDataView<String> dataView;
    private ComboBox<String> component;
    private DataCommunicatorTest.MockUI ui;

    @Before
    public void init() {
        items = new ArrayList<>(Arrays.asList("first", "middle", "last"));
        component = new ComboBox<>();
        ui = new DataCommunicatorTest.MockUI();
        ui.add(component);

        dataView = component.setItems(items);
    }

    @Test
    public void getItems_noFiltersSet_allItemsObtained() {
        Stream<String> allItems = dataView.getItems();
        Assert.assertArrayEquals("Unexpected data set", items.toArray(),
                allItems.toArray());
    }

    @Test
    public void getItems_withInMemoryAndTextFilter_itemsFilteredWithOnlyInMemoryFilter() {
        items = Arrays.asList("foo", "bar", "banana");
        dataView = component.setItems(items);

        ComboBoxDataViewTestHelper.setClientSideFilter(component, "ba");
        // Close combo box drop down to trigger the filter erase
        component.setOpened(false);
        ComboBoxDataViewTestHelper.fakeClientCommunication(ui);

        Stream<String> filteredItems = dataView.getItems();

        // Check that the client filter does not affect the item handling API
        // in data view
        Assert.assertArrayEquals("The client filter shouldn't impact the items",
                new String[] { "foo", "bar", "banana" },
                filteredItems.toArray());

        dataView.setFilter(item -> item.length() == 3);
        filteredItems = dataView.getItems();
        Assert.assertArrayEquals("Unexpected data set after in-memory filter",
                new String[] { "foo", "bar" }, filteredItems.toArray());

        // Remove the filters
        dataView.removeFilters();
        filteredItems = dataView.getItems();
        Assert.assertArrayEquals(
                "Unexpected data set after removing in-memory filter",
                new String[] { "foo", "bar", "banana" },
                filteredItems.toArray());
    }

    @Test
    public void getItemCount_noFilters_totalItemsCountObtained() {
        Assert.assertEquals("Unexpected size for data", items.size(),
                dataView.getItemCount());
    }

    @Test
    public void getItemCount_withInMemoryAndTextFilter_itemsFilteredWithOnlyInMemoryFilter() {
        items = Arrays.asList("foo", "bar", "banana");
        dataView = component.setItems(items);

        ComboBoxDataViewTestHelper.setClientSideFilter(component, "ba");
        // Close combo box drop down to trigger the filter erase
        component.setOpened(false);
        ComboBoxDataViewTestHelper.fakeClientCommunication(ui);

        int itemCount = dataView.getItemCount();

        // Check that the client filter does not affect the item count
        Assert.assertEquals(
                "The client filter shouldn't impact the items count", 3,
                itemCount);

        dataView.setFilter(item -> item.length() == 3);
        itemCount = dataView.getItemCount();
        Assert.assertEquals("Unexpected item count after server side filter", 2,
                itemCount);

        // Remove the filters
        dataView.removeFilters();
        itemCount = dataView.getItemCount();
        Assert.assertEquals(
                "Unexpected item count after removing server side filter", 3,
                itemCount);
    }

    @Test
    public void setIdentifierProvider_customIdentifier_keyMapperUsesIdentifier() {
        Item first = new Item(1L, "first");
        Item second = new Item(2L, "middle");

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));

        ComboBox<Item> component = new ComboBox<>();

        DataCommunicator<Item> dataCommunicator = new DataCommunicator<>(
                (item, jsonObject) -> {
                }, null, null, component.getElement().getNode());

        ComboBoxListDataView<Item> dataView = new ComboBoxListDataView<>(
                dataCommunicator, component, (filter, sorting) -> {
                });
        DataKeyMapper<Item> keyMapper = dataCommunicator.getKeyMapper();
        items.forEach(keyMapper::key);

        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(Item::getId);
        Assert.assertTrue(keyMapper.has(new Item(1L, "non-present")));
    }

    @Override
    protected HasListDataView<String, ComboBoxListDataView<String>> getComponent() {
        return new ComboBox<>();
    }

    private static class Item {
        private long id;
        private String value;

        public Item(long id) {
            this.id = id;
        }

        public Item(long id, String value) {
            this.id = id;
            this.value = value;
        }

        public long getId() {
            return id;
        }

        public String getValue() {
            return value;
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Item item = (Item) o;
            return id == item.id && Objects.equals(value, item.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, value);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}
