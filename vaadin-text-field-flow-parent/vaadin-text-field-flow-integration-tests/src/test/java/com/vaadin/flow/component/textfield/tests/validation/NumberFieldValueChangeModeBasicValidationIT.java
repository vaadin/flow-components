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

import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SERVER_VALIDITY_STATE_LOG;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.RESET_SERVER_VALIDITY_STATE_LOG_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SET_EAGER_MODE_BUTTON;
import static com.vaadin.flow.component.textfield.tests.validation.NumberFieldValueChangeModeBasicValidationPage.SET_LAZY_MODE_BUTTON;

@TestPath("vaadin-number-field/validation/value-change-mode/basic")
public class NumberFieldValueChangeModeBasicValidationIT
        extends AbstractValidationIT<NumberFieldElement> {
    @Test
    public void eagerMode_enterChars_assertValidity() {
        $("button").id(SET_EAGER_MODE_BUTTON).click();

        // Entered: -
        testField.sendKeys("-");
        assertServerValidityStateLog("invalid");

        // Entered: -2
        testField.sendKeys("2");
        assertServerValidityStateLog("valid");

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertServerValidityStateLog("invalid", "invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertServerValidityStateLog("valid");

        // Entered: 2
        testField.sendKeys("2");
        assertServerValidityStateLog("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertServerValidityStateLog("valid", "valid");
    }

    @Test
    public void lazyMode_enterOneChar_wait_assertValidity() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();

        // Entered: -
        testField.sendKeys("-");
        assertServerValidityStateLog("invalid");

        // Entered: -2
        testField.sendKeys("2");
        assertServerValidityStateLog("valid");

        // Entered: -
        testField.sendKeys(Keys.BACK_SPACE);
        assertServerValidityStateLog("invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertServerValidityStateLog("valid");

        // Entered: 2
        testField.sendKeys("2");
        assertServerValidityStateLog("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE);
        assertServerValidityStateLog("valid", "valid");
    }

    @Test
    public void lazyMode_enterMultipleChars_wait_assertValidity() {
        $("button").id(SET_LAZY_MODE_BUTTON).click();

        // Entered: --
        testField.sendKeys("--");
        assertServerValidityStateLog("invalid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertServerValidityStateLog("valid", "valid");

        // Entered: -2
        testField.sendKeys("-2");
        assertServerValidityStateLog("valid");

        // Entered:
        testField.sendKeys(Keys.BACK_SPACE, Keys.BACK_SPACE);
        assertServerValidityStateLog("valid", "valid");
    }

    protected List<String> getServerValidityStates() {
        return $("div").id(SERVER_VALIDITY_STATE_LOG).$("div").all().stream().map(record -> record.getText()).toList();
    }

    protected void assertServerValidityStateLog(String... expectedStates) {
        Assert.assertEquals(Arrays.asList(expectedStates), getServerValidityStates());
        resetServerValidityStateLog();
    }

    protected void resetServerValidityStateLog() {
        $("button").id(RESET_SERVER_VALIDITY_STATE_LOG_BUTTON).click();
    }

    @Override
    protected NumberFieldElement getTestField() {
        return $(NumberFieldElement.class).first();
    }
}
