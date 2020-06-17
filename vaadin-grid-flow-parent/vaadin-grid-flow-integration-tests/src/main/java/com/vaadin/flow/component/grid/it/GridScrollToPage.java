package com.vaadin.flow.component.grid.it;

import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("grid-scroll-to")
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

        add(grid, scrollToStart, scrollToEnd, scrollToRow500);
    }
}
