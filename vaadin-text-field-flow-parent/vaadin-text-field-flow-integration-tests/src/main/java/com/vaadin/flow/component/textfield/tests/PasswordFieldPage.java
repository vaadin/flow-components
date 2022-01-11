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
package com.vaadin.flow.component.textfield.tests;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link PasswordField}.
 */
@Route("vaadin-text-field/password-field-test")
public class PasswordFieldPage extends Div {

    /**
     * Constructs a basic layout with a text field.
     */
    public PasswordFieldPage() {
        Div message = new Div();
        PasswordField passwordField = new PasswordField();
        passwordField.addValueChangeListener(event -> message
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue())));
        add(passwordField, message);

        NativeButton button = new NativeButton(
                "Set/unset text field read-only");
        button.setId("read-only");
        button.addClickListener(event -> passwordField
                .setReadOnly(!passwordField.isReadOnly()));
        add(button);

        NativeButton required = new NativeButton(
                "Set/unset field required property");
        required.setId("required");
        required.addClickListener(
                event -> passwordField.setRequiredIndicatorVisible(
                        !passwordField.isRequiredIndicatorVisible()));
        add(required);

        PasswordField passwordFieldClear = new PasswordField();
        passwordFieldClear.setId("clear-password-field");
        passwordFieldClear.getStyle().set("display", "block");
        passwordFieldClear.setClearButtonVisible(true);
        Div clearValueMessage = new Div();
        clearValueMessage.setId("clear-message");
        passwordFieldClear.addValueChangeListener(event -> clearValueMessage
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue())));
        add(passwordFieldClear, clearValueMessage);
        addFocusShortcut();
        addBasicField();
        addDisabledField();
        addInvalidCheck();
    }

    private void addFocusShortcut() {
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Press ALT + 1 to focus");
        passwordField.addFocusShortcut(Key.DIGIT_1, KeyModifier.ALT);
        passwordField.setId("shortcut-field");
        add(passwordField);
    }

    private void addBasicField() {
        Div message = new Div();

        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password field label");
        passwordField.setPlaceholder("placeholder text");
        passwordField.addValueChangeListener(event -> message.setText(
                String.format("Password field value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        NativeButton button = new NativeButton("Toggle eye icon", event -> {
            passwordField.setRevealButtonVisible(
                    !passwordField.isRevealButtonVisible());
        });

        passwordField.setId("password-field-with-value-change-listener");
        message.setId("password-field-value");
        button.setId("toggle-button");

        add(button, passwordField,
                new ValueChangeModeButtonProvider(passwordField)
                        .getValueChangeModeRadios(),
                message);
    }

    private void addDisabledField() {
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password field label");
        passwordField.setPlaceholder("placeholder text");
        passwordField.setEnabled(false);
        passwordField.setId("disabled-password-field");
        Div message = new Div();
        message.setId("disabled-password-field-message");
        passwordField.addValueChangeListener(
                change -> message.setText("password changed"));

        add(passwordField, message);
    }

    private void addInvalidCheck() {
        final PasswordField field = new PasswordField();
        field.setMaxLength(10);
        field.setMinLength(5);
        TextFieldTestPageUtil.addInvalidCheck(this, field);
    }

}
