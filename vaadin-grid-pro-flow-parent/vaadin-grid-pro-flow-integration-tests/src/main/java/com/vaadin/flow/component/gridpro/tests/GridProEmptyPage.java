package com.vaadin.flow.component.gridpro.tests;

import com.vaadin.flow.component.gridpro.GridPro;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid-pro/empty")
public class GridProEmptyPage extends Div {
    public GridProEmptyPage() {
        GridPro<String> grid = new GridPro<>();
        add(grid);
    }
}
