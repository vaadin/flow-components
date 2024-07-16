/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.Route;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author Vaadin Ltd.
 */
@Route("vaadin-grid/grid-data-provider-size")
public class GridDataProviderSize extends Div {

    public GridDataProviderSize() {
        final Grid<String> grid = new Grid<>();
        final Grid.Column<String> col = grid.addColumn(it -> it)
                .setHeader("First column");

        Div div = new Div();
        div.setText("0");
        div.setId("info");

        grid.setDataProvider(new AbstractBackEndDataProvider<String, Object>() {
            @Override
            protected Stream<String> fetchFromBackEnd(
                    Query<String, Object> query) {
                return Stream.of();
            }

            @Override
            protected int sizeInBackEnd(Query<String, Object> query) {
                Integer count = Integer.parseInt(div.getText()) + 1;
                div.setText(count.toString());
                return 0;
            }
        });

        grid.sort(Arrays
                .asList(new GridSortOrder<>(col, SortDirection.ASCENDING)));
        add(grid, div);
    }

}
