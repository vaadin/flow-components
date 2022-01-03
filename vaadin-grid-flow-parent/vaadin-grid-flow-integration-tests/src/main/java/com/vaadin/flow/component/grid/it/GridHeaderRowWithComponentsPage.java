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

import java.util.Arrays;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

/**
 * Test view that adds header rows with components to a Grid.
 */
@Route("vaadin-grid/grid-header-row-with-components")
public class GridHeaderRowWithComponentsPage extends Div {

    public GridHeaderRowWithComponentsPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        Column<String> column = grid.addColumn(ValueProvider.identity());
        add(grid);

        HeaderRow row1 = grid.appendHeaderRow();
        row1.getCell(column).setComponent(new Label("foo"));
        HeaderRow row2 = grid.appendHeaderRow();
        row2.getCell(column).setComponent(new Label("bar"));

        NativeButton button = new NativeButton(
                "Prepend header, set first text and then component");
        button.setId("set-both-text-and-component");
        button.addClickListener(event -> {
            HeaderRow topRow = grid.prependHeaderRow();
            topRow.getCell(column).setText("this is text");
            topRow.getCell(column).setComponent(new Label("this is component"));
        });
        add(button);
    }

}
