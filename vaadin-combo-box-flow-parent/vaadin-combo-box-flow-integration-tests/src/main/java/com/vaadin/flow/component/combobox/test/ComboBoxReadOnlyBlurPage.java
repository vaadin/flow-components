/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.combobox.test;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("vaadin-combo-box/readonly-blur")
public class ComboBoxReadOnlyBlurPage extends VerticalLayout {

    public ComboBoxReadOnlyBlurPage() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setAllowCustomValue(true);
        comboBox.setRequired(true);
        comboBox.setReadOnly(true);
        comboBox.addCustomValueSetListener(ev -> {
            Span span = new Span();
            span.setText("Custom value set");
            span.setId("custom-value-set");
            add(span);
        });
        comboBox.setItems("A", "B", "C");
        comboBox.setValue("D");
        comboBox.focus();

        add(comboBox);
    }

}
