/*
 * Copyright 2000-2017 Vaadin Ltd.
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
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

/**
 * Test view for {@link TextField}.
 */
@Route("text-field-test")
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
        valueChangeSource.setId("value-change");
        NativeButton valueChange = new NativeButton("Get text field value",
                event -> handleTextFieldValue(valueChangeSource));
        valueChange.setId("get-value");
        add(valueChangeSource, valueChange);
    }

    private void handleTextFieldValue(TextField field) {
        Label label = new Label(field.getValue());
        label.addClassName("text-field-value");
        add(label);
    }
}
