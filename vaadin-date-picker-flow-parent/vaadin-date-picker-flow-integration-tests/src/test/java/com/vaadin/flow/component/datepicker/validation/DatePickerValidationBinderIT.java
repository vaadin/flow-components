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
package com.vaadin.flow.component.datepicker.validation;

import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.validation.DatePickerValidationBinderPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-date-picker/validation/binder")
public class DatePickerValidationBinderIT
        extends AbstractValidationIT<DatePickerElement> {
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
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        testField.setInputValue("");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeInputValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-04-01", Keys.ENTER);

        // Constraint validation fails:
        testField.setInputValue("2/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setInputValue("3/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setInputValue("4/1/2022");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-02-01", Keys.ENTER);

        // Constraint validation fails:
        testField.setInputValue("4/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage("");

        // Binder validation fails:
        testField.setInputValue("3/1/2022");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setInputValue("2/1/2022");
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeInputValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage("");

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    protected DatePickerElement getTestField() {
        return $(DatePickerElement.class).first();
    }
}
