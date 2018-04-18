/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.NoTheme;

/**
 * Test view for grid's multi selection column.
 */
@Route("grid-multi-selection-column")
@NoTheme
public class GridMultiSelectionColumnPage extends Div {

    public static final int ITEM_COUNT = 1000;

    private Div message;

    /**
     * Constructor.
     */
    public GridMultiSelectionColumnPage() {
        message = new Div();
        message.setId("selected-item-count");

        createLazyGrid();
        createInMemoryGrid();
        createGridWithSwappedDataProvider();

        add(message);
    }

    private void createLazyGrid() {
        Grid<String> lazyGrid = new Grid<>();
        lazyGrid.setDataProvider(DataProvider.fromCallbacks(query -> {
            return IntStream
                    .range(query.getOffset(),
                            query.getOffset() + query.getLimit())
                    .mapToObj(Integer::toString);
        }, query -> ITEM_COUNT));
        setUp(lazyGrid);
        lazyGrid.setId("lazy-grid");

        add(new H2("Lazy grid"), lazyGrid);
    }

    private void createInMemoryGrid() {
        Grid<String> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, ITEM_COUNT).mapToObj(Integer::toString));
        setUp(grid);
        grid.setId("in-memory-grid");
        add(new H2("In-memory grid"), grid);
    }

    private void createGridWithSwappedDataProvider() {
        Grid<String> grid = new Grid<>();
        setUp(grid);
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setDataProvider(
                new CallbackDataProvider<>(this::fetch, this::count));
        grid.setId("swapped-grid");

        NativeButton inMemory = new NativeButton("Set in-memory DataProvider",
                evt -> grid
                        .setItems(Arrays.asList("Item 1", "Item 2", "Item 3")));
        inMemory.setId("set-in-memory-button");
        NativeButton backEnd = new NativeButton("Set backend DataProvider",
                evt -> grid.setDataProvider(
                        new CallbackDataProvider<>(this::fetch, this::count)));
        backEnd.setId("set-backend-button");
        add(new H2("Swapped grid"), grid, inMemory, backEnd);
    }

    private Stream<String> fetch(Query<String, ?> query) {
        List<String> list = new ArrayList<>(query.getLimit());
        for (int i = 0; i < query.getLimit(); i++) {
            int id = query.getOffset() + i + 1;
            list.add("Item " + id);
        }
        return list.stream();
    }

    private int count(Query<String, ?> query) {
        return 100000;
    }

    private void setUp(Grid<String> grid) {
        grid.setSelectionMode(SelectionMode.MULTI);
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");
        grid.addSelectionListener(event -> message.setText(
                "Selected item count: " + event.getAllSelectedItems().size()));
    }
}
