/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/1453
 *
 * A draggable grid with GridDropMode.BETWEEN and a filter TextField in the
 * header. Dropping a row onto the header filter field used to dispatch a
 * grid-drop event with an empty dropLocation, which made GridDropEvent's
 * constructor throw NoSuchElementException on the server.
 */
@Route("repro-1453")
public class Repro1453View extends Div {

    public Repro1453View() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");

        TextField filter = new TextField();
        filter.setId("filter");
        filter.setPlaceholder("Filter");

        grid.addColumn(item -> item).setHeader(filter);
        grid.setItems(IntStream.range(0, 5).mapToObj(i -> "Item " + i)
                .collect(Collectors.toList()));

        grid.setRowsDraggable(true);
        grid.setDropMode(GridDropMode.BETWEEN);

        Div dropMessage = new Div();
        dropMessage.setId("drop-message");
        grid.addDropListener(e -> dropMessage.setText("drop: "
                + e.getDropLocation() + " on " + e.getDropTargetItem().orElse(null)));

        add(grid, dropMessage);
    }
}
