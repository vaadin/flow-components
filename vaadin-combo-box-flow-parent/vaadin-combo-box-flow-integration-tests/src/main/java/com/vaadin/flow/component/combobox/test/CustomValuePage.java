/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
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
