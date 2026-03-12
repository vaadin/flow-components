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
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.combobox.dataview.ComboBoxDataView;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.signals.BindingActiveException;
import com.vaadin.flow.signals.local.ListSignal;
import com.vaadin.flow.signals.local.ValueSignal;
import com.vaadin.tests.AbstractSignalsUnitTest;

public class ComboBoxSignalTest extends AbstractSignalsUnitTest {

    private ComboBox<String> comboBox;

    @Before
    public void setup() {
        comboBox = new ComboBox<>();
    }

    @Test
    public void bindItems_defaultItemsFilter_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>("Alice");
        var item2Signal = new ValueSignal<>("Bob");
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        ComboBoxDataView<String> dataView = comboBox.bindItems(listSignal);
        ui.add(comboBox);

        Assert.assertNotNull("Data view should not be null", dataView);
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("Alice", items.get(0));
        Assert.assertEquals("Bob", items.get(1));
    }

    @Test
    public void bindItems_defaultItemsFilter_updatesWhenListSignalChanges() {
        var item1Signal = new ValueSignal<>("Alice");
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        comboBox.bindItems(listSignal);
        ui.add(comboBox);

        ComboBoxDataView<String> dataView = comboBox.getGenericDataView();
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals(1, items.size());

        var item2Signal = new ValueSignal<>("Bob");
        var item3Signal = new ValueSignal<>("Charlie");
        listSignal.set(List.of(item1Signal, item2Signal, item3Signal));

        items = dataView.getItems().toList();
        Assert.assertEquals(3, items.size());
    }

    @Test
    public void bindItems_defaultItemsFilter_updatesWhenItemSignalChanges() {
        var item1Signal = new ValueSignal<>("Alice");
        var listSignal = new ValueSignal<>(List.of(item1Signal));

        comboBox.bindItems(listSignal);
        ui.add(comboBox);

        ComboBoxDataView<String> dataView = comboBox.getGenericDataView();
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals("Alice", items.getFirst());

        item1Signal.set("Updated Alice");

        items = dataView.getItems().toList();
        Assert.assertEquals("Updated Alice", items.getFirst());
    }

    @Test(expected = NullPointerException.class)
    public void bindItems_nullSignal_throws() {
        comboBox.bindItems(null);
    }

    @Test
    public void bindItems_customItemsFilter_setsItemsFromSignal() {
        var item1Signal = new ValueSignal<>("Alice");
        var item2Signal = new ValueSignal<>("Bob");
        var listSignal = new ValueSignal<>(List.of(item1Signal, item2Signal));

        ComboBoxDataView<String> dataView = comboBox.bindItems(listSignal,
                filter -> item -> item.toLowerCase()
                        .contains(filter.toLowerCase()));
        ui.add(comboBox);

        Assert.assertNotNull("Data view should not be null", dataView);
        List<String> items = dataView.getItems().toList();
        Assert.assertEquals(2, items.size());
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithCollection_throws() {
        var comboBox = createComboBoxWithBoundItems();
        comboBox.setItems(Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithItemFilterAndCollection_throws() {
        var comboBox = createComboBoxWithBoundItems();
        ComboBox.ItemFilter<String> itemFilter = (item, filterText) -> item
                .toLowerCase().contains(filterText.toLowerCase());
        comboBox.setItems(itemFilter,
                Arrays.asList("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithItemFilterAndVarargs_throws() {
        var comboBox = createComboBoxWithBoundItems();
        ComboBox.ItemFilter<String> itemFilter = (item, filterText) -> item
                .toLowerCase().contains(filterText.toLowerCase());
        comboBox.setItems(itemFilter, "New Item 1", "New Item 2");
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithItemFilterAndListDataProvider_throws() {
        var comboBox = createComboBoxWithBoundItems();
        ComboBox.ItemFilter<String> itemFilter = (item, filterText) -> item
                .toLowerCase().contains(filterText.toLowerCase());
        ListDataProvider<String> dataProvider = new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2"));
        comboBox.setItems(itemFilter, dataProvider);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithListDataProvider_throws() {
        var comboBox = createComboBoxWithBoundItems();
        comboBox.setItems(new ListDataProvider<>(
                Arrays.asList("New Item 1", "New Item 2")));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithFetchCallback_throws() {
        var comboBox = createComboBoxWithBoundItems();
        CallbackDataProvider.FetchCallback<String, String> fetchCallback = query -> Stream
                .of("New Item 1", "New Item 2");
        comboBox.setItems(fetchCallback);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithFetchAndCountCallback_throws() {
        var comboBox = createComboBoxWithBoundItems();
        CallbackDataProvider.FetchCallback<String, String> fetchCallback = query -> Stream
                .of("New Item 1", "New Item 2");
        CallbackDataProvider.CountCallback<String, String> countCallback = query -> 2;
        comboBox.setItems(fetchCallback, countCallback);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithDataProviderString_throws() {
        var comboBox = createComboBoxWithBoundItems();
        comboBox.setItems(DataProvider.ofItems("New Item 1", "New Item 2"));
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetItemsWithInMemoryDataProviderAndFilterConverter_throws() {
        var comboBox = createComboBoxWithBoundItems();
        SerializableFunction<String, SerializablePredicate<String>> filterConverter = filterText -> item -> item
                .toLowerCase().contains(filterText.toLowerCase());
        comboBox.setItems(
                DataProvider.ofCollection(
                        Arrays.asList("New Item 1", "New Item 2")),
                filterConverter);
    }

    @Test(expected = BindingActiveException.class)
    public void bindItems_thenSetDataProviderWithFetchItemsCallback_throws() {
        var comboBox = createComboBoxWithBoundItems();
        ComboBox.FetchItemsCallback<String> fetchItems = (filter, offset,
                limit) -> Stream.of("New Item 1", "New Item 2");
        SerializableFunction<String, Integer> sizeCallback = filter -> 2;
        comboBox.setDataProvider(fetchItems, sizeCallback);
    }

    private ComboBox<String> createComboBoxWithBoundItems() {
        var comboBox = new ComboBox<String>();
        var itemsSignal = new ListSignal<String>();
        itemsSignal.insertLast("Item 1");
        itemsSignal.insertLast("Item 2");
        comboBox.bindItems(itemsSignal);
        ui.add(comboBox);
        return comboBox;
    }
}
