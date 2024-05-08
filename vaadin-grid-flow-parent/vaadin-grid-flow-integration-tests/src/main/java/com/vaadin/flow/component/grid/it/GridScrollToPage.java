package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.Collectors;
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

        List<String> items = IntStream.rangeClosed(0, 1000)
                .mapToObj(String::valueOf).collect(Collectors.toList());
        grid.setItems(items);

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

        NativeButton addRowsAndScrollToEnd = new NativeButton(
                "Add row and scroll to end", e -> {
                    items.add(String.valueOf(items.size()));
                    items.add(String.valueOf(items.size()));
                    grid.getDataProvider().refreshAll();
                    grid.scrollToEnd();
                });
        addRowsAndScrollToEnd.setId("add-row-and-scroll-to-end");

        NativeButton addRowAndScrollToIndex = new NativeButton(
                "Add row and scroll to index", e -> {
                    items.add(String.valueOf(items.size()));
                    grid.getDataProvider().refreshAll();
                    grid.scrollToIndex(items.size() - 1);
                });
        addRowAndScrollToIndex.setId("add-row-and-scroll-to-index");

        NativeButton scrollToItem500 = new NativeButton("Scroll to item 500",
                e -> grid.scrollToItem(items.get(500)));
        scrollToItem500.setId("scroll-to-item-500");

        NativeButton addRowAndScrollToItem = new NativeButton(
                "Add row and scroll to item", e -> {
                    String itemToAdd = String.valueOf(items.size());
                    items.add(itemToAdd);
                    grid.getDataProvider().refreshAll();
                    grid.scrollToItem(itemToAdd);
                });
        addRowAndScrollToItem.setId("add-row-and-scroll-to-item");

        NativeButton setSmallPageSize = new NativeButton(
                "Set small page size (5)", e -> {
                    grid.setPageSize(5);
                });
        setSmallPageSize.setId("set-small-page-size");

        add(grid, scrollToStart, scrollToEnd, scrollToRow500,
                addRowsAndScrollToEnd, addRowAndScrollToIndex, scrollToItem500,
                addRowAndScrollToItem, setSmallPageSize);
    }
}
