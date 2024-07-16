/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
