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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.TIMEOUT;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.VALIDATION_LOG;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.RESET_VALIDATION_LOG_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SET_EAGER_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SET_LAZY_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SET_TIMEOUT_MODE_BUTTON;

@TestPath("vaadin-number-field/validation/value-change-mode/basic")
public class NumberFieldValueChangeModeBasicValidationIT
        extends AbstractValidationIT<NumberFieldElement> {

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
        testField.sendKeys("2");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid", "valid");

        // Entered: -
        testField.sendKeys("-");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        testField.sendKeys("2");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("invalid", "invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");
    }

    @Test
    public void lazyMode_enterMultipleChars_assertValidityAndTimeout() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();

        // Entered: -2
        testField.sendKeys("-2");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid", "valid");

        // Entered: --
        testField.sendKeys("--");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        testField.sendKeys(Keys.BACK_SPACE, "2");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        testField.sendKeys(Keys.BACK_SPACE, "-");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("invalid", "invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");
    }

    @Test
    public void timeoutMode_enterOneChar_assertValidity()
            throws InterruptedException {
        $("button").id(SET_TIMEOUT_MODE_BUTTON).click();

        // Entered: 2
        testField.sendKeys("2");
        assertValidationResults("valid");

        Thread.sleep(TIMEOUT);

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid", "valid");

        Thread.sleep(TIMEOUT);

        // Entered: -
        testField.sendKeys("-");
        assertValidationResults("invalid");

        Thread.sleep(TIMEOUT);

        // Entered: -2
        testField.sendKeys("2");
        assertValidationResults("valid");

        Thread.sleep(TIMEOUT);

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid", "invalid");

        Thread.sleep(TIMEOUT);

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("valid");
    }

    @Test
    public void timeoutMode_enterMultipleChars_assertValidityAndTimeout()
            throws InterruptedException {
        $("button").id(SET_TIMEOUT_MODE_BUTTON).click();

        // Entered: -2
        testField.sendKeys("-");
        assertValidationResults("invalid");
        testField.sendKeys("2");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid", "invalid");
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        testField.sendKeys("-");
        assertValidationResults("invalid");
        testField.sendKeys("-");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("invalid");

        // Entered: -2
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys("2");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");

        // Entered: --
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid", "invalid");
        testField.sendKeys("-");
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationResults("invalid");
        testField.sendKeys(Keys.BACK_SPACE);
        assertValidationTimeout(TIMEOUT);
        assertValidationResults("valid");
    }

    protected List<String> getValidationResults() {
        return $("div").id(VALIDATION_LOG).$("div").all().stream()
                .map(record -> record.getText()).toList();
    }

    protected void assertValidationTimeout(int expected) {
        long start = System.currentTimeMillis();

        // Wait for validation
        waitUntil(e -> !getValidationResults().isEmpty());

        long actual = System.currentTimeMillis() - start;

        Assert.assertTrue("The validation was triggered in " + actual
                + "ms (expected " + expected + "ms)", actual >= expected);
    }

    protected void assertValidationResults(String... expectedResults) {
        Assert.assertEquals(Arrays.asList(expectedResults),
                getValidationResults());
        resetValidationLog();
    }

    protected void resetValidationLog() {
        $("button").id(RESET_VALIDATION_LOG_BUTTON).click();
    }

    @Override
    protected NumberFieldElement getTestField() {
        return $(NumberFieldElement.class).first();
    }
}
