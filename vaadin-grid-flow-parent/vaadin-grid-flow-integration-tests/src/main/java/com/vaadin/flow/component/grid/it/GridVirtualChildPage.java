/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route(value = "vaadin-grid/virtual-child")
public class GridVirtualChildPage extends Div {

    public GridVirtualChildPage() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(s -> s).setHeader("Test");
        grid.setItems("1", "2", "3");
        getElement().appendVirtualChild(grid.getElement());
    }

}
