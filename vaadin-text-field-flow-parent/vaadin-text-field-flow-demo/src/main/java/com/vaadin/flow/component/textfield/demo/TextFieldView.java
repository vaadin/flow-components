
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
import com.vaadin.flow.component.textfield.TextAreaVariant;
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
        textFieldSetPattern();
        textFieldHelperText();
        passwordFieldBasic(); // PasswordField
        passwordFieldHideRevealButton();
        emailFieldBasic(); // EmailField
        numberFieldBasic(); // NumberField
        integerField();
        numberFieldWithControls();
        numberFieldWithValueLimits();
        numberFieldWithStep();
        bigDecimalField();
        numberFieldWithHelperText();
        textAreaBasic(); // TextArea
        textAreaMaxHeight();
        textAreaMinHeight();
        textAreaHelperText();
        prefixAndSuffix(); // Prefix and suffix
        prefixAndSuffixSearch();
        validationMinMaxLength(); // Validation
        validationPattern();
        customValidation();
        themeVariantsTextAlign(); // Theme Variants
        themeVariantsSmallSize();
        helperTextVariants();
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

        add(labelField, placeholderField, valueField);
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

        add(disabledField, readonlyField);
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

        add(textField);
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

        add(textField);
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

        add(textField);
        // end-source-example

        textField.setId("shortcut-field");
        this.addCard("Text field", "Focus shortcut usage", textField);
    }

    private void textFieldSetPattern() {
        // begin-source-example
        // source-example-heading: Pattern usage
        TextField zipCode = new TextField("ZIP code");
        zipCode.setPlaceholder("Only 3 letters codes are valid");
        zipCode.setPattern("[A-Za-z]{3}");
        zipCode.setPreventInvalidInput(true);

        Paragraph status = new Paragraph();
        TextField username = new TextField("Username");
        username.setPattern("^[a-zA-Z0-9._-]{3,}");
        username.addValueChangeListener(e -> {
            if (username.isInvalid())
                status.setText("Username should be, at least, 3 character long "
                        + "and contain only letters, digits, dashes or dots.");
            else
                status.setText("Your username seems valid!");
        });
        // end-source-example

        zipCode.setId("text-field-zip-pattern");
        username.setId("text-field-username-pattern");
        addCard("Text field", "Pattern usage", zipCode, username, status);
    }

    private void textFieldHelperText() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Helper Text and Component
        TextField helperFieldText = new TextField("First name");
        helperFieldText.setHelperText("Enter all your first names");

        TextField helperFieldComponent = new TextField("Last Name");
        helperFieldComponent.setHelperComponent(new Span("Family name"));

        add(helperFieldText, helperFieldComponent);
        // end-source-example

        div.add(helperFieldText, new Text("  "), helperFieldComponent);
        addCard("Text field", "Helper Text and Component", div);
    }

    private void passwordFieldBasic() {
        // begin-source-example
        // source-example-heading: Basic password field
        PasswordField passwordField = new PasswordField();
        passwordField.setLabel("Password");
        passwordField.setPlaceholder("Enter password");
        passwordField.setValue("secret1");

        add(passwordField);
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

        add(passwordField);
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

        add(emailField);
        // end-source-example

        emailField.setId("email-field");
        addCard("Email field", "Basic email field", emailField);
    }

    private void numberFieldBasic() {
        // begin-source-example
        // source-example-heading: Basic number field
        NumberField numberField = new NumberField("Years of expertise");

        add(numberField);
        // end-source-example

        numberField.setId("number-field-id");
        addCard("Number field", "Basic number field", numberField);
    }

    private void integerField() {
        // begin-source-example
        // source-example-heading: Integer field
        IntegerField integerField = new IntegerField("Age");

        add(integerField);
        // end-source-example

        integerField.setId("integer-field");
        addCard("Number field", "Integer field", integerField);
    }

    private void numberFieldWithControls() {
        // begin-source-example
        // source-example-heading: Number field with controls
        NumberField numberField = new NumberField();
        numberField.setHasControls(true);

        add(numberField);
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

        add(numberField);
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

        add(numberField);
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

        add(bigDecimalField, tax);
        // end-source-example
        addCard("Number field", "Big decimal field", bigDecimalField, tax);
    }

    private void numberFieldWithHelperText() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Number fields with helper text
        NumberField numberField = new NumberField("Total Salary");
        numberField
                .setHelperText("Any valid number can be put in a NumberField");

        IntegerField integerField = new IntegerField("Your age");
        integerField
                .setHelperText("Only integers can be put in an IntegerField");

        add(numberField, integerField);
        // end-source-example

        numberField.setId("number-field-helper-id");
        integerField.setId("integer-field-helper-id");
        div.add(numberField, new Text(" "), integerField);
        addCard("Number field", "Number fields with helper text", div);
    }

    private void textAreaBasic() {
        // begin-source-example
        // source-example-heading: Basic text area
        TextArea textArea = new TextArea("Description");
        textArea.setPlaceholder("Write here ...");

        add(textArea);
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

        add(textArea);
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

        add(textArea);
        // end-source-example

        textArea.getStyle().set("padding", "0");
        textArea.setId("text-area-with-min-height");
        addCard("Text Area", "Minimum height", textArea);
    }

    private void textAreaHelperText() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Helper text and helper component
        TextArea textAreaHelperText = new TextArea("Overview");
        textAreaHelperText.setPlaceholder("Write here ...");
        textAreaHelperText
                .setHelperText("Short description of your current role");

        TextArea textAreaHelperComponent = new TextArea("Feedback");
        textAreaHelperComponent.setPlaceholder("Write here ...");
        textAreaHelperComponent.setHelperComponent(
                new Span("Here you can share what you've liked and "
                        + "what can be improved in the next lesson"));

        add(textAreaHelperText, textAreaHelperComponent);
        // end-source-example

        textAreaHelperText.setId("text-area-with-helper-text");
        textAreaHelperText.getStyle().set("margin-right", "20px");
        div.add(textAreaHelperText, textAreaHelperComponent);
        addCard("Text Area", "Helper text and helper component", div);
    }

    private void prefixAndSuffix() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Currency field
        NumberField dollarField = new NumberField("Dollars");
        dollarField.setPrefixComponent(new Span("$"));

        NumberField euroField = new NumberField("Euros");
        euroField.setSuffixComponent(new Span("€"));

        add(dollarField, euroField);
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

        add(textField);
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

        add(minField, maxField);
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

        add(textField, div);
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

        add(textField, div);
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

        add(leftTextField, centerTextField, rightTextField);
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

        add(textField);
        // end-source-example
        addCard("Theme Variants", "Small size", textField);
    }

    private void helperTextVariants() {
        Div div = new Div();
        // begin-source-example
        // source-example-heading: Helper Variant

        TextField helperFieldAbove = new TextField();
        helperFieldAbove.setHelperText("Helper Text displayed above the field");
        helperFieldAbove
                .addThemeVariants(TextFieldVariant.LUMO_HELPER_ABOVE_FIELD);

        TextArea textAreaAbove = new TextArea();
        textAreaAbove.setPlaceholder("Write here ...");
        textAreaAbove.setHelperText("Helper Text is above the field");
        textAreaAbove.addThemeVariants(TextAreaVariant.LUMO_HELPER_ABOVE_FIELD);

        add(helperFieldAbove, textAreaAbove);
        // end-source-example

        div.getStyle().set("display", "flex");
        helperFieldAbove.getStyle().set("margin-right", "20px");
        div.add(helperFieldAbove, textAreaAbove);
        addCard("Theme Variants", "Helper Variant", div);
    }

    private void styling() {

        Div firstDiv = new Div();
        firstDiv.setText(
                "To read about styling you can read the related tutorial in");
        Anchor firstAnchor = new Anchor(
                "https://vaadin.com/docs/flow/theme/using-component-themes.html",
                "Using Component Themes");

        Div secondDiv = new Div();
        secondDiv.setText("To know about styling in html you can read the ");
        Anchor secondAnchor = new Anchor("https://vaadin.com/components/"
                + "vaadin-text-field/html-examples/text-field-styling-demos",
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
