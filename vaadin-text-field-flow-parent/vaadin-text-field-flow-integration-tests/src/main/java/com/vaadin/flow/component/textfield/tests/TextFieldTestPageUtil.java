/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;

class TextFieldTestPageUtil {
    private TextFieldTestPageUtil() {
    }

    static <C extends Component & HasValidation> void addInvalidCheck(
            HasComponents parent, C field) {
        field.setId("invalid-test-field");
        Div isValid = new Div();
        isValid.setId("is-invalid");
        NativeButton checkIsValid = new NativeButton(
                "Check if current value of invalid-test-field is invalid");
        checkIsValid.setId("check-is-invalid");
        checkIsValid.addClickListener(event -> isValid
                .setText(field.isInvalid() ? "invalid" : "valid"));
        parent.add(field, checkIsValid, isValid);
    }
}
