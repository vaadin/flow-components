/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

/**
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-virtual-scrolling")
public class GridVirtualScrollingPage extends Div {

    private DataProvider<String, ?> dataProvider = DataProvider.fromCallbacks(
            query -> getStream(query).mapToObj(Integer::toString),
            query -> 100 * 1000 * 1000);

    private Label info;

    public GridVirtualScrollingPage() {
        Grid<String> grid = new Grid<>();

        grid.setDataProvider(dataProvider);
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");

        info = new Label();
        info.setId("query-info");

        add(grid, info);
    }

    private IntStream getStream(Query<String, Void> query) {
        info.setText(String.format("Query offset: %d Query limit: %d",
                query.getOffset(), query.getLimit()));
        return IntStream.range(query.getOffset(),
                query.getOffset() + query.getLimit());
    }

}
