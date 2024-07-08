/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.customfield.examples;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;

public class NameField extends CustomField<String> {
    private final TextField firstName = new TextField();
    private final TextField lastName = new TextField();

    NameField() {
        setLabel("Phone number");
        setHelperText("Your full first and last names");
        firstName.setMinLength(2);
        firstName.getStyle().set("width", "7em");
        lastName.getStyle().set("width", "7em");

        Div layout = new Div();
        layout.add(firstName, lastName);

        add(layout);
    }

    @Override
    protected String generateModelValue() {
        return firstName.getValue() + " " + lastName.getValue();
    }

    @Override
    protected void setPresentationValue(String newPresentationValue) {
        if (newPresentationValue == null) {
            firstName.clear();
            lastName.clear();
        }
    }
}