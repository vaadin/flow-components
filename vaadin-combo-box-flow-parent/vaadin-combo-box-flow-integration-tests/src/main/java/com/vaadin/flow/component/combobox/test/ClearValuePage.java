/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/clear-value")
public class ClearValuePage extends Div {
    static final String INITIAL_VALUE = "two";
    static final List<String> ITEMS = List.of("one", "two", "three");

    static final String COMBO_BOX = "combo-box";
    static final String COMBO_BOX_CLEAR_BUTTON = "combo-box-clear-button";
    static final String COMBO_BOX_SET_NULL_VALUE_BUTTON = "combo-box-set-null-value-button";

    static final String COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE = "combo-box-with-allow-custom-value";
    static final String COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_CLEAR_BUTTON = "combo-box-with-allow-custom-value-clear-button";
    static final String COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_SET_NULL_VALUE_BUTTON = "combo-box-with-allow-custom-value-set-null-value-button";

    static final String COMBO_BOX_WITH_CLEAR_BUTTON = "combo-box-with-clear-button";
    static final String COMBO_BOX_WITH_CLEAR_BUTTON_VALUE_MESSAGES = "combo-box-with-clear-button-value-messages";

    public ClearValuePage() {
        createComboBox();
        createComboBoxWithClearButton();
        createComboBoxWithAllowCustomValue();
    }

    private void createComboBox() {
        ComboBox<String> comboBox = new ComboBox<>("Choose option:", ITEMS);
        comboBox.setValue(INITIAL_VALUE);
        comboBox.setId(COMBO_BOX);

        NativeButton setNullValueButton = new NativeButton("Set null value",
                event -> comboBox.setValue(null));
        setNullValueButton.setId(COMBO_BOX_SET_NULL_VALUE_BUTTON);

        NativeButton clearButton = new NativeButton("Clear",
                event -> comboBox.clear());
        clearButton.setId(COMBO_BOX_CLEAR_BUTTON);

        addCard("ComboBox", comboBox, setNullValueButton, clearButton);
    }

    private void createComboBoxWithClearButton() {
        Div valueMessages = new Div();
        valueMessages.setId(COMBO_BOX_WITH_CLEAR_BUTTON_VALUE_MESSAGES);

        ComboBox<String> comboBox = new ComboBox<>("Choose option:", ITEMS);
        comboBox.setValue(INITIAL_VALUE);
        comboBox.setClearButtonVisible(true);
        comboBox.setId(COMBO_BOX_WITH_CLEAR_BUTTON);

        comboBox.addValueChangeListener(e -> valueMessages.add(
                new Paragraph(e.getValue() == null ? "null" : e.getValue())));

        addCard("ComboBox with clear button", comboBox, valueMessages);
    }

    private void createComboBoxWithAllowCustomValue() {
        ComboBox<String> comboBox = new ComboBox<>("Choose option:", ITEMS);
        comboBox.setAllowCustomValue(true);
        comboBox.setValue(INITIAL_VALUE);
        comboBox.setId(COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE);
        comboBox.addCustomValueSetListener(event -> {
            if (event.getDetail().equals("NotAcceptableCustomValue")) {
                comboBox.clear();
            }
        });

        NativeButton setNullValueButton = new NativeButton("Set null value",
                event -> comboBox.setValue(null));
        setNullValueButton
                .setId(COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_SET_NULL_VALUE_BUTTON);

        NativeButton clearButton = new NativeButton("Clear",
                event -> comboBox.clear());
        clearButton.setId(COMBO_BOX_WITH_ALLOW_CUSTOM_VALUE_CLEAR_BUTTON);

        addCard("ComboBox with custom values", comboBox, setNullValueButton,
                clearButton);
    }

    private void addCard(String title, Component... components) {
        VerticalLayout layout = new VerticalLayout();
        layout.add(new H2(title));
        layout.add(components);
        add(layout);
    }
}
