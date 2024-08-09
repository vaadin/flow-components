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
package com.vaadin.flow.component.combobox.test.validation;

import java.util.List;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-combo-box/validation/binder")
public class ComboBoxBinderValidationPage
        extends AbstractValidationPage<ComboBox<String>> {
    public static final String ENABLE_CUSTOM_VALUE_BUTTON = "enable-custom-value-button";
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

    public ComboBoxBinderValidationPage() {
        super();

        binder = new Binder<>(Bean.class);
        binder.forField(testField).asRequired(REQUIRED_ERROR_MESSAGE)
                .withValidator(value -> value.equals(expectedValue),
                        UNEXPECTED_VALUE_ERROR_MESSAGE)
                .bind("property");
        binder.addStatusChangeListener(event -> {
            incrementServerValidationCounter();
        });

        add(createButton(ENABLE_CUSTOM_VALUE_BUTTON, "Enable custom values",
                event -> {
                    testField.setAllowCustomValue(true);
                    testField.addCustomValueSetListener(e -> {
                        testField.setValue(e.getDetail());
                    });
                }));

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected value", event -> {
            expectedValue = event.getValue();
        }));
    }

    @Override
    protected ComboBox<String> createTestField() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setItems(List.of("foo", "bar", "baz"));

        return comboBox;
    }
}
