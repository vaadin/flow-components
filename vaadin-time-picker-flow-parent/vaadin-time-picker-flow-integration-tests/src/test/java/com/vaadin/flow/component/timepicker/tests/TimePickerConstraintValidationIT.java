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
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.ATTACH_BINDER_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.MAX_VALUE_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.MIN_VALUE_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.SERVER_VALIDITY_STATE;
import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.SERVER_VALIDITY_STATE_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.TimePickerValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

/**
 * Integration tests for validation with binder.
 */
@TestPath("vaadin-time-picker/validation-binder")
public class TimePickerConstraintValidationIT extends AbstractComponentIT {

    private TimePickerElement field;

    @Before
    public void init() {
        open();
        field = $(TimePickerElement.class).waitForFirst();
    }

    @Test
    public void required_fieldIsInitiallyValid() {
        findElement(By.id(REQUIRED_BUTTON)).click();
        assertClientValid(true);
        assertServerValid(true);
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        findElement(By.id(REQUIRED_BUTTON)).click();
        field.sendKeys(Keys.TAB);
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void emptyField_invalidTime_assertValidity() {
        field.sendKeys("INVALID");
        field.sendKeys(Keys.TAB);
        assertClientValid(false);
        assertServerValid(false);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        findElement(By.id(REQUIRED_BUTTON)).click();
        field.setValue("12:00");
        assertServerValid(true);
        assertClientValid(true);

        field.setValue("");
        assertServerValid(false);
        assertClientValid(false);
    }

    @Test
    public void minTime_changeInputValue_assertValidity() {
        findElement(By.id(MIN_VALUE_BUTTON)).click();

        // MIN CONSTRAINT FAILS
        field.setValue("09:00");
        assertClientValid(false);
        assertServerValid(false);

        // VALIDATIONS PASSES
        field.setValue("12:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    @Test
    public void maxTime_changeInputValue_assertValidity() {
        findElement(By.id(MAX_VALUE_BUTTON)).click();

        // MAX CONSTRAINT FAILS
        field.setValue("15:00");
        assertClientValid(false);
        assertServerValid(false);

        // VALIDATIONS PASSES
        field.setValue("12:00");
        assertClientValid(true);
        assertServerValid(true);
    }

    protected void assertErrorMessage(String expected) {
        Assert.assertEquals(expected, field.getPropertyString("errorMessage"));
    }

    protected void assertClientValid(boolean expected) {
        Assert.assertEquals(expected, !field.getPropertyBoolean("invalid"));
    }

    protected void assertServerValid(boolean expected) {
        $("button").id(SERVER_VALIDITY_STATE_BUTTON).click();

        var actual = $("div").id(SERVER_VALIDITY_STATE).getText();
        Assert.assertEquals(String.valueOf(expected), actual);
    }
}
