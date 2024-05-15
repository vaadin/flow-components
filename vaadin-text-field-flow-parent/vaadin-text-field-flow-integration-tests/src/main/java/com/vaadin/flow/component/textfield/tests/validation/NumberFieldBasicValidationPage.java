/*
 * Copyright 2000-2024 Vaadin Ltd.
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

    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String BAD_INPUT_ERROR_MESSAGE = "Number has incorrect format";
    public static final String MIN_ERROR_MESSAGE = "Number is too small";
    public static final String MAX_ERROR_MESSAGE = "Number is too big";
    public static final String STEP_ERROR_MESSAGE = "Number does not match the step";

    public NumberFieldBasicValidationPage() {
        super();

        testField.setI18n(new NumberField.NumberFieldI18n()
            .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE)
            .setBadInputErrorMessage(BAD_INPUT_ERROR_MESSAGE)
            .setMinErrorMessage(MIN_ERROR_MESSAGE)
            .setMaxErrorMessage(MAX_ERROR_MESSAGE)
            .setStepErrorMessage(STEP_ERROR_MESSAGE));

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

    @Override
    protected NumberField createTestField() {
        return new NumberField() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
    }
}
