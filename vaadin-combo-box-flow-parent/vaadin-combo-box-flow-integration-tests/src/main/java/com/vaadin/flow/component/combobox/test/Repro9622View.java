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

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

/**
 * Reproduction for https://github.com/vaadin/flow-components/issues/9622
 *
 * An initially hidden ComboBox does not load its items when made visible and
 * opened from a click listener (regression since Vaadin 25.2.0).
 */
@Route("repro-9622")
public class Repro9622View extends Div {

    public Repro9622View() {
        ComboBox<String> addTokenComboBox = new ComboBox<>();
        addTokenComboBox.setId("combo");
        addTokenComboBox.setVisible(false);
        addTokenComboBox.setItems(List.of("A", "B", "C"));

        Button addTokenButton = new Button("Add item");
        addTokenButton.setId("add-button");

        addTokenButton.addClickListener(event -> {
            addTokenButton.setVisible(false);
            addTokenComboBox.setVisible(true);
            addTokenComboBox.setValue(null);
            addTokenComboBox.setOpened(true);
            addTokenComboBox.focus();
        });

        addTokenComboBox.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                Notification.show("Selected: " + event.getValue());

                addTokenButton.setVisible(true);
                addTokenComboBox.setVisible(false);
            }
        });

        addTokenComboBox.getElement().addPropertyChangeListener("opened",
                event -> {
                    if (event.getValue() instanceof Boolean value) {
                        if (!value) {
                            addTokenButton.setVisible(true);
                            addTokenComboBox.setVisible(false);
                        }
                    }
                });

        add(addTokenComboBox, addTokenButton);
    }
}
