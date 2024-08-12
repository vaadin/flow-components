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

import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.BAD_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.MAX_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.MIN_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.RESET_BEAN_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.BinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-date-picker/validation/binder")
public class BinderValidationIT
        extends AbstractValidationIT<DatePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("1/1/2022");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);

        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.setInputValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_setValue_resetBean_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        $("button").id(RESET_BEAN_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-04-01", Keys.ENTER);

        // Constraint validation fails:
        testField.setInputValue("2/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setInputValue("3/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setInputValue("4/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        // Binder validation fails:
        testField.setInputValue("");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-02-01", Keys.ENTER);

        // Constraint validation fails:
        testField.setInputValue("4/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_ERROR_MESSAGE);

        // Binder validation fails:
        testField.setInputValue("3/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(UNEXPECTED_VALUE_ERROR_MESSAGE);

        // Both validations pass:
        testField.setInputValue("2/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        // Binder validation fails:
        testField.setInputValue("");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.setInputValue("1/1/2022");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.setInputValue("");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2022-01-01", Keys.ENTER);

        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Override
    protected DatePickerElement getTestField() {
        return $(DatePickerElement.class).first();
    }
}
