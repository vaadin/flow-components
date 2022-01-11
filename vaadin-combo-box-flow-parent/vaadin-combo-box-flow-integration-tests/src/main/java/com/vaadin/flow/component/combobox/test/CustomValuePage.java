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
package com.vaadin.flow.component.combobox.test;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/custom-value")
public class CustomValuePage extends Div {

    private ComboBox<String> combo = new ComboBox<>();
    private List<String> items = new ArrayList<>();

    public CustomValuePage() {
        items.add("foo");

        Div customValueMessages = new Div();
        customValueMessages.setId("custom-value-messages");
        Div valueMessages = new Div();
        valueMessages.setId("value-messages");

        combo.setItems(items);
        combo.addCustomValueSetListener(
                e -> customValueMessages.add(new Paragraph(e.getDetail())));
        combo.addValueChangeListener(e -> valueMessages.add(
                new Paragraph(e.getValue() == null ? "null" : e.getValue())));

        add(combo, new H3("custom value changes:"), customValueMessages,
                new H3("combo box value changes:"), valueMessages);

        // Allow configuring common use cases
        NativeButton button1 = new NativeButton(
                "set custom value as combo box value",
                e -> setComboBoxToSetCustomValuesAsValue(combo));
        button1.setId("set-custom-values-as-value");

        NativeButton button2 = new NativeButton("add custom values to data set",
                e -> setComboBoxToAddCustomValuesToData(combo));
        button2.setId("add-custom-values-to-data");

        add(new H3("config common use cases:"), button1, button2);
    }

    private void setComboBoxToSetCustomValuesAsValue(ComboBox<String> combo) {
        combo.addCustomValueSetListener(e -> combo.setValue(e.getDetail()));
    }

    private void setComboBoxToAddCustomValuesToData(ComboBox<String> combo) {
        combo.addCustomValueSetListener(e -> {
            items.add(e.getDetail());
            combo.getDataProvider().refreshAll();
        });
    }

}
