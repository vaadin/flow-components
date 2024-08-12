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
package com.vaadin.flow.component.timepicker.tests.validation;

import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.ATTACH_FIELD_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.DETACH_FIELD_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBasicPage.REQUIRED_BUTTON;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-time-picker/validation/basic")
public class TimePickerValidationBasicIT
        extends AbstractValidationIT<TimePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void onlyServerCanSetFieldToValid() {
        $("button").id(REQUIRED_BUTTON).click();

        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys("10:00");
        executeScript("arguments[0].validate()", testField);
        assertClientInvalid();

        testField.sendKeys(Keys.ENTER);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_onlyServerCanSetFieldToValid() {
        $("button").id(DETACH_FIELD_BUTTON).click();
        $("button").id(ATTACH_FIELD_BUTTON).click();

        testField = getTestField();

        onlyServerCanSetFieldToValid();
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("12:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("10:00");
        assertClientInvalid();
        assertServerInvalid();

        testField.selectByText("11:00");
        assertClientValid();
        assertServerValid();

        testField.selectByText("12:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("12:00");
        assertClientInvalid();
        assertServerInvalid();

        testField.selectByText("11:00");
        assertClientValid();
        assertServerValid();

        testField.selectByText("10:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    protected TimePickerElement getTestField() {
        return $(TimePickerElement.class).first();
    }
}
