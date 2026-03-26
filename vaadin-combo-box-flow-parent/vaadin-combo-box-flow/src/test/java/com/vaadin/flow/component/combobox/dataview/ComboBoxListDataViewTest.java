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
package com.vaadin.flow.component.combobox.dataview;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.HasListDataView;
import com.vaadin.tests.dataprovider.AbstractListDataViewListenerTest;

class ComboBoxListDataViewTest extends AbstractListDataViewListenerTest {
    private List<String> items;
    private ComboBoxListDataView<String> dataView;
    private ComboBox<String> component;

    @BeforeEach
    void init() {
        items = new ArrayList<>(Arrays.asList("first", "middle", "last"));
        component = new ComboBox<>();
        ui.add(component);

        dataView = component.setItems(items);
    }

    @Test
    void getItems_noFiltersSet_allItemsObtained() {
        Stream<String> allItems = dataView.getItems();
        Assertions.assertArrayEquals(items.toArray(), allItems.toArray(),
                "Unexpected data set");
    }

    @Test
    void getItems_withInMemoryAndTextFilter_itemsFilteredWithOnlyInMemoryFilter() {
        items = Arrays.asList("foo", "bar", "banana");
        dataView = component.setItems(items);

        setClientFilter(component, "ba");
        // Close combo box drop down to trigger the filter erase
        component.setOpened(false);
        ui.fakeClientCommunication();

        Stream<String> filteredItems = dataView.getItems();

        // Check that the client filter does not affect the item handling API
        // in data view
        Assertions.assertArrayEquals(new String[] { "foo", "bar", "banana" },
                filteredItems.toArray(),
                "The client filter shouldn't impact the items");

        dataView.setFilter(item -> item.length() == 3);
        filteredItems = dataView.getItems();
        Assertions.assertArrayEquals(new String[] { "foo", "bar" },
                filteredItems.toArray(),
                "Unexpected data set after in-memory filter");

        // Remove the filters
        dataView.removeFilters();
        filteredItems = dataView.getItems();
        Assertions.assertArrayEquals(new String[] { "foo", "bar", "banana" },
                filteredItems.toArray(),
                "Unexpected data set after removing in-memory filter");
    }

    @Test
    void getItemCount_noFilters_totalItemsCountObtained() {
        Assertions.assertEquals(items.size(), dataView.getItemCount(),
                "Unexpected size for data");
    }

    @Test
    void getItemCount_withInMemoryAndTextFilter_itemsFilteredWithOnlyInMemoryFilter() {
        items = Arrays.asList("foo", "bar", "banana");
        dataView = component.setItems(items);

        setClientFilter(component, "ba");
        // Close combo box drop down to trigger the filter erase
        component.setOpened(false);
        ui.fakeClientCommunication();

        int itemCount = dataView.getItemCount();

        // Check that the client filter does not affect the item count
        Assertions.assertEquals(3, itemCount,
                "The client filter shouldn't impact the items count");

        dataView.setFilter(item -> item.length() == 3);
        itemCount = dataView.getItemCount();
        Assertions.assertEquals(2, itemCount,
                "Unexpected item count after server side filter");

        // Remove the filters
        dataView.removeFilters();
        itemCount = dataView.getItemCount();
        Assertions.assertEquals(3, itemCount,
                "Unexpected item count after removing server side filter");
    }

    @Test
    void setIdentifierProvider_customIdentifier_keyMapperUsesIdentifier() {
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

        Assertions.assertFalse(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(Item::getId);
        Assertions.assertTrue(keyMapper.has(new Item(1L, "non-present")));
    }

    @Override
    protected HasListDataView<String, ComboBoxListDataView<String>> getComponent() {
        return new ComboBox<>();
    }

    private void setClientFilter(ComboBox<String> comboBox,
            String clientFilter) {
        try {
            // Reset the client filter on server side as though it's sent from
            // client
            Method setViewportRangeMethod = ComboBoxBase.class
                    .getDeclaredMethod("setViewportRange", int.class, int.class,
                            String.class);
            setViewportRangeMethod.setAccessible(true);
            setViewportRangeMethod.invoke(comboBox, 0, comboBox.getPageSize(),
                    clientFilter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
