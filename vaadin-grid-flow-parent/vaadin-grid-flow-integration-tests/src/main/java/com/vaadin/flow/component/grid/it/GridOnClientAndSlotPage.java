package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/grid-on-client-and-slot")
public class GridOnClientAndSlotPage extends Div {
    public GridOnClientAndSlotPage() {
        GridOnClientAndSlot gridOnClientAndSlot = new GridOnClientAndSlot();
        Grid<String> grid = new Grid<>();
        grid.addColumn(String::toString).setHeader("Column");
        grid.setItems("Item 1", "Item 2", "Item 3", "Item 4");
        
        gridOnClientAndSlot.add(grid);

        add(gridOnClientAndSlot);
    }
}
