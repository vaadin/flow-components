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
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/update-editor")
public class UpdateEditorComponentPage extends Div {

    public UpdateEditorComponentPage() {
        Grid<String> grid = new Grid<>();
        Column<String> column = grid.addColumn(ValueProvider.identity())
                .setHeader("Name");
        grid.setItems("foo", "bar");

        Binder<String> binder = new Binder<>();
        grid.getEditor().setBinder(binder);

        TextField filed = new TextField();
        binder.bind(filed, ValueProvider.identity(), (item, value) -> {
        });
        column.setEditorComponent(filed);

        grid.addItemDoubleClickListener(
                event -> grid.getEditor().editItem(event.getItem()));

        NativeButton updateEditorComponent = new NativeButton(
                "Update editor component", event -> {
                    TextArea area = new TextArea();
                    binder.bind(area, ValueProvider.identity(),
                            (item, value) -> {
                            });
                    column.setEditorComponent(area);
                });
        updateEditorComponent.setId("update-editor");
        add(grid, updateEditorComponent);
    }
}
