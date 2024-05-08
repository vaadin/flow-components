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
package com.vaadin.flow.component.textfield.tests.validation;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.testutil.TestPath;

import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValueChangeModeBasicValidationPage.VALUE_CHANGE_TIMEOUT;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValueChangeModeBasicValidationPage.SET_EAGER_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValueChangeModeBasicValidationPage.SET_LAZY_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.IntegerFieldValueChangeModeBasicValidationPage.SET_TIMEOUT_MODE_BUTTON;

@TestPath("vaadin-integer-field/validation/value-change-mode/basic")
public class IntegerFieldValueChangeModeBasicValidationIT
        extends AbstractValueChangeModeValidationIT<IntegerFieldElement> {

    @Test
    public void eagerMode_enterChars_assertValidity() {
        $("button").id(SET_EAGER_MODE_BUTTON).click();

        // Entered: 2
        testField.sendKeys("2");
        assertValidationResults("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid", "valid");

        // Entered: -
        testField.sendKeys("-");
        assertValidationResults("invalid");

        // Entered: -2
        testField.sendKeys("2");
        assertValidationResults("valid");

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid", "invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid");
    }

    @Test
    public void lazyMode_enterOneChar_assertValidityAndTimeout() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();

        // Entered: 2
        startValidationTimeout();
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid", "valid");

        // Entered: -
        startValidationTimeout();
        testField.sendKeys("-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: -
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid", "invalid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");
    }

    @Test
    public void lazyMode_enterMultipleChars_assertValidityAndTimeout() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys("-2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid", "valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys("--");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, "2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, "-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid", "invalid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");
    }

    @Test
    public void timeoutMode_enterOneChar_assertValidity()
            throws InterruptedException {
        $("button").id(SET_TIMEOUT_MODE_BUTTON).click();

        // Entered: 2
        testField.sendKeys("2");
        assertValidationResults("valid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid", "valid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered: -
        testField.sendKeys("-");
        assertValidationResults("invalid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered: -2
        testField.sendKeys("2");
        assertValidationResults("valid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid", "invalid");

        Thread.sleep(VALUE_CHANGE_TIMEOUT);

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid");
    }

    @Test
    public void timeoutMode_enterMultipleChars_assertValidityAndTimeout()
            throws InterruptedException {
        $("button").id(SET_TIMEOUT_MODE_BUTTON).click();

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys("-");
        assertValidationResults("invalid");
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid", "invalid");
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys("-");
        assertValidationResults("invalid");
        testField.sendKeys("-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys("2");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid", "invalid");
        testField.sendKeys("-");
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("invalid");

        // Entered:
        startValidationTimeout();
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(VALUE_CHANGE_TIMEOUT);
        assertValidationResults("valid");
    }

    @Override
    protected IntegerFieldElement getTestField() {
        return $(IntegerFieldElement.class).first();
    }
}
