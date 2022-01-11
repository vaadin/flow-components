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

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link EmailField}.
 */
@Route("vaadin-text-field/email-field-test")
public class EmailFieldPage extends Div {

    /**
     * Constructs a basic layout with a text field.
     */
    public EmailFieldPage() {
        Div message = new Div();
        message.setId("message");
        EmailField emailField = new EmailField();
        emailField.addValueChangeListener(event -> message
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue())));
        add(emailField, message);

        NativeButton button = new NativeButton(
                "Set/unset text field read-only");
        button.setId("read-only");
        button.addClickListener(
                event -> emailField.setReadOnly(!emailField.isReadOnly()));
        add(button);

        NativeButton required = new NativeButton(
                "Set/unset field required property");
        required.setId("required");
        required.addClickListener(
                event -> emailField.setRequiredIndicatorVisible(
                        !emailField.isRequiredIndicatorVisible()));
        add(required);

        NativeButton enabled = new NativeButton(
                "Set/unset field enabled property");
        enabled.setId("disabled");
        enabled.addClickListener(
                event -> emailField.setEnabled(!emailField.isEnabled()));
        add(enabled);

        EmailField emailFieldClear = new EmailField();
        emailFieldClear.setId("clear-email-field");
        emailFieldClear.getStyle().set("display", "block");
        emailFieldClear.setClearButtonVisible(true);
        Div clearValueMessage = new Div();
        clearValueMessage.setId("clear-message");
        emailFieldClear.addValueChangeListener(event -> clearValueMessage
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue())));
        add(emailFieldClear, clearValueMessage);
        TextFieldTestPageUtil.addInvalidCheck(this, new EmailField());
    }
}
