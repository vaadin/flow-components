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
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.GeneratedVaadinEmailField;
import com.vaadin.flow.component.textfield.GeneratedVaadinNumberField;
import com.vaadin.flow.component.textfield.GeneratedVaadinTextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
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
        addClearButtonFeature();
        addAutoselectFeature();
        addNumberFields();
        addDisabledField();
        addEmailFieldFields();
        addVariantsFeature();
    }

    private void addVariantsFeature() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        TextField textField = new TextField();
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        
        NumberField numberField = new NumberField();
        numberField.addThemeVariants(TextFieldVariant.LUMO_SMALL);

        EmailField emailField = new EmailField();
        emailField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        // end-source-example

        addVariantsDemo(TextField::new,
                GeneratedVaadinTextField::addThemeVariants,
                GeneratedVaadinTextField::removeThemeVariants,
                TextFieldVariant::getVariantName, TextFieldVariant.LUMO_SMALL);
        
        addVariantsDemo(NumberField::new,
                GeneratedVaadinNumberField::addThemeVariants,
                GeneratedVaadinNumberField::removeThemeVariants,
                TextFieldVariant::getVariantName, TextFieldVariant.LUMO_SMALL);
        
        addVariantsDemo(EmailField::new,
                GeneratedVaadinEmailField::addThemeVariants,
                GeneratedVaadinEmailField::removeThemeVariants,
                TextFieldVariant::getVariantName, TextFieldVariant.LUMO_SMALL);
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
                        .getValueChangeModeRadios(),
                message);
    }

    private void addClearButtonFeature() {
        // begin-source-example
        // source-example-heading: Text field with clear button
        TextField textField = new TextField();
        textField.setLabel("Text field label");
        textField.setPlaceholder("placeholder text");
        NativeButton clearButton = new NativeButton("Toggle clear button", event -> {
            textField.setClearButtonVisible(
                    !textField.isClearButtonVisible());
        });
        // end-source-example

        addCard("Text field with clear button", textField, clearButton);
    }

    private void addAutoselectFeature() {
        // begin-source-example
        // source-example-heading: Text field with autoselect
        TextField textField = new TextField();
        textField.setLabel("Text field label");
        textField.setValue("Text field value");
        textField.setAutoselect(true);

        // end-source-example

        addCard("Text field with autoselect", textField);
    }

    private void addNumberFields() {
        // begin-source-example
        // source-example-heading: Number fields
        NumberField dollarField = new NumberField("Dollars");
        dollarField.setPrefixComponent(new Span("$"));

        NumberField euroField = new NumberField("Euros");
        euroField.setSuffixComponent(new Span("€"));

        NumberField stepperField = new NumberField("Stepper");
        stepperField.setValue(1d);
        stepperField.setMin(0);
        stepperField.setMax(10);
        stepperField.setHasControls(true);

        euroField.setSuffixComponent(new Span("€"));
        // end-source-example

        dollarField.setId("dollar-field");
        euroField.setId("euro-field");
        stepperField.setId("step-number-field");

        addCard("Number fields", dollarField, euroField, stepperField);
    }

    private void addEmailFieldFields() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Email field
        EmailField emailField = new EmailField("Email");
        emailField.addValueChangeListener(event -> message.setText(
                String.format("Email field value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        // end-source-example

        emailField.setId("email-field");
        message.setId("email-field-value");

        addCard("Email field", emailField, message);
    }

    private void addDisabledField() {

        // begin-source-example
        // source-example-heading: Disabled text field
        TextField textField = new TextField();
        textField.setLabel("Text field label");
        textField.setPlaceholder("placeholder text");
        textField.setEnabled(false);
        // end-source-example

        textField.setId("disabled-text-field");
        Div message = new Div();
        message.setId("disabled-text-field-message");
        textField.addValueChangeListener(
                change -> message.setText("Value changed"));

        addCard("Disabled text field", textField, message);
    }
}
