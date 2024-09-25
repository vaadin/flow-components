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

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-checkbox/validation/basic")
public class CheckboxBasicValidationPage
        extends AbstractValidationPage<Checkbox> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String REQUIRED_ERROR_MESSAGE = "Field is required";

    public CheckboxBasicValidationPage() {
        testField.setI18n(new Checkbox.CheckboxI18n()
                .setRequiredErrorMessage(REQUIRED_ERROR_MESSAGE));

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
        }));
    }

    @Override
    protected Checkbox createTestField() {
        return new Checkbox("Checkbox") {
            @Override
            protected void validate() {
                super.validate();
                incrementServerValidationCounter();
            }
        };
    }
}
