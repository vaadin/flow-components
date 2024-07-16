/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/text-component-renderer")
public class TextInsideComponentRendererPage extends Div {

    public TextInsideComponentRendererPage() {
        Grid<String> grid = new Grid<>();

        grid.addColumn(new ComponentRenderer<>(line -> new Text(line)))
                .setHeader("Name");
        grid.setItems("foo", "bar");

        add(grid);
    }

}
