/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/combo-box-async-value-initialization")
public class ComboBoxAsyncValueInitialization extends Div {
    public ComboBoxAsyncValueInitialization() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems("Item 1", "Item 2", "Item 3");

        Div valueLog = new Div();
        valueLog.setId("value-log");
        comboBox.addValueChangeListener(e -> {
            Div entry = new Div();
            entry.setText("Value: %s, isFromClient: %s".formatted(e.getValue(),
                    e.isFromClient()));
            valueLog.add(entry);
        });

        // Initialize the value asynchronously. Running ui.access() in
        // beforeClientResponse() should ensure that the value change happens
        // after sending the initial state to the client. The changes from
        // ui.access() should be sent to the client in a follow-up response even
        // without using @Push, as the first response triggers a sync to the
        // server due to several client-side events.
        UI ui = UI.getCurrent();
        ui.beforeClientResponse(ui, context -> {
            ui.access(() -> comboBox.setValue("Item 1"));
        });

        add(comboBox, valueLog);
    }
}
