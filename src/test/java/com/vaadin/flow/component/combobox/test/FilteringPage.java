/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.ComboBox.ItemFilter;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;

@Route("filtering")
public class FilteringPage extends Div {

    private Div message = new Div();

    public FilteringPage() {
        message.setId("message");
        add(message);

        ComboBox<String> comboBox = new ComboBox<>();

        List<String> items = LazyLoadingPage.generateStrings(40);
        ListDataProvider<String> dp = DataProvider.ofCollection(items);
        comboBox.setDataProvider(dp);

        comboBox.addValueChangeListener(e -> message.setText(e.getValue()));

        NativeButton addButton = new NativeButton("Add 20 items", e -> {
            List<String> newItems = LazyLoadingPage
                    .generateStrings(items.size() + 20)
                    .subList(items.size(), items.size() + 20);
            items.addAll(newItems);
            dp.refreshAll();
        });
        addButton.setId("add-items");

        NativeButton removeButton = new NativeButton("Remove 20 items", e -> {
            items.removeAll(items.subList(items.size() - 20, items.size()));
            dp.refreshAll();
        });
        removeButton.setId("remove-items");

        NativeButton itemFilterButton = new NativeButton(
                "Set items with ItemFilter", e -> {
                    ItemFilter<String> itemFilter = (item, filter) -> item
                            .startsWith(filter);
                    comboBox.setItems(itemFilter, items);
                });
        itemFilterButton.setId("item-filter");

        add(comboBox, addButton, removeButton, itemFilterButton);

        ComboBox<String> pageSizeBox = new ComboBox<>(60);
        pageSizeBox.setDataProvider(dp);
        pageSizeBox.setLabel("Page size 60");
        pageSizeBox.setId("page-size-60");
        add(new Div(), pageSizeBox);

        ComboBox<String> comboBoxWithFilterableDataProvider = new ComboBox<>();
        comboBoxWithFilterableDataProvider.setId("filterable-data-provider");
        comboBoxWithFilterableDataProvider
                .setLabel("Filter configured in the data provider");
        CallbackDataProvider<String, String> dataProviderWithFiltering = DataProvider
                .fromFilteringCallbacks(query -> {
                    return IntStream
                            .range(query.getOffset(),
                                    query.getOffset() + query.getLimit())
                            .mapToObj(i -> {
                                if (query.getFilter().isPresent()) {
                                    return "filtered";
                                } else {
                                    return "foo";
                                }
                            });
                }, query -> {
                    return 1;
                });
        comboBoxWithFilterableDataProvider
                .setDataProvider(dataProviderWithFiltering);
        add(new Div(), comboBoxWithFilterableDataProvider);
    }

}
