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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-on-client-and-slot")
public class GridOnClientAndSlotPage extends Div {
    public GridOnClientAndSlotPage() {
        GridOnClientAndSlot gridOnClientAndSlot = new GridOnClientAndSlot();

        gridOnClientAndSlot.add(createGrid());

        NativeButton addGridButton = new NativeButton("add grid",
                e -> gridOnClientAndSlot.add(createGrid()));
        addGridButton.setId("add-new-grid-button");

        add(gridOnClientAndSlot, addGridButton);
    }

    private Grid<String> createGrid() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(String::toString).setHeader("Column");
        grid.setItems("Item 1", "Item 2", "Item 3", "Item 4");

        return grid;
    }
}
