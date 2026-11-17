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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-select-on-item-click")
public class GridSelectOnItemClickPage extends Div {
    public GridSelectOnItemClickPage() {
        Grid<String> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, 20).mapToObj(i -> "Item " + i).toList());
        grid.addColumn(item -> item).setHeader("Name");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        ((GridMultiSelectionModel<String>) grid.getSelectionModel())
                .setSelectOnItemClick(true);

        Span selectedItems = new Span();
        selectedItems.setId("selected-items");
        grid.addSelectionListener(
                event -> selectedItems.setText(event.getAllSelectedItems()
                        .stream().sorted().collect(Collectors.joining(", "))));

        add(grid, new Div(selectedItems));
    }
}
