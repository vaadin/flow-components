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
package com.vaadin.flow.component.combobox;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.tests.MockUIRule;

public class ComboBoxBindItemsTest {

    @Rule
    public final MockUIRule ui = new MockUIRule();

    private ComboBox<String> comboBox;

    @Before
    public void setup() {
        comboBox = new ComboBox<>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        comboBox.bindItems(itemsSignal);
        ui.getUI().add(comboBox);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        comboBox.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithItemFilterAndCollection_throws() {
        ComboBox.ItemFilter<String> itemFilter = (item, filterText) -> item
                .toLowerCase().contains(filterText.toLowerCase());
        comboBox.setItems(itemFilter,
                Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithItemFilterAndVarargs_throws() {
        ComboBox.ItemFilter<String> itemFilter = (item, filterText) -> item
                .toLowerCase().contains(filterText.toLowerCase());
        comboBox.setItems(itemFilter, "New Item 1", "New Item 2");
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithItemFilterAndListDataProvider_throws() {
        ComboBox.ItemFilter<String> itemFilter = (item, filterText) -> item
                .toLowerCase().contains(filterText.toLowerCase());
        ListDataProvider<String> dataProvider = new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2"));
        comboBox.setItems(itemFilter, dataProvider);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        comboBox.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithFetchCallback_throws() {
        CallbackDataProvider.FetchCallback<String, String> fetchCallback = query -> Stream
                .of("New Item 1", "New Item 2");
        comboBox.setItems(fetchCallback);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithFetchAndCountCallback_throws() {
        CallbackDataProvider.FetchCallback<String, String> fetchCallback = query -> Stream
                .of("New Item 1", "New Item 2");
        CallbackDataProvider.CountCallback<String, String> countCallback = query -> 2;
        comboBox.setItems(fetchCallback, countCallback);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProviderString_throws() {
        comboBox.setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProviderAndFilterConverter_throws() {
        SerializableFunction<String, SerializablePredicate<String>> filterConverter = filterText -> item -> item
                .toLowerCase().contains(filterText.toLowerCase());
        comboBox.setItems(
                DataProvider.ofCollection(
                        Arrays.asList("New Item 1", "New Item 2")),
                filterConverter);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProviderWithFetchItemsCallback_throws() {
        ComboBox.FetchItemsCallback<String> fetchItems = (filter, offset,
                limit) -> Stream.of("New Item 1", "New Item 2");
        SerializableFunction<String, Integer> sizeCallback = filter -> 2;
        comboBox.setDataProvider(fetchItems, sizeCallback);
    }
}
