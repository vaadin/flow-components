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

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-integer-field/validation/binder")
public class IntegerFieldBinderValidationPage
        extends AbstractValidationPage<IntegerField> {
    public static final String STEP_INPUT = "step-input";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";
    public static final String RESET_BEAN_BUTTON = "reset-bean-button";

    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String BAD_INPUT_ERROR_MESSAGE = "Number has incorrect format";
    public static final String MIN_ERROR_MESSAGE = "Number is too small";
    public static final String MAX_ERROR_MESSAGE = "Number is too big";
    public static final String STEP_ERROR_MESSAGE = "Number does not match the step";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "Number does not match the expected value";

    public static class Bean {
        private Integer property;

        public Integer getProperty() {
            return property;
        }

        public void setProperty(Integer property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    private Integer expectedValue;

    public IntegerFieldBinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });

        testField.setI18n(new IntegerField.IntegerFieldI18n()
                .setBadInputErrorMessage(BAD_INPUT_ERROR_MESSAGE)
                .setMinErrorMessage(MIN_ERROR_MESSAGE)
                .setMaxErrorMessage(MAX_ERROR_MESSAGE)
                .setStepErrorMessage(STEP_ERROR_MESSAGE));

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected value", event -> {
            expectedValue = Integer.parseInt(event.getValue());
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

        add(createButton(RESET_BEAN_BUTTON, "Reset bean", event -> {
            binder.setBean(new Bean());
        }));
    }

    @Override
    protected IntegerField createTestField() {
        return new IntegerField();
    }
}
