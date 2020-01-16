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

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.GeneratedVaadinTextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.component.textfield.demo.entity.Person;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.RegexpValidator;
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
        textFieldBasic(); // TextField
        textFieldDisabledReadonly();
        textFieldAutoselect();
        textFieldClearButton();
        textFieldFocusShortcut();
        passwordFieldBasic(); // PasswordField
        passwordFieldHideRevealButton();
        emailFieldBasic(); // EmailField
        numberFieldBasic(); // NumberField
        integerField();
        numberFieldWithControls();
        numberFieldWithValueLimits();
        numberFieldWithStep();
        bigDecimalField();
        textAreaBasic(); // TextArea
        textAreaMaxHeight();
        textAreaMinHeight();
        prefixAndSuffix(); // Prefix and suffix
        prefixAndSuffixSearch();
        validationMinMaxLength(); // Validation
        validationPattern();
        customValidation();
        themeVariantsTextAlign(); // Theme Variants
        themeVariantsSmallSize();
        styling(); // Styling
    }

    private void textFieldBasic() {
        Div div = new Div();

        // begin-source-example
        // source-example-heading: Basic text field
        TextField labelField = new TextField();
        labelField.setLabel("Label");

        TextField placeholderField = new TextField();
        placeholderField.setPlaceholder("Placeholder");

        TextField valueField = new TextField();
        valueField.setValue("Value");
        // end-source-example

        labelField.setId("text-field-label-id");
        placeholderField.setId("text-field-placeholder-id");
        valueField.setId("text-field-value");
        div.add(labelField, new Text(" "), placeholderField, new Text(" "),
                valueField);
        addCard("Text field", "Basic text field", div);
    }

    private void textFieldDisabledReadonly() {
        Div div = new Div();

        // begin-source-example
        // source-example-heading: Disabled and read-only
        TextField disabledField = new TextField();
        disabledField.setValue("Value");
        disabledField.setLabel("Disabled");
        disabledField.setEnabled(false);

        TextField readonlyField = new TextField();
        readonlyField.setValue("Value");
        readonlyField.setLabel("Read-only");
        readonlyField.setReadOnly(true);

        // end-source-example
        div.add(disabledField, new Text(" "), readonlyField);
        disabledField.setId("text-field-disabled-id");
        readonlyField.setId("text-field-readonly-id");
        addCard("Text field", "Disabled and read-only", div);
    }

    private void textFieldAutoselect() {
        // begin-source-example
        // source-example-heading: Autoselect
        TextField textField = new TextField();
        textField.setLabel("Autoselect");
        textField.setValue("Text selected on focus");
        textField.setAutoselect(true);

        // end-source-example
        textField.setId("autoselect-id");
        addCard("Text field", "Autoselect", textField);
    }

    private void textFieldClearButton() {
        // begin-source-example
        // source-example-heading: Display the clear button
        TextField textField = new TextField();
        textField.setValue("Value");
        textField.setClearButtonVisible(true);
        // end-source-example

        textField.setId("text-field-clear-button-id");
        addCard("Text field", "Display the clear button", textField);
    }

    private void textFieldFocusShortcut() {
        // begin-source-example
        // source-example-heading: Focus shortcut usage
        TextField textField = new TextField();
        textField.setLabel("Press ALT + 1 to focus");
        textField.addFocusShortcut(Key.DIGIT_1, KeyModifier.ALT);
        // end-source-example

        textField.setId("shortcut-field");
        this.addCard("Text field", "Focus shortcut usage", textField);
    }

    private void passwordFieldBasic() {
        // begin-source-example
        // source-example-heading: Basic password field
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password");
        passwordField.setPlaceholder("Enter password");
        passwordField.setValue("secret1");
        // end-source-example

        passwordField.setId("password-field-id");
        addCard("Password Field", "Basic password field", passwordField);
    }

    private void passwordFieldHideRevealButton() {
        // begin-source-example
        // source-example-heading: Hide the reveal button
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password");
        passwordField.setValue("secret1");
        passwordField.setRevealButtonVisible(false);
        // end-source-example

        passwordField.setId("hidden-reveal-button-id");
        addCard("Password Field", "Hide the reveal button", passwordField);
    }

    private void emailFieldBasic() {
        // begin-source-example
        // source-example-heading: Basic email field
        EmailField emailField = new EmailField("Email");
        emailField.setClearButtonVisible(true);
        emailField.setErrorMessage("Please enter a valid email address");
        // end-source-example

        emailField.setId("email-field");
        addCard("Email field", "Basic email field", emailField);
    }

    private void numberFieldBasic() {
        // begin-source-example
        // source-example-heading: Basic number field
        NumberField numberField = new NumberField("Years of expertise");
        // end-source-example

        numberField.setId("number-field-id");
        addCard("Number field", "Basic number field", numberField);
    }

    private void integerField() {
        // begin-source-example
        // source-example-heading: Integer field
        IntegerField integerField = new IntegerField("Age");
        // end-source-example

        integerField.setId("integer-field");
        addCard("Number field", "Integer field", integerField);
    }

    private void numberFieldWithControls() {
        // begin-source-example
        // source-example-heading: Number field with controls
        NumberField numberField = new NumberField();
        numberField.setHasControls(true);
        // end-source-example

        numberField.setId("number-field-has-control-id");
        addCard("Number field", "Number field with controls", numberField);
    }

    private void numberFieldWithValueLimits() {
        // begin-source-example
        // source-example-heading: Number field with value limits
        NumberField numberField = new NumberField();
        numberField.setValue(1d);
        numberField.setHasControls(true);
        numberField.setMin(1);
        numberField.setMax(10);
        // end-source-example

        numberField.setId("number-field-limit-id");
        addCard("Number field", "Number field with value limits", numberField);
    }

    private void numberFieldWithStep() {
        // begin-source-example
        // source-example-heading: Number field with step
        NumberField numberField = new NumberField();
        numberField.setHasControls(true);
        numberField.setStep(0.2d);
        numberField.setMin(0);
        numberField.setMax(10);
        // end-source-example

        numberField.setId("number-field-step-id");
        addCard("Number field", "Number field with step", numberField);
    }

    private void bigDecimalField() {
        // begin-source-example
        // source-example-heading: Big decimal field
        BigDecimalField bigDecimalField = new BigDecimalField("Total cost");
        bigDecimalField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        bigDecimalField.setPrefixComponent(new Icon(VaadinIcon.DOLLAR));

        Paragraph tax = new Paragraph();

        bigDecimalField.addValueChangeListener(e -> {
            BigDecimal taxValue;
            if (e.getValue() == null) {
                taxValue = BigDecimal.ZERO;
            } else {
                taxValue = e.getValue().multiply(new BigDecimal("0.24"))
                        .setScale(2, RoundingMode.HALF_EVEN);
            }
            tax.setText("VAT 24%: $" + taxValue);
        });

        bigDecimalField.setValue(new BigDecimal(15).setScale(2));
        // end-source-example
        addCard("Number field", "Big decimal field", bigDecimalField, tax);
    }

    private void textAreaBasic() {
        // begin-source-example
        // source-example-heading: Basic text area
        TextArea textArea = new TextArea("Description");
        textArea.setPlaceholder("Write here ...");
        // end-source-example

        textArea.setId("text-area-basic-id");
        addCard("Text Area", "Basic text area", textArea);
    }

    private void textAreaMaxHeight() {
        // begin-source-example
        // source-example-heading: Maximum height
        TextArea textArea = new TextArea("Description");
        textArea.getStyle().set("maxHeight", "150px");
        textArea.setPlaceholder("Write here ...");
        // end-source-example

        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-max-height");
        addCard("Text Area", "Maximum height", textArea);
    }

    private void textAreaMinHeight() {
        // begin-source-example
        // source-example-heading: Minimum height
        TextArea textArea = new TextArea("Description");
        textArea.getStyle().set("minHeight", "150px");
        textArea.setPlaceholder("Write here ...");
        // end-source-example

        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-min-height");
        addCard("Text Area", "Minimum height", textArea);
    }

    private void prefixAndSuffix() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Currency field
        NumberField dollarField = new NumberField("Dollars");
        dollarField.setPrefixComponent(new Span("$"));

        NumberField euroField = new NumberField("Euros");
        euroField.setSuffixComponent(new Span("€"));
        // end-source-example

        dollarField.setId("dollar-field");
        euroField.setId("euro-field");
        div.add(dollarField, new Text(" "), euroField);
        addCard("Prefix and suffix", "Currency field", div);
    }

    private void prefixAndSuffixSearch() {
        // begin-source-example
        // source-example-heading: Search field
        TextField textField = new TextField();
        textField.setPlaceholder("Search");
        Icon icon = VaadinIcon.SEARCH.create();
        textField.setPrefixComponent(icon);
        // end-source-example

        textField.setId("text-field-search-id");
        icon.setId("icon-id");
        addCard("Prefix and suffix", "Search field", textField);
    }

    private void validationMinMaxLength() {
        Person person = new Person();
        Binder<Person> binder = new Binder<>();
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Minlength and maxlength
        TextField minField = new TextField("Min 2 characters");
        minField.setMinLength(2);
        binder.forField(minField)
                .withValidator(min -> min.length() >= 2, "Minimum 2 characters")
                .bind(Person::getName, Person::setName);

        TextField maxField = new TextField("Max 4 characters");
        maxField.setMaxLength(4);
        binder.forField(maxField)
                .withValidator(max -> max.length() <= 4, "Maximum 4 characters")
                .bind(Person::getName, Person::setName);
        binder.setBean(person);
        // end-source-example

        minField.setId("min-id");
        maxField.setId("max-id");
        div.add(minField, new Text(" "), maxField);
        addCard("Validation", "Minlength and maxlength", div);
    }

    private void validationPattern() {
        Div div = new Div();
        Person person = new Person();
        Binder<Person> binder = new Binder<>();
        // begin-source-example
        // source-example-heading: Pattern
        TextField textField = new TextField("Flight number");
        binder.forField(textField)
                .withValidator(new RegexpValidator("Not a valid flight number",
                        "[A-Z]{2}\\d{3,4}"))
                .bind(Person::getFlightNumber, Person::setFlightNumber);
        binder.setBean(person);
        // end-source-example

        div.setText("Valid flight number: 2 uppercase letters followed by "
                + "3 or 4 numbers. For example: SA1234");
        textField.setId("text-field-pattern-id");
        addCard("Validation", "Pattern", textField, div);
    }

    private void customValidation() {
        Div div = new Div();
        Person person = new Person();
        Binder<Person> binder = new Binder<>();
        // begin-source-example
        // source-example-heading: Custom validator
        TextField textField = new TextField("ID");
        binder.forField(textField)
                .withValidator((Validator<String>) (value, context) -> {

                    long sumDigits = 0;
                    long intValue = 0;

                    try {
                        intValue = Long.valueOf(value);
                    } catch (NumberFormatException ex) {
                        return ValidationResult.error("Is not a valid number");
                    }

                    if (value.length() != 10) {
                        return ValidationResult
                                .error("Length must be 10 digits");
                    }

                    while (intValue > 0) {
                        sumDigits += intValue % 10;
                        intValue = intValue / 10;
                    }

                    if (sumDigits % 10 != 0) {
                        return ValidationResult.error("ID is not correct");
                    }

                    return ValidationResult.ok();
                }).bind(Person::getId, Person::setId);
        binder.setBean(person);
        // end-source-example
        div.setText(
                "Valid ID: Use a 10 digit number. The sum of digits must be divisible by 10. For example 1111111111.");
        textField.setId("text-field-synchronous-id");
        addCard("Validation", "Custom validator", textField, div);
    }

    private void themeVariantsTextAlign() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Text align
        TextField leftTextField = new TextField();
        leftTextField.setValue("left");

        TextField centerTextField = new TextField();
        centerTextField.setValue("center");
        centerTextField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_CENTER);

        TextField rightTextField = new TextField();
        rightTextField.setValue("right");
        rightTextField.addThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT);
        // end-source-example

        leftTextField.setId("text-field-left-id");
        centerTextField.setId("text-field-center-id");
        rightTextField.setId("text-field-right-id");
        div.add(leftTextField, new Text(" "), centerTextField, new Text(" "),
                rightTextField);
        addCard("Theme Variants", "Text align", div);
    }

    private void themeVariantsSmallSize() {
        // begin-source-example
        // source-example-heading: Small size
        TextField textField = new TextField("Label");
        textField.setPlaceholder("Text field");
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        // end-source-example
        addCard("Theme Variants", "Small size", textField);
    }

    private void styling() {

        Div firstDiv = new Div();
        firstDiv.setText(
                "To read about styling you can read the related tutorial in");
        Anchor firstAnchor = new Anchor("https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes");

        Div secondDiv = new Div();
        secondDiv.setText("To know about styling in html you can read the ");
        Anchor secondAnchor = new Anchor(
                "https://vaadin.com/components/" +
                        "vaadin-text-field/html-examples/text-field-styling-demos",
                "HTML Styling Demos");

        HorizontalLayout firstHorizontalLayout = new HorizontalLayout(firstDiv,
                firstAnchor);
        HorizontalLayout secondHorizontalLayout = new HorizontalLayout(
                secondDiv, secondAnchor);
        // begin-source-example
        // source-example-heading: Styling references

        // end-source-example
        addCard("Styling", "Styling references", firstHorizontalLayout,
                secondHorizontalLayout);
    }
}

