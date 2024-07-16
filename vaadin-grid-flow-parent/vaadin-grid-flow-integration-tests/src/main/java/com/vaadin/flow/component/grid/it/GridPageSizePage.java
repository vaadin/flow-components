/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

/**
 * Test view that uses Grid with different pageSize settings.
 */
@Route("vaadin-grid/grid-page-size")
public class GridPageSizePage extends Div {

    private DataProvider<String, ?> dataProvider = DataProvider.fromCallbacks(
            query -> getStream(query).mapToObj(Integer::toString),
            query -> 10000);

    private Label info;

    /**
     * Creates a view with a grid with page size of 10.
     */
    public GridPageSizePage() {
        Grid<String> grid = new Grid<>(10);

        grid.setDataProvider(dataProvider);
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");

        info = new Label();
        info.setId("query-info");

        Input size = new Input();
        size.setId("size-input");
        NativeButton button = new NativeButton("Change page size", event -> {
            int pageSize = Integer.parseInt(size.getValue());
            grid.setPageSize(pageSize);
        });
        button.setId("size-submit");

        add(grid, info, new Div(size, button));
    }

    private IntStream getStream(Query<String, Void> query) {
        info.setText(String.format("Query offset: %d Query limit: %d",
                query.getOffset(), query.getLimit()));
        return IntStream.range(query.getOffset(),
                query.getOffset() + query.getLimit());
    }

}
