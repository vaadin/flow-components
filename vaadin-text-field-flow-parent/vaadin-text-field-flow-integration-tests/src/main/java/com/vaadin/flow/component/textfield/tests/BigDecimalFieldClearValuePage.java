/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import java.math.BigDecimal;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.component.textfield.BigDecimalField;

@Route("vaadin-big-decimal-field/clear-value")
public class BigDecimalFieldClearValuePage extends Div {
    public static final String CLEAR_BUTTON = "clear-button";
    public static final String CLEAR_AND_SET_VALUE_BUTTON = "clear-and-set-value-button";

    public BigDecimalFieldClearValuePage() {
        BigDecimalField bigDecimalField = new BigDecimalField();

        NativeButton clearButton = new NativeButton("Clear value");
        clearButton.setId(CLEAR_BUTTON);
        clearButton.addClickListener(event -> bigDecimalField.clear());

        NativeButton clearAndSetValueButton = new NativeButton(
                "Clear and set value", event -> {
                    bigDecimalField.clear();
                    bigDecimalField.setValue(BigDecimal.valueOf(12.34));
                });
        clearAndSetValueButton.setId(CLEAR_AND_SET_VALUE_BUTTON);

        add(bigDecimalField, clearButton, clearAndSetValueButton);
    }
}
