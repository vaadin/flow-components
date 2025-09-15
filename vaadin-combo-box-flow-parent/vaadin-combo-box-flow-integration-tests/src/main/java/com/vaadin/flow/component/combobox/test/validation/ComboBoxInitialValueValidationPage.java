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

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-combo-box/validation/initial-value")
public class ComboBoxInitialValueValidationPage
        extends AbstractValidationPage<ComboBox<String>> {
    public static final String SET_NULL = "set-null-button";
    public static final String SET_EMPTY_STRING = "set-empty-string-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";

    public ComboBoxInitialValueValidationPage() {
        testField.setI18n(new ComboBox.ComboBoxI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(SET_NULL, "Set null", event -> {
            testField.setValue(null);
        }));

        add(createButton(SET_EMPTY_STRING, "Set empty string", event -> {
            testField.setValue("");
        }));
    }

    @Override
    protected ComboBox<String> createTestField() {
        ComboBox<String> comboBox = new ComboBox<>() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
        comboBox.setItems(List.of("foo", "bar", "baz"));
        comboBox.setRequired(true);

        return comboBox;
    }
}
