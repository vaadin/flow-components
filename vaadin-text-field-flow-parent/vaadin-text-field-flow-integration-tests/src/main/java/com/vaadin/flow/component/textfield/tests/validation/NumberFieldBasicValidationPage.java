/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.tests.validation;

import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-number-field/validation/basic")
public class NumberFieldBasicValidationPage
        extends AbstractValidationPage<NumberField> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String STEP_INPUT = "step-input";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public NumberFieldBasicValidationPage() {
        super();

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));

        add(createInput(STEP_INPUT, "Set step", event -> {
            double value = Double.parseDouble(event.getValue());
            testField.setStep(value);
        }));

        add(createInput(MIN_INPUT, "Set min", event -> {
            double value = Double.parseDouble(event.getValue());
            testField.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max", event -> {
            double value = Double.parseDouble(event.getValue());
            testField.setMax(value);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));
    }

    protected NumberField createTestField() {
        return new NumberField();
    }
}
