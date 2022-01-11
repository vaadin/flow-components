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

import java.util.Arrays;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

@Route("vaadin-grid/disabled-grid")
public class DisabledGridPage extends Div {

    private Div message;

    public DisabledGridPage() {
        message = new Div();
        message.setId("message");

        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.addColumn(ValueProvider.identity()).setHeader("Item");
        grid.addColumn(new NativeButtonRenderer<>("Native button",
                item -> reportError())).setHeader("Button renderer");

        grid.addComponentColumn(this::createCheckBox).setHeader("Checkbox");

        NativeButton headerButton = new NativeButton("Button in header",
                event -> reportError());
        headerButton.setId("header-button");
        grid.prependHeaderRow().getCells().get(0).setComponent(headerButton);

        NativeButton toggleEnabled = new NativeButton("Toggle enabled",
                event -> grid.setEnabled(!grid.isEnabled()));
        toggleEnabled.setId("toggleEnabled");

        grid.addItemClickListener(event -> reportError());
        grid.addItemDoubleClickListener(event -> reportError());

        add(grid, message, toggleEnabled);
    }

    private void reportError() {
        message.setText("ERROR!!! This listener should not be triggered!!!");
    }

    private Checkbox createCheckBox(String item) {
        Checkbox checkbox = new Checkbox();
        checkbox.addValueChangeListener(event -> reportError());
        return checkbox;
    }

}
