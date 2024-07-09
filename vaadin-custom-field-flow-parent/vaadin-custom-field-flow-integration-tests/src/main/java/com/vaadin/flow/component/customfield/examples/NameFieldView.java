/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.customfield.examples;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.Route;

@Route("vaadin-custom-field/custom-field-helper")
public class NameFieldView extends Div {

    public NameFieldView() {

        NameField fieldHelper = new NameField();
        fieldHelper.setHelperText("Helper text");
        fieldHelper.setId("custom-field-helper-text");

        NativeButton clearText = new NativeButton("Clear helper", e -> {
            fieldHelper.setHelperText(null);
        });
        clearText.setId("button-clear-helper");

        NameField fieldHelperComponent = new NameField();
        fieldHelperComponent.setId("custom-field-helper-component");

        Span span = new Span("Helper component");
        span.setId("helper-component");

        NativeButton clearComponent = new NativeButton("Clear helper component",
                e -> {
                    fieldHelperComponent.setHelperComponent(null);
                });
        clearComponent.setId("button-clear-helper-component");

        fieldHelperComponent.setHelperComponent(span);

        add(fieldHelper, clearText, fieldHelperComponent, clearComponent);
    }
}
