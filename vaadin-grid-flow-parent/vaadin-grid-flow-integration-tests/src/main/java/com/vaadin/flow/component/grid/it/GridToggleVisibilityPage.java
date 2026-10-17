/*
 * Copyright 2000-2026 Vaadin Ltd.
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
import java.util.List;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/toggle-visibility")
public class GridToggleVisibilityPage extends Div {

    private int generation;

    public GridToggleVisibilityPage() {
        createHiddenGrid();
        createHiddenParentGrid();
        createComponentGrid();
    }

    private void createHiddenGrid() {
        ListDataProvider<String> dataProvider = new ListDataProvider<>(
                new ArrayList<>(List.of("foo", "bar")));
        Grid<String> grid = new Grid<>();
        grid.setId("hidden-grid");
        grid.setItems(dataProvider);
        grid.addColumn(ValueProvider.identity()).setHeader("Name");
        grid.setVisible(false);

        NativeButton selectAndShow = new NativeButton("Select and show",
                event -> {
                    grid.setVisible(true);
                    grid.select("foo");
                });
        selectAndShow.setId("select-and-show");

        NativeButton refreshAndShow = new NativeButton("Refresh and show",
                event -> {
                    dataProvider.getItems().clear();
                    dataProvider.getItems().add("baz");
                    dataProvider.refreshAll();
                    grid.setVisible(true);
                });
        refreshAndShow.setId("refresh-and-show");

        add(grid, selectAndShow, refreshAndShow);
    }

    private void createHiddenParentGrid() {
        Grid<String> grid = new Grid<>();
        grid.setId("hidden-parent-grid");
        grid.setItems(
                IntStream.range(0, 10).mapToObj(i -> "Item " + i).toList());
        grid.addColumn(ValueProvider.identity());

        // https://github.com/vaadin/flow/issues/4448
        Div parent = new Div(grid);
        parent.setVisible(false);

        NativeButton toggleParent = new NativeButton("Toggle parent",
                event -> parent.setVisible(!parent.isVisible()));
        toggleParent.setId("toggle-parent");

        add(parent, toggleParent);
    }

    private void createComponentGrid() {
        Grid<String> grid = new Grid<>();
        grid.setId("component-grid");
        Column<String> textColumn = grid.addColumn(item -> "text of " + item)
                .setHeader("Text");
        grid.addComponentColumn(item -> new Span("component of " + item))
                .setHeader("Component");
        grid.setItems(nextItems());

        NativeButton hideGrid = new NativeButton("Hide the grid",
                event -> grid.setVisible(false));
        hideGrid.setId("hide-grid");

        NativeButton setItems = new NativeButton("Set new items",
                event -> grid.setItems(nextItems()));
        setItems.setId("set-items");

        NativeButton showGrid = new NativeButton("Show the grid",
                event -> grid.setVisible(true));
        showGrid.setId("show-grid");

        // Hiding the grid keeps the connector's JavaScript invocations on the
        // server, so the client keeps rendering rows whose components the new
        // item set has already discarded
        NativeButton hideGridAndColumn = new NativeButton(
                "Hide the grid, clear the items, hide the text column",
                event -> {
                    grid.setVisible(false);
                    grid.setItems(List.of());
                    textColumn.setVisible(false);
                });
        hideGridAndColumn.setId("hide-grid-and-column");

        NativeButton showGridWithNewItems = new NativeButton(
                "Show the grid with new items", event -> {
                    grid.setVisible(true);
                    grid.setItems(nextItems());
                });
        showGridWithNewItems.setId("show-grid-with-new-items");

        add(grid, hideGrid, setItems, showGrid, hideGridAndColumn,
                showGridWithNewItems);
    }

    private List<String> nextItems() {
        generation++;
        return IntStream.rangeClosed(1, 10)
                .mapToObj(row -> "item " + generation + "." + row).toList();
    }
}
