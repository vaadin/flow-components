/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/changing-data-size")
public class ChangingDataSizePage extends Div {

    private List<String> items;
    private NativeButton removeItems;

    public ChangingDataSizePage() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        grid.addColumn(s -> s);

        items = IntStream.range(0, 133).mapToObj(idx -> "Item " + idx)
                .collect(Collectors.toList());

        removeItems = new NativeButton("Remove 10 items from the DataProvider",
                event -> {
                    for (int i = 0; i < 10; i++) {
                        items.remove(items.size() - 1);
                    }
                });
        removeItems.setId("remove-items");

        CallbackDataProvider<String, Void> dataProvider = DataProvider
                .<String> fromCallbacks(this::getItems, this::count);
        grid.setDataProvider(dataProvider);
        add(grid, removeItems);
    }

    private Stream<String> getItems(Query<String, Void> query) {
        List<String> subList = items.subList(query.getOffset(),
                query.getOffset() + Math.min(items.size() - query.getOffset(),
                        query.getLimit()));
        return subList.stream();
    }

    private int count(Query<String, Void> query) {
        return items.size();
    }

}
