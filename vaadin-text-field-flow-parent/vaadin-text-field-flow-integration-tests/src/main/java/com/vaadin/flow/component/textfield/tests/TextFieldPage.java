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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link TextField}.
 */
@Route("vaadin-text-field/text-field-test")
public class TextFieldPage extends Div {

    /**
     * Constructs a basic layout with a text field.
     */
    public TextFieldPage() {
        initView();
    }

    private void initView() {
        Div message = new Div();
        TextField textField = new TextField();
        textField.addValueChangeListener(event -> message
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue())));
        add(textField, message);

        NativeButton button = new NativeButton(
                "Set/unset text field read-only");
        button.setId("read-only");
        button.addClickListener(
                event -> textField.setReadOnly(!textField.isReadOnly()));
        add(button);

        NativeButton required = new NativeButton(
                "Set/unset field required property");
        required.setId("required");
        required.addClickListener(
                event -> textField.setRequiredIndicatorVisible(
                        !textField.isRequiredIndicatorVisible()));
        add(required);

        TextField valueChangeSource = new TextField();
        valueChangeSource.getStyle().set("display", "block");
        valueChangeSource.setId("value-change");
        NativeButton valueChange = new NativeButton("Get text field value",
                event -> handleTextFieldValue(valueChangeSource));
        valueChange.setId("get-value");
        add(valueChangeSource, valueChange);

        TextField textFieldClear = new TextField();
        textFieldClear.setId("clear-text-field");
        textFieldClear.getStyle().set("display", "block");
        textFieldClear.setClearButtonVisible(true);
        Div clearValueMessage = new Div();
        clearValueMessage.setId("clear-message");
        textFieldClear.addValueChangeListener(event -> clearValueMessage
                .setText(String.format("Old value: '%s'. New value: '%s'.",
                        event.getOldValue(), event.getValue())));
        add(textFieldClear, clearValueMessage);
        addDisabledField();
        addBasicFeatures();
        addFocusShortcut();
        addInvalidCheck();
        addHelperText();
        addHelperComponent();
    }

    private void handleTextFieldValue(TextField field) {
        Label label = new Label(field.getValue());
        label.addClassName("text-field-value");
        add(label);
    }

    private void addDisabledField() {
        TextField textField = new TextField();
        textField.setLabel("Text field label");
        textField.setPlaceholder("placeholder text");
        textField.setEnabled(false);
        textField.setId("disabled-text-field");
        Div message = new Div();
        message.setId("disabled-text-field-message");
        textField.addValueChangeListener(
                change -> message.setText("Value changed"));
        add(textField, message);
    }

    private void addBasicFeatures() {
        Div message = new Div();

        TextField textField = new TextField();
        textField.setLabel("Text field label");
        textField.setPlaceholder("placeholder text");
        textField.addValueChangeListener(event -> message.setText(
                String.format("Text field value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));

        textField.setId("text-field-with-value-change-listener");
        message.setId("text-field-value");

        add(textField, new ValueChangeModeButtonProvider(textField)
                .getValueChangeModeRadios(), message);
    }

    private void addFocusShortcut() {
        TextField textField = new TextField();
        textField.setLabel("Press ALT + 1 to focus");
        textField.addFocusShortcut(Key.DIGIT_1, KeyModifier.ALT);
        textField.setId("shortcut-field");
        add(textField);
    }

    private void addInvalidCheck() {
        final TextField field = new TextField();
        field.setMaxLength(10);
        field.setMinLength(5);
        TextFieldTestPageUtil.addInvalidCheck(this, field);
    }

    private void addHelperText() {
        TextField textField = new TextField();
        textField.setLabel("Helper text should be visible");
        textField.setHelperText("Helper text");
        textField.setId("helper-text-field");

        NativeButton clearButton = new NativeButton("Clear helper text");
        clearButton.setId("clear-helper-text-button");
        clearButton.addClickListener(event -> textField.setHelperText(null));
        add(textField, clearButton);
    }

    private void addHelperComponent() {
        TextField textField = new TextField();
        textField.setLabel("Helper component should be visible");
        Span span = new Span("Helper Component");
        span.setId("helper-component");
        textField.setHelperComponent(span);
        textField.setId("helper-component-field");

        NativeButton clearButton = new NativeButton("Clear helper component");
        clearButton.setId("clear-helper-component-button");
        clearButton
                .addClickListener(event -> textField.setHelperComponent(null));
        add(textField, clearButton);
    }
}
