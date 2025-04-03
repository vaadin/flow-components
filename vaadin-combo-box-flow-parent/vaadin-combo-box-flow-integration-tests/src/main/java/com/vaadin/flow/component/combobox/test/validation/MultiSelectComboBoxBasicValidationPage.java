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
package com.vaadin.flow.component.combobox.test.validation;

import java.util.List;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBoxI18n;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-multi-select-combo-box/validation/basic")
public class MultiSelectComboBoxBasicValidationPage
        extends AbstractValidationPage<MultiSelectComboBox<String>> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";
    public static final String ENABLE_CUSTOM_VALUE_BUTTON = "enable-custom-value-button";

    public MultiSelectComboBoxBasicValidationPage() {
        testField.setI18n(new MultiSelectComboBoxI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));

        add(createButton(ENABLE_CUSTOM_VALUE_BUTTON, "Enable custom values",
                event -> {
                    testField.setAllowCustomValue(true);
                    testField.addCustomValueSetListener(e -> {
                        testField.select(e.getDetail());
                    });
                }));
    }

    @Override
    protected MultiSelectComboBox<String> createTestField() {
        MultiSelectComboBox<String> comboBox = new MultiSelectComboBox<>() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
        comboBox.setItems(List.of("foo", "bar", "baz"));

        return comboBox;
    }
}
