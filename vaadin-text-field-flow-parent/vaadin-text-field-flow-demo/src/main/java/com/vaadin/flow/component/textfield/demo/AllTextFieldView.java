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
import com.vaadin.flow.component.textfield.GeneratedVaadinTextArea;
import com.vaadin.flow.component.textfield.GeneratedVaadinTextField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextAreaVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

/**
 * View for {@link GeneratedVaadinTextField} demo.
 *
 * @author Vaadin Ltd
 */
@Route("all-vaadin-text-field")
public class AllTextFieldView extends DemoView {

    @Override
    public void initView() {
        addBasicFeatures();
        addClearButtonFeature();
        addNumberFields();
        addTextFieldStates();
        addVariantsFeature();

        addPasswordBasicField();
        addPasswordDisabledField();
        addPasswordVariantsFeature();

        addTextAreaBasicFeatures();
        addTextAreaMaxHeightFeature();
        addTextAreaMinHeightFeature();
        addTextAreaDisabledField();
        addTextAreaVariantsFeature();
    }

    private void addVariantsFeature() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        TextField textField = new TextField();
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        // end-source-example

        addVariantsDemo(TextField::new,
                GeneratedVaadinTextField::addThemeVariants,
                GeneratedVaadinTextField::removeThemeVariants,
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

    private void addTextFieldStates() {

        // begin-source-example
        // source-example-heading: Text field states
        TextField enabledTextField = new TextField();
        enabledTextField.setLabel("Text field label");
        enabledTextField.setPlaceholder("placeholder text");

        TextField disabledTextField = new TextField();
        disabledTextField.setLabel("Text field label");
        disabledTextField.setPlaceholder("placeholder text");
        disabledTextField.setEnabled(false);

        TextField readOnlyTextField = new TextField();
        readOnlyTextField.setLabel("Text field label");
        readOnlyTextField.setPlaceholder("placeholder text");
        readOnlyTextField.setReadOnly(true);
        // end-source-example

        enabledTextField.setId("enabled-text-field");
        disabledTextField.setId("disabled-text-field");
        enabledTextField.setId("read-only-text-field");

        Div message = new Div();
        message.setId("disabled-text-field-message");
        disabledTextField.addValueChangeListener(
                change -> message.setText("Value changed"));

        Div textFieldsContainer = new Div(enabledTextField, disabledTextField,
                readOnlyTextField);
        textFieldsContainer.getChildren().forEach(child -> {
            child.getElement().getStyle().set("margin",
                    "var(--lumo-space-s,8)");
        });

        addCard("Text field states", textFieldsContainer, message);
    }

    private void addPasswordVariantsFeature() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        PasswordField passwordField = new PasswordField();
        passwordField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        // end-source-example

        addVariantsDemo(PasswordField::new,
                GeneratedVaadinTextField::addThemeVariants,
                GeneratedVaadinTextField::removeThemeVariants,
                TextFieldVariant::getVariantName, TextFieldVariant.LUMO_SMALL);
    }

    private void addPasswordBasicField() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic password field
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
        // end-source-example

        passwordField.setId("password-field-with-value-change-listener");
        message.setId("password-field-value");
        button.setId("toggle-button");

        addCard("Basic password field", button, passwordField,
                new ValueChangeModeButtonProvider(passwordField)
                        .getValueChangeModeRadios(),
                message);
    }

    private void addPasswordDisabledField() {
        // begin-source-example
        // source-example-heading: Disabled password field
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password field label");
        passwordField.setPlaceholder("placeholder text");
        passwordField.setEnabled(false);
        // end-source-example

        passwordField.setId("disabled-password-field");
        Div message = new Div();
        message.setId("disabled-password-field-message");
        passwordField.addValueChangeListener(
                change -> message.setText("password changed"));

        addCard("Disabled password field", passwordField, message);
    }

    private void addTextAreaVariantsFeature() {
        // begin-source-example
        // source-example-heading: Theme variants usage
        TextArea textArea = new TextArea();
        textArea.addThemeVariants(TextAreaVariant.LUMO_SMALL);
        // end-source-example

        addVariantsDemo(TextArea::new,
                GeneratedVaadinTextArea::addThemeVariants,
                GeneratedVaadinTextArea::removeThemeVariants,
                TextAreaVariant::getVariantName, TextAreaVariant.LUMO_SMALL);
    }

    private void addTextAreaMaxHeightFeature() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Text area with max-height
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area growing stops at 125px");
        textArea.getStyle().set("maxHeight", "125px");
        // end-source-example

        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-max-height");

        addCard("Text area with max-height", textArea, message);
    }

    private void addTextAreaMinHeightFeature() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Text area with min-height
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area won't shrink under 125px");
        textArea.getStyle().set("minHeight", "125px");
        // end-source-example

        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-min-height");

        addCard("Text area with min-height", textArea, message);
    }

    private void addTextAreaBasicFeatures() {
        Div message = new Div();

        // begin-source-example
        // source-example-heading: Basic text area
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        textArea.addValueChangeListener(event -> message.setText(
                String.format("Text area value changed from '%s' to '%s'",
                        event.getOldValue(), event.getValue())));
        // end-source-example

        textArea.setId("text-area-with-value-change-listener");
        message.setId("text-area-value");

        addCard("Basic text area", textArea,
                new ValueChangeModeButtonProvider(textArea)
                        .getValueChangeModeRadios(),
                message);
    }

    private void addTextAreaDisabledField() {
        // begin-source-example
        // source-example-heading: Disabled text area
        TextArea textArea = new TextArea();
        textArea.setLabel("Text area label");
        textArea.setPlaceholder("placeholder text");
        textArea.setEnabled(false);
        // end-source-example

        textArea.setId("disabled-text-area");
        Div message = new Div();
        message.setId("disabled-text-area-message");
        textArea.addValueChangeListener(
                change -> message.setText("Value changed"));

        addCard("Disabled text area", textArea, message);
    }
}
