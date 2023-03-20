/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/text-renderer")
public class TextRendererPage extends Div {

    public TextRendererPage() {
        Grid<String> grid = new Grid<>();
        grid.setItems("foo", "bar");

        Renderer<String> renderer = new ComponentRenderer<Text, String>(
                item -> new Text(item));
        grid.addColumn(renderer).setHeader("Header");
        add(grid);

        NativeButton button = new NativeButton("Refresh data provider",
                event -> {
                    grid.getDataProvider().refreshAll();
                    grid.getClassNames().add("refreshed");
                });
        button.setId("refresh");
        add(button);
    }
}
