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
package com.vaadin.flow.component.checkbox.tests.validation;

import java.util.List;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox-group/validation/basic")
public class BasicValidationPage
        extends AbstractValidationPage<CheckboxGroup<String>> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";

    public BasicValidationPage() {
        testField.setI18n(new CheckboxGroup.CheckboxGroupI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequired(true);
        }));
    }

    @Override
    protected CheckboxGroup<String> createTestField() {
        CheckboxGroup<String> checkboxGroup = new CheckboxGroup<>() {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
        checkboxGroup.setItems(List.of("foo", "bar", "baz"));
        return checkboxGroup;
    }
}
