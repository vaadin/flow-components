/*
 * Copyright 2000-2024 Vaadin Ltd.
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

        NativeButton setSmallPageSize = new NativeButton(
                "Set small page size (5)", e -> {
                    grid.setPageSize(5);
                });
        setSmallPageSize.setId("set-small-page-size");

        add(grid, scrollToStart, scrollToEnd, scrollToRow500,
                addRowsAndScrollToEnd, addRowAndScrollToIndex,
                setSmallPageSize);
    }
}
