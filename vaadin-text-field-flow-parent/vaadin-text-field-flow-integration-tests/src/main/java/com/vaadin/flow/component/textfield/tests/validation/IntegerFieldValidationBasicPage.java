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

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.tests.validation.AbstractValidationPage;

@Route("vaadin-integer-field/validation/basic")
public class IntegerFieldValidationBasicPage
        extends AbstractValidationPage<IntegerField> {
    public static final String REQUIRED_BUTTON = "required-button";
    public static final String STEP_INPUT = "step-input";
    public static final String MIN_INPUT = "min-input";
    public static final String MAX_INPUT = "max-input";
    public static final String CLEAR_VALUE_BUTTON = "clear-value-button";

    public IntegerFieldValidationBasicPage() {
        super();

        add(createButton(REQUIRED_BUTTON, "Enable required", event -> {
            testField.setRequiredIndicatorVisible(true);
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
    }

    protected IntegerField createTestField() {
        return new IntegerField();
    }
}
