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
import java.util.Set;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-multi-select-combo-box/validation/binder")
public class MultiSelectComboBoxBinderValidationPage
        extends AbstractValidationPage<MultiSelectComboBox<String>> {
    public static final String ENABLE_CUSTOM_VALUE_BUTTON = "enable-custom-value-button";
    public static final String EXPECTED_VALUE_INPUT = "expected-value-input";

    public static final String REQUIRED_ERROR_MESSAGE = "The field is required";
    public static final String UNEXPECTED_VALUE_ERROR_MESSAGE = "The field doesn't match the expected value";

    public static class Bean {
        private Set<String> property;

        public Set<String> getProperty() {
            return property;
        }

        public void setProperty(Set<String> property) {
            this.property = property;
        }
    }

    protected Binder<Bean> binder;

    private Set<String> expectedValue;

    public MultiSelectComboBoxBinderValidationPage() {
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
                        testField.select(e.getDetail());
                    });
                }));

        add(createInput(EXPECTED_VALUE_INPUT, "Set expected value", event -> {
            expectedValue = Set.of(event.getValue());
        }));
    }

    @Override
    protected MultiSelectComboBox<String> createTestField() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>();
        comboBox.setItems(List.of("foo", "bar", "baz"));

        return comboBox;
    }
}
