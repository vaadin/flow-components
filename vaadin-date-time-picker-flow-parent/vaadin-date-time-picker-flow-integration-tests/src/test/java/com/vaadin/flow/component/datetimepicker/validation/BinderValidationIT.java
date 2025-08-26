/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.flow.component.datetimepicker.validation;

import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.BAD_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.EXPECTED_VALUE_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.INCOMPLETE_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.MAX_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.MIN_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.REQUIRED_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BinderValidationPage.UNEXPECTED_VALUE_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-time-picker/validation/binder")
public class BinderValidationIT extends AbstractDateTimePickerValidationIT {

    @Test
    public void fieldIsInitiallyValid() {
        assertValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerDateInputBlur_assertValidity() {
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValid();
        assertErrorMessage(null);
        assertNoValidation();
    }

    @Test
    public void required_triggerTimeInputBlur_assertValidity() {
        getTimeInput().sendKeys(Keys.TAB);
        assertValid();
        assertErrorMessage(null);
        assertNoValidation();
    }

    @Test
    public void required_changeAndClearValueWithoutBlur_triggerBlur_assertValidity() {
        getDateInput().sendKeys("1/1/2000", Keys.ENTER);
        getDateInput().sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME),
                Keys.BACK_SPACE);
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T12:00",
                Keys.ENTER);

        setValue("1/1/2000", "12:00");
        assertValidation(true, "");

        setInputValue(getDateInput(), "");
        assertValidation(false, INCOMPLETE_INPUT_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "");
        assertValidation(false, REQUIRED_ERROR_MESSAGE);

        setInputValue(getDateInput(), "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        setInputValue(getDateInput(), "");
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-03-03T11:00",
                Keys.ENTER);

        setInputValue(getDateInput(), "1/1/2000");
        assertValidation(false, MIN_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "10:00");
        assertValidation(false, MIN_ERROR_MESSAGE);

        setInputValue(getDateInput(), "2/2/2000");
        assertValidation(false, MIN_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "12:00");
        assertValidation(false, UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "13:00");
        assertValidation(false, UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(getDateInput(), "3/3/2000");
        assertValidation(false, UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "11:00");
        assertValid();
        assertValidationCount(1);
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T13:00",
                Keys.ENTER);

        setInputValue(getDateInput(), "3/3/2000");
        assertValidation(false, MAX_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "13:00");
        assertValidation(false, MAX_ERROR_MESSAGE);

        setInputValue(getDateInput(), "2/2/2000");
        assertValidation(false, MAX_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "12:00");
        assertValidation(false, UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "11:00");
        assertValidation(false, UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(getDateInput(), "1/1/2000");
        assertValidation(false, UNEXPECTED_VALUE_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "13:00");
        assertValid();
        assertValidationCount(1);
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T10:00",
                Keys.ENTER);

        setValue("1/1/2000", "10:00");
        assertValidation(true, "");

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(false, REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        $("input").id(EXPECTED_VALUE_INPUT).sendKeys("2000-01-01T10:00",
                Keys.ENTER);

        setValue("1/1/2000", "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "10:00");
        assertValidation(true, "");

        setInputValue(getDateInput(), "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        setInputValue(getDateInput(), "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(false, REQUIRED_ERROR_MESSAGE);
    }
}
