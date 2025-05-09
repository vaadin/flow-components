/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.datetimepicker.validation;

import java.time.LocalDateTime;

import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-date-time-picker/validation/binder")
public class BinderValidationPage
        extends AbstractValidationPage<DateTimePicker> {
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String BAD_INPUT_ERROR_MESSAGE = "Value has incorrect format";
    public static final String INCOMPLETE_INPUT_ERROR_MESSAGE = "Value is incomplete";
    public static final String MIN_ERROR_MESSAGE = "Value is too small";
    public static final String MAX_ERROR_MESSAGE = "Value is too big";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "Value does not match the expected value";

    public static class Bean {
        private LocalDateTime property;

        public LocalDateTime getProperty() {
            return property;
        }

        public void setProperty(LocalDateTime property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    private LocalDateTime expectedValue;

    public BinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");

        testField.setI18n(new DateTimePicker.DateTimePickerI18n()
                .setBadInputErrorMessage(BAD_INPUT_ERROR_MESSAGE)
                .setIncompleteInputErrorMessage(INCOMPLETE_INPUT_ERROR_MESSAGE)
                .setMinErrorMessage(MIN_ERROR_MESSAGE)
                .setMaxErrorMessage(MAX_ERROR_MESSAGE));

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected date time",
                event -> {
                    var value = LocalDateTime.parse(event.getValue());
                    expectedValue = value;
                }));

        add(createInput(MIN_INPUT, "Set min date time", event -> {
            var value = LocalDateTime.parse(event.getValue());
            testField.setMin(value);
        }));

        add(createInput(MAX_INPUT, "Set max date time", event -> {
            var value = LocalDateTime.parse(event.getValue());
            testField.setMax(value);
        }));

        add(createButton(CLEAR_VALUE_BUTTON, "Clear value", event -> {
            testField.clear();
        }));
    }

    protected DateTimePicker createTestField() {
        return new DateTimePicker() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
    }
}
