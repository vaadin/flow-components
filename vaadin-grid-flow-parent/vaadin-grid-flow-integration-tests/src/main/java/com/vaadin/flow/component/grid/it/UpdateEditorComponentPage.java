/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
