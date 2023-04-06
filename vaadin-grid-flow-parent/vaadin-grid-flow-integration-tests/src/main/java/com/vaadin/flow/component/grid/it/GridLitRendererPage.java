/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/lit-renderer")
public class GridLitRendererPage extends Div {

    public GridLitRendererPage() {
        Grid<Integer> grid = new Grid<>();
        grid.setItems(
                IntStream.range(0, 1000).boxed().collect(Collectors.toList()));
        grid.addColumn(LitRenderer
                .<Integer> of(
                        "<span id=\"item-${index}\">Lit: ${item.name}</span>")
                .withProperty("name", item -> "Item " + item))
                .setEditorComponent(new Span("Editor component"));
        grid.getEditor().setBinder(new Binder<>());
        setLitRenderer(grid);
        add(grid);

        NativeButton componentRendererButton = new NativeButton(
                "Set ComponentRenderer", e -> {
                    setComponentRenderer(grid);
                });
        componentRendererButton.setId("componentRendererButton");

        NativeButton litRendererButton = new NativeButton("Set LitRenderer",
                e -> {
                    setLitRenderer(grid);
                });
        litRendererButton.setId("litRendererButton");

        NativeButton toggleEditButton = new NativeButton("Toggle edit mode",
                e -> {
                    if (grid.getEditor().isOpen()) {
                        grid.getEditor().cancel();
                    } else {
                        grid.getEditor().editItem(0);
                    }
                });
        toggleEditButton.setId("toggleEditButton");

        NativeButton toggleAttachedButton = new NativeButton("Toggle attached",
                e -> {
                    if (grid.isAttached()) {
                        remove(grid);
                    } else {
                        add(grid);
                    }
                });
        toggleAttachedButton.setId("toggleAttachedButton");

        add(componentRendererButton, litRendererButton, toggleEditButton,
                toggleAttachedButton);
    }

    private void setLitRenderer(Grid<Integer> grid) {
        grid.setItemDetailsRenderer(LitRenderer.<Integer> of(
                "<span id=\"details-${index}\">Lit: ${item.name}</span>")
                .withProperty("name", item -> "Item details " + item));
    }

    private void setComponentRenderer(Grid<Integer> grid) {
        grid.setItemDetailsRenderer(
                new ComponentRenderer<Component, Integer>(item -> {
                    Div content = new Div();
                    content.setId("details-" + item);
                    content.setText("Component: Item details " + item);
                    return content;
                }));
    }

}
