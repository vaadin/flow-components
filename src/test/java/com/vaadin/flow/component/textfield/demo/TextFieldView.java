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
package com.vaadin.flow.component.textfield.demo;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.GeneratedVaadinTextField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link GeneratedVaadinTextField} demo.
 *
 * @author Vaadin Ltd
 */
@Route("vaadin-text-field")
public class TextFieldView extends DemoView {

    @Override
    public void initView() {
        addBasicFeatures();
        addNumberFields();
    }

    private void addBasicFeatures() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic text field
        TextField textField = new TextField();
        textField.setLabel("Text field label");
        textField.setPlaceholder("placeholder text");
        textField.addValueChangeListener(event -> message.setText(
                String.format("Text field value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        // end-source-example

        textField.setId("text-field-with-value-change-listener");
        message.setId("text-field-value");

        addCard("Basic text field", textField,
                new ValueChangeModeButtonProvider(textField)
                        .getToggleValueSyncButton(),
                message);
    }

    private void addNumberFields() {
        // begin-source-example
        // source-example-heading: Number fields
        TextField dollarField = new TextField("Dollars");
        dollarField.setPattern("[0-9]*");
        dollarField.setPreventInvalidInput(true);
        dollarField.setPrefixComponent(new Span("$"));

        TextField euroField = new TextField("Euros");
        euroField.setPattern("[0-9]*");
        euroField.setPreventInvalidInput(true);
        euroField.setSuffixComponent(new Span("€"));
        // end-source-example

        dollarField.setId("dollar-field");
        euroField.setId("euro-field");

        addCard("Number fields", dollarField, euroField);
    }
}
