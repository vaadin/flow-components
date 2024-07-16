/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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

@Route("vaadin-combo-box/filtering")
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
                                if (query.getFilter().isPresent() && query
                                        .getFilter().get().length() > 0) {
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

        ComboBox<String> comboBoxWithEmptyFilterReturnsNone = new ComboBox<>();
        comboBoxWithEmptyFilterReturnsNone.setDataProvider(
                (filter, offset, limit) -> IntStream
                        .range(offset, offset + limit)
                        .mapToObj(i -> "Item " + i),
                filter -> filter.isEmpty() ? 0 : 1);
        comboBoxWithEmptyFilterReturnsNone.setId("empty-filter-returns-none");
        add(new Div(), comboBoxWithEmptyFilterReturnsNone);
    }

}
