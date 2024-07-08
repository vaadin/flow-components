/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-scroll-to")
public class GridScrollToPage extends Div {
    public GridScrollToPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("data-grid");

        grid.setItems(IntStream.rangeClosed(0, 1000).mapToObj(String::valueOf));

        grid.addColumn(item -> item).setHeader("Data");

        NativeButton scrollToStart = new NativeButton("Scroll to start",
                e -> grid.scrollToStart());
        scrollToStart.setId("scroll-to-start");

        NativeButton scrollToEnd = new NativeButton("Scroll to end",
                e -> grid.scrollToEnd());
        scrollToEnd.setId("scroll-to-end");

        NativeButton scrollToRow500 = new NativeButton("Scroll to row 500",
                e -> grid.scrollToIndex(500));
        scrollToRow500.setId("scroll-to-row-500");

        Grid<String> grid2 = new Grid<>();
        grid2.setId("scroll-to-end-grid");

        List<String> items = new ArrayList<>();
        grid2.setItems(items);
        grid2.addColumn(item -> item);

        NativeButton addRowsAndScrollToEnd = new NativeButton(
                "Add row and scroll to end", e -> {
                    items.add(String.valueOf(items.size()));
                    items.add(String.valueOf(items.size()));
                    grid2.getDataProvider().refreshAll();
                    grid2.scrollToEnd();
                });
        addRowsAndScrollToEnd.setId("add-row-and-scroll-to-end");

        add(grid, scrollToStart, scrollToEnd, scrollToRow500, grid2,
                addRowsAndScrollToEnd);
    }
}
