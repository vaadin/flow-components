/*
 * Copyright 2000-2022 Vaadin Ltd.
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

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;

@Route("vaadin-text-field/validation/binder")
public class TextFieldValidationBinderPage
        extends AbstractValidationPage<TextField> {
    public static final String PATTERN_INPUT = "pattern-input";
    public static final String MIN_LENGTH_INPUT = "min-length-input";
    public static final String MAX_LENGTH_INPUT = "max-length-input";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";

    public static class Bean {
        private String property;

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    private String expectedValue;

    public TextFieldValidationBinderPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(field).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected value", event -> {
            expectedValue = event.getValue();
        }));

        add(createInput(PATTERN_INPUT, "Set pattern", event -> {
            field.setPattern(event.getValue());
        }));

        add(createInput(MIN_LENGTH_INPUT, "Set min length", event -> {
            var value = Integer.parseInt(event.getValue());
            field.setMinLength(value);
        }));

        add(createInput(MAX_LENGTH_INPUT, "Set max length", event -> {
            var value = Integer.parseInt(event.getValue());
            field.setMaxLength(value);
        }));
    }

    protected TextField createField() {
        return new TextField();
    }
}
