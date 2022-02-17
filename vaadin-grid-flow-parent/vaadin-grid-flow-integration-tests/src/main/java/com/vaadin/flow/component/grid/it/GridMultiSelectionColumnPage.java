/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

/**
 * Test view for grid's multi selection column.
 */
@Route("vaadin-grid/grid-multi-selection-column")
public class GridMultiSelectionColumnPage extends Div {

    public static final int ITEM_COUNT = 1000;

    static final String IN_MEMORY_GRID_ID = "in-memory-grid";
    static final String DEFINED_ITEM_COUNT_LAZY_GRID_ID = "defined-item-count-lazy-grid";
    static final String UNKNOWN_ITEM_COUNT_LAZY_GRID_ID = "unknown-item-count-lazy-grid";
    static final String MULTI_SELECT_GRID_ALL_SELECTED_GRID_ID = "multi-select-grid-all-selected";
    static final String MULTI_SELECT_GRID_ONE_NOT_SELECTED_GRID_ID = "multi-select-grid-one-deselected";

    private Div message;

    /**
     * Constructor.
     */
    public GridMultiSelectionColumnPage() {
        message = new Div();
        message.setId("selected-item-count");

        createDefinedItemCountLazyGrid();
        createUnknownItemCountLazyGrid();
        createInMemoryGrid();
        createGridWithSwappedDataProvider();
        createBasicGridMultiAllRowsSelected();
        createBasicGridMultiOneRowDeSelected();

        add(message);
        createBasicGridFromSingleToMultiBeforeAttached();
        createBasicGridFromMultiToSingleBeforeAttached();
        setAutoWidthIsTrueOfSelectionColumn();
    }

    private void createDefinedItemCountLazyGrid() {
        Grid<String> lazyGrid = new Grid<>();
        lazyGrid.setItems(query -> IntStream
                .range(query.getOffset(), query.getOffset() + query.getLimit())
                .mapToObj(Integer::toString), query -> ITEM_COUNT);
        setUp(lazyGrid);
        lazyGrid.setId(DEFINED_ITEM_COUNT_LAZY_GRID_ID);

        add(new H2("Lazy grid"), lazyGrid);
    }

    private void createUnknownItemCountLazyGrid() {
        Grid<String> unknownItemCountLazyGrid = new Grid<>();
        unknownItemCountLazyGrid.setItems(query -> IntStream
                .range(query.getOffset(), query.getOffset() + query.getLimit())
                .mapToObj(Integer::toString));
        setUp(unknownItemCountLazyGrid);
        unknownItemCountLazyGrid.setId(UNKNOWN_ITEM_COUNT_LAZY_GRID_ID);

        add(new H2("Unknown Item Count Lazy grid"), unknownItemCountLazyGrid);
    }

    private void createInMemoryGrid() {
        Grid<String> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, ITEM_COUNT).mapToObj(Integer::toString));
        setUp(grid);
        grid.setId(IN_MEMORY_GRID_ID);
        add(new H2("In-memory grid"), grid);
    }

    private void createGridWithSwappedDataProvider() {
        Grid<String> grid = new Grid<>();
        setUp(grid);
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.setItems(new CallbackDataProvider<>(this::fetch, this::count));
        grid.setId("swapped-grid");

        NativeButton inMemory = new NativeButton("Set in-memory DataProvider",
                evt -> grid
                        .setItems(Arrays.asList("Item 1", "Item 2", "Item 3")));
        inMemory.setId("set-in-memory-button");
        NativeButton backEnd = new NativeButton("Set backend DataProvider",
                evt -> grid.setItems(
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

    private void createBasicGridFromSingleToMultiBeforeAttached() {
        Grid<String> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, ITEM_COUNT).mapToObj(Integer::toString));
        setUp(grid);
        grid.setId("in-testing-multi-selection-mode-grid");
        add(new H2("in-testing-multi-selection-mode-grid"), grid);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        add(grid);
    }

    private void createBasicGridFromMultiToSingleBeforeAttached() {
        Grid<String> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, ITEM_COUNT).mapToObj(Integer::toString));
        setUp(grid);
        grid.setId("in-testing-multi-selection-mode-grid-single");
        add(new H2("in-testing-multi-selection-mode-grid-single"), grid);

        grid.setSelectionMode(SelectionMode.MULTI);
        grid.setSelectionMode(SelectionMode.SINGLE);
        add(grid);
    }

    private void setAutoWidthIsTrueOfSelectionColumn() {
        Grid<String> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, ITEM_COUNT).mapToObj(Integer::toString));
        setUp(grid);
        grid.setId("set-auto-width-true");
        add(new H2("In-set-auto-width-true"), grid);

        grid.setSelectionMode(SelectionMode.MULTI);
        add(grid);
    }

    private void createBasicGridMultiAllRowsSelected() {
        Grid<String> grid = new Grid<>();
        grid.setId(MULTI_SELECT_GRID_ALL_SELECTED_GRID_ID);
        grid.setItems(IntStream.range(0, 2).mapToObj(Integer::toString));
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        grid.getSelectionModel().select("0");
        grid.getSelectionModel().select("1");

        NativeButton deSelectRow0Button = new NativeButton("DeSelect Row 0");
        deSelectRow0Button.setId("deSelectRow0");
        deSelectRow0Button.addClickListener(event -> {
            grid.getSelectionModel().deselect("0");
        });

        add(new H2("Small grid with two rows all selected"), grid,
                deSelectRow0Button);
    }

    private void createBasicGridMultiOneRowDeSelected() {
        Grid<String> grid = new Grid<>();
        grid.setId(MULTI_SELECT_GRID_ONE_NOT_SELECTED_GRID_ID);
        grid.setItems(IntStream.range(0, 2).mapToObj(Integer::toString));
        grid.addColumn(i -> i).setHeader("text");
        grid.addColumn(i -> String.valueOf(i.length())).setHeader("length");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);

        grid.getSelectionModel().select("1");

        NativeButton selectRow0Button = new NativeButton("Select Row 0");
        selectRow0Button.setId("selectRow0");
        selectRow0Button.addClickListener(event -> {
            grid.getSelectionModel().select("0");
        });

        add(new H2("Grid with two rows only one row selected"), grid,
                selectRow0Button);
    }
}
