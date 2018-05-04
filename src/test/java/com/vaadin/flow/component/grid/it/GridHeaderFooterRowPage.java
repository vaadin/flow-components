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

import java.util.Arrays;
import java.util.Comparator;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.NoTheme;

/**
 * Test view that adds header and footer rows to Grid after rendering.
 */
@Route("grid-header-footer-rows")
@NoTheme
public class GridHeaderFooterRowPage extends Div {

    int counter = 0;

    public GridHeaderFooterRowPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        Column<String> column = grid.addColumn(ValueProvider.identity());
        add(grid);
        NativeButton button = new NativeButton("Prepend header", event -> grid
                .prependHeaderRow().getCell(column).setText("" + (counter++)));
        button.setId("prepend-header");
        add(button);

        button = new NativeButton("Append header", event -> grid
                .appendHeaderRow().getCell(column).setText("" + (counter++)));
        button.setId("append-header");
        add(button);

        button = new NativeButton("Prepend footer", event -> grid
                .prependFooterRow().getCell(column).setText("" + (counter++)));
        button.setId("prepend-footer");
        add(button);

        button = new NativeButton("Append footer", event -> grid
                .appendFooterRow().getCell(column).setText("" + (counter++)));
        button.setId("append-footer");
        add(button);

        button = new NativeButton("Set sortable", event -> column
                .setComparator(Comparator.comparing(String::toString)));
        button.setId("set-sortable");
        add(button);

        button = new NativeButton("Append header without setting content",
                event -> grid.appendHeaderRow());
        button.setId("append-header-without-content");
        add(button);

        button = new NativeButton("Set multiselect",
                event -> grid.setSelectionMode(SelectionMode.MULTI));
        button.setId("set-multiselect");
        add(button);

        button = new NativeButton("Disable selection",
                event -> grid.setSelectionMode(SelectionMode.NONE));
        button.setId("disable-selection");
        add(button);

        button = new NativeButton("Set components for headers",
                event -> grid.getHeaderRows().stream()
                        .flatMap(row -> row.getCells().stream())
                        .forEach(cell -> cell.setComponent(new Label("foo"))));
        button.setId("set-components-for-headers");
        add(button);

        button = new NativeButton("Set text for headers",
                event -> grid.getHeaderRows().stream()
                        .flatMap(row -> row.getCells().stream())
                        .forEach(cell -> cell.setText("bar")));
        button.setId("set-texts-for-headers");
        add(button);

        button = new NativeButton("Column::setHeader",
                event -> column.setHeader("" + (counter++)));
        button.setId("column-set-header");
        add(button);
    }

}
