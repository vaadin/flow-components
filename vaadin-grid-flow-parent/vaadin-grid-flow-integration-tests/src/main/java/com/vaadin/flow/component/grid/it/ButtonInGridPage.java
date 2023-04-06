/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/vaadin-button-inside-grid")
public class ButtonInGridPage extends Div {

    public ButtonInGridPage() {
        Grid<String> grid = new Grid<>();
        grid.setId("grid");

        Div div = new Div();
        div.setId("info");

        grid.addComponentColumn(item -> new Button("Show item", evt -> {
            div.setText(item);
        })).setHeader("Click to see an item");

        grid.setItems("foo", "bar");
        add(grid, div);
    }
}
