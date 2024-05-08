package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/empty")
public class GridEmptyPage extends Div {
    public GridEmptyPage() {
        Grid<String> grid = new Grid<>();
        add(grid);
    }
}
