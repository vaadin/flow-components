/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.NoTheme;

@Route("disabled-grid")
@NoTheme
public class DisabledGridPage extends Div {

    public DisabledGridPage() {
        Div message = new Div();
        message.setId("message");

        Grid<String> grid = new Grid<>();
        grid.setId("grid");
        grid.setItems(Arrays.asList("Item 1", "Item 2", "Item 3"));
        grid.addColumn(ValueProvider.identity()).setHeader("Item");
        grid.addColumn(new NativeButtonRenderer<>("Native button",
                item -> message.setText(
                        "ERROR!!! This listener should not be triggered!!!")))
                .setHeader("Button renderer");

        NativeButton headerButton = new NativeButton("Button in header",
                event -> message.setText(
                        "ERROR!!! This listener should not be triggered!!!"));
        headerButton.setId("header-button");
        grid.prependHeaderRow().getCells().get(0).setComponent(headerButton);

        NativeButton toggleEnabled = new NativeButton("Toggle enabled",
                event -> grid.setEnabled(!grid.isEnabled()));
        toggleEnabled.setId("toggleEnabled");

        add(grid, message, toggleEnabled);
    }

}
