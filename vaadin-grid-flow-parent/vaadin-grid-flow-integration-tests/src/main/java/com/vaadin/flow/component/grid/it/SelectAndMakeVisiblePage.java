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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/select-invisible-grid")
public class SelectAndMakeVisiblePage extends Div {

    public SelectAndMakeVisiblePage() {
        Grid<String> grid = new Grid<>();
        grid.setItems("foo", "bar");
        grid.setVisible(false);
        grid.addColumn(ValueProvider.identity()).setHeader("Name");

        NativeButton button = new NativeButton("Make grid visible", event -> {
            grid.setVisible(true);
            grid.getSelectionModel().select("foo");
        });

        button.setId("select");

        add(grid, button);
    }
}
