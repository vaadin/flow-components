/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-email-field/validation/basic")
public class EmailFieldBasicValidationPage
        extends AbstractValidationPage<EmailField> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String PATTERN_INPUT = "pattern-input";
    public static final String MIN_LENGTH_INPUT = "min-length-input";
    public static final String MAX_LENGTH_INPUT = "max-length-input";

    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String MIN_LENGTH_ERROR_MESSAGE = "Value is too short";
    public static final String MAX_LENGTH_ERROR_MESSAGE = "Value is too long";
    public static final String PATTERN_ERROR_MESSAGE = "Value has incorrect format";

    public EmailFieldBasicValidationPage() {
        super();

        testField.setI18n(new EmailField.EmailFieldI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE)
                .setMinLengthErrorMessage(MIN_LENGTH_ERROR_MESSAGE)
                .setMaxLengthErrorMessage(MAX_LENGTH_ERROR_MESSAGE)
                .setPatternErrorMessage(PATTERN_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));

        add(createInput(PATTERN_INPUT, "Set pattern", event -> {
            testField.setPattern(event.getValue());
        }));

        add(createInput(MIN_LENGTH_INPUT, "Set min length", event -> {
            int value = Integer.parseInt(event.getValue());
            testField.setMinLength(value);
        }));

        add(createInput(MAX_LENGTH_INPUT, "Set max length", event -> {
            int value = Integer.parseInt(event.getValue());
            testField.setMaxLength(value);
        }));
    }

    @Override
    protected EmailField createTestField() {
        return new EmailField() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
    }
}
