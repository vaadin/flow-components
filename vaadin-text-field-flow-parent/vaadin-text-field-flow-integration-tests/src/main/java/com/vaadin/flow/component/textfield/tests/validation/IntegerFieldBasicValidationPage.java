/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-integer-field/validation/basic")
public class IntegerFieldBasicValidationPage
        extends AbstractValidationPage<IntegerField> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String STEP_INPUT = "step-input";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String BAD_INPUT_ERROR_MESSAGE = "Number has incorrect format";
    public static final String MIN_ERROR_MESSAGE = "Number is too small";
    public static final String MAX_ERROR_MESSAGE = "Number is too big";
    public static final String STEP_ERROR_MESSAGE = "Number does not match the step";

    public IntegerFieldBasicValidationPage() {
        super();

        testField.setI18n(new IntegerField.IntegerFieldI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE)
                .setBadInputErrorMessage(BAD_INPUT_ERROR_MESSAGE)
                .setMinErrorMessage(MIN_ERROR_MESSAGE)
                .setMaxErrorMessage(MAX_ERROR_MESSAGE)
                .setStepErrorMessage(STEP_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));

        add(createInput(STEP_INPUT, "Set step", event -> {
            int value = Integer.parseInt(event.getValue());
            testField.setStep(value);
        }));

        add(createInput(MIN_INPUT, "Set min", event -> {
            int value = Integer.parseInt(event.getValue());
            testField.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max", event -> {
            int value = Integer.parseInt(event.getValue());
            testField.setMax(value);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));
    }

    @Override
    protected IntegerField createTestField() {
        return new IntegerField() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
    }
}
