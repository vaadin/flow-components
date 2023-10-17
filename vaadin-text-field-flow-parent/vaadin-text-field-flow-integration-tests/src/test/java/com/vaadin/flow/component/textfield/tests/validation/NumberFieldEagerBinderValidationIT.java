/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-number-field/validation/eager-binder")
public class NumberFieldEagerBinderValidationIT
        extends AbstractValidationIT<NumberFieldElement> {
    @Test
    public void enterChars_fieldValidatesOnEveryChar() {
        // Entered: -
        testField.sendKeys("-");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        resetValidationCount();

        // Entered: -2
        testField.sendKeys("2");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        resetValidationCount();

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        resetValidationCount();

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    protected NumberFieldElement getTestField() {
        return $(NumberFieldElement.class).first();
    }
}
