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

import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.timepicker.tests.validation.TimePickerValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-time-picker/validation/binder")
public class TimePickerValidationBinderIT
        extends AbstractValidationIT<TimePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerInputBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("12:00", Keys.ENTER);

        // Constraint validation fails:
        testField.selectByText("10:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.selectByText("11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.selectByText("12:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        // Constraint validation fails:
        testField.selectByText("12:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.selectByText("11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.selectByText("10:00");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("10:00", Keys.ENTER);

        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();

        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.selectByText("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    protected TimePickerElement getTestField() {
        return $(TimePickerElement.class).first();
    }
}
