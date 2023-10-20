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

import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SET_EAGER_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SET_LAZY_MODE_BUTTON;

@TestPath("vaadin-number-field/validation/value-change-mode/binder")
public class NumberFieldValueChangeModeBinderValidationIT
        extends AbstractValidationIT<NumberFieldElement> {
    @Test
    public void eagerMode_enterChars_assertValidity() {
        $("button").id(SET_EAGER_MODE_BUTTON).click();

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

        // Entered: 2
        testField.sendKeys("2");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        resetValidationCount();

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationCount(2);
        assertServerValid();
        assertClientValid();

        resetValidationCount();
    }

    @Test
    public void lazyMode_enterChars() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();
    }

    protected NumberFieldElement getTestField() {
        return $(NumberFieldElement.class).first();
    }
}
