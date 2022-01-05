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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.data.provider.CustomInMemoryDataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.provider.AbstractComponentDataViewTest;
import com.vaadin.flow.data.provider.DataCommunicator;
import com.vaadin.flow.data.provider.DataCommunicatorTest;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.DataProviderListener;
import com.vaadin.flow.data.provider.DataView;
import com.vaadin.flow.data.provider.IdentifierProvider;
import com.vaadin.flow.data.provider.InMemoryDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;

public class ComboBoxDataViewTest extends AbstractComponentDataViewTest {

    private ComboBox<String> comboBox;
    private DataCommunicatorTest.MockUI ui;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
        items = new ArrayList<>(Arrays.asList("first", "middle", "last"));
        dataProvider = new CustomInMemoryDataProvider<>(items);
        comboBox = getComponent();
        ui = new DataCommunicatorTest.MockUI();
        ui.add(comboBox);
        dataView = comboBox.setItems(dataProvider, textFilter -> item -> true);
        component = comboBox;
    }

    @Test
    public void dataViewWithItems_getItem_returnsCorrectItem() {
        Assert.assertEquals(items.get(0), dataView.getItem(0));
        Assert.assertEquals(items.get(1), dataView.getItem(1));
        Assert.assertEquals(items.get(2), dataView.getItem(2));
    }

    @Test
    public void setIdentifierProvider_customIdentifier_keyMapperUsesIdentifier() {
        Item first = new Item(1L, "first");
        Item second = new Item(2L, "middle");

        List<Item> items = new ArrayList<>(Arrays.asList(first, second));
        ComboBox<Item> comboBox = new ComboBox<>();

        // We create a generic data provider to test the identity
        // handling behavior in generic data view
        DataProvider<Item, String> dataProvider = new DataProvider<Item, String>() {
            @Override
            public boolean isInMemory() {
                return true;
            }

            @Override
            public int size(Query<Item, String> query) {
                return 2;
            }

            @Override
            public Stream<Item> fetch(Query<Item, String> query) {
                return Stream.of(first, second);
            }

            @Override
            public void refreshItem(Item item) {

            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(
                    DataProviderListener<Item> listener) {
                return null;
            }
        };

        DataCommunicator<Item> dataCommunicator = new DataCommunicator<>(
                (item, jsonObject) -> {
                }, null, null, comboBox.getElement().getNode());
        dataCommunicator.setDataProvider(dataProvider, null);

        // Generic combo box data view
        DataView<Item> dataView = new ComboBoxDataView<>(dataCommunicator,
                comboBox);

        DataKeyMapper<Item> keyMapper = dataCommunicator.getKeyMapper();
        items.forEach(keyMapper::key);

        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(Item::getId);
        Assert.assertTrue(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(IdentifierProvider.identity());
        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));

        // In-memory combo box data view
        dataCommunicator.setDataProvider(DataProvider.ofCollection(items),
                null);
        dataView = new ComboBoxListDataView<>(dataCommunicator, comboBox,
                (filter, sorting) -> {
                });
        // We need to repopulate the keyMapper after setting a new data provider
        items.forEach(keyMapper::key);

        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(Item::getId);
        Assert.assertTrue(keyMapper.has(new Item(1L, "non-present")));
        dataView.setIdentifierProvider(IdentifierProvider.identity());
        Assert.assertFalse(keyMapper.has(new Item(1L, "non-present")));
    }

    @Test
    public void setInMemoryDataProvider_convertsToGenericDataProviderAndAppliesFilterCorrectly() {
        final String[] items = { "bar", "banana", "iguana" };

        ComboBox<String> comboBox = Mockito.spy(new ComboBox<>());

        InMemoryDataProvider<String> inMemoryDataProvider = new InMemoryDataProvider<String>() {

            private SerializablePredicate<String> filter;

            @Override
            public int size(
                    Query<String, SerializablePredicate<String>> query) {
                Assert.assertTrue(query.getFilter().isPresent());
                return (int) Stream.of(items).filter(query.getFilter().get())
                        .count();
            }

            @Override
            public Stream<String> fetch(
                    Query<String, SerializablePredicate<String>> query) {
                Assert.assertTrue(query.getFilter().isPresent());
                return Stream.of(items).filter(query.getFilter().get());
            }

            @Override
            public void refreshItem(String item) {

            }

            @Override
            public void refreshAll() {

            }

            @Override
            public Registration addDataProviderListener(
                    DataProviderListener<String> listener) {
                return null;
            }

            @Override
            public SerializablePredicate<String> getFilter() {
                return filter;
            }

            @Override
            public void setFilter(SerializablePredicate<String> filter) {
                this.filter = filter;
            }

            @Override
            public SerializableComparator<String> getSortComparator() {
                return null;
            }

            @Override
            public void setSortComparator(
                    SerializableComparator<String> comparator) {

            }
        };

        // This captor will catch the generic data provider with string filter
        ArgumentCaptor<DataProvider> dataProviderCaptor = ArgumentCaptor
                .forClass(DataProvider.class);

        // Set IMDP and set the converter for combobox text filter
        ComboBoxDataView<String> dataView = comboBox.setItems(
                inMemoryDataProvider,
                textFilter -> item -> item.startsWith(textFilter));

        // Open the comboBox explicitly, because it is not initialized
        // eagerly with in-memory data provider
        comboBox.setOpened(true);

        // We expect that the current implementation of 'setItems' with IMDP
        // will delegate to 'setItems(DataProvider)'
        Mockito.verify(comboBox).setItems(dataProviderCaptor.capture());

        // Verify the predicate filter always returns true and passes all items
        Assert.assertArrayEquals(items, dataView.getItems().toArray());

        // Now set the predicate and verify it goes to query parameter
        inMemoryDataProvider.setFilter(item -> item.length() == 6);
        Assert.assertArrayEquals(new String[] { "banana", "iguana" },
                dataView.getItems().toArray());

        // Finally set the text filter to the ComboBox and fetch the items
        DataProvider<String, String> genericDataProvider = dataProviderCaptor
                .getValue();

        List<String> filteredItems = genericDataProvider
                .fetch(new Query<>(0, Integer.MAX_VALUE, null, null, "ba"))
                .collect(Collectors.toList());

        // The result should contain an intersection of two filters (text and
        // predicate), so the result should start with 'ba' and contain 6 chars.
        Assert.assertEquals(1, filteredItems.size());
        Assert.assertEquals("banana", filteredItems.get(0));
    }

    @Test
    public void setInMemoryDataProviderWithNoConverter_throws() {
        expectedException.expect(UnsupportedOperationException.class);
        expectedException
                .expectMessage(String.format("ComboBox does not support "
                        + "setting a custom in-memory data provider without "
                        + "knowledge of the rules on how to convert internal text filter "
                        + "into a predicate applied to the data provider. Please use%n"
                        + "setItems(InMemoryDataProvider<T>, SerializableFunction<String, "
                        + "SerializablePredicate<T>>)"
                        + "%noverloaded method instead"));
        component.setItems(dataProvider);
    }

    @Test
    public void getItems_noClientSideFilter_returnsNotFilteredItems() {
        Stream<String> filteredItems = dataView.getItems();

        Assert.assertArrayEquals("Unexpected items obtained",
                new String[] { "first", "middle", "last" },
                filteredItems.toArray());
    }

    @Test
    public void getItems_withClientSideFilter_returnsNotFilteredItems() {
        ComboBoxDataViewTestHelper.setClientSideFilter(comboBox, "middle");
        ComboBoxDataViewTestHelper.fakeClientCommunication(ui);

        Stream<String> filteredItems = dataView.getItems();

        // Check that the client filter does not affect the item handling API
        // in data view
        Assert.assertArrayEquals("The client filter shouldn't impact the items",
                new String[] { "first", "middle", "last" },
                filteredItems.toArray());

        // Reset the client filter and check again
        ComboBoxDataViewTestHelper.setClientSideFilter(comboBox, "");
        ComboBoxDataViewTestHelper.fakeClientCommunication(ui);
        filteredItems = dataView.getItems();
        Assert.assertArrayEquals(
                "The client filter reset shouldn't impact the items",
                new String[] { "first", "middle", "last" },
                filteredItems.toArray());
    }

    @Test
    public void getItem_noClientSideFilter_returnsItemFromNotFilteredSet() {
        Assert.assertEquals("Invalid item on index 1", "middle",
                dataView.getItem(1));
    }

    @Test
    public void getItem_withClientSideFilter_returnsItemFromNotFilteredSet() {
        ComboBoxDataViewTestHelper.setClientSideFilter(comboBox, "middle");
        ComboBoxDataViewTestHelper.fakeClientCommunication(ui);
        Assert.assertEquals("Invalid item on index 0", "first",
                dataView.getItem(0));
    }

    @Test
    public void getItem_negativeIndex_throws() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage("Index must be non-negative");
        dataView.getItem(-1);
    }

    @Test
    public void getItem_emptyData_throws() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage("Requested index 0 on empty data.");
        dataView = comboBox.setItems();

        dataView.getItem(0);
    }

    @Test
    public void getItem_outsideOfRange_throws() {
        expectedException.expect(IndexOutOfBoundsException.class);
        expectedException.expectMessage(
                "Given index 3 should be less than the item count '3'");
        dataView.getItem(3);
    }

    @Override
    protected ComboBox<String> getComponent() {
        return new ComboBox<>();
    }
}
