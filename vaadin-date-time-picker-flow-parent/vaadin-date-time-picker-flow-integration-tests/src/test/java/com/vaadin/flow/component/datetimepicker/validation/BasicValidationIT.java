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

import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.BAD_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.INCOMPLETE_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.MAX_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.MIN_ERROR_MESSAGE;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.datetimepicker.validation.BasicValidationPage.REQUIRED_ERROR_MESSAGE;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-date-time-picker/validation/basic")
public class BasicValidationIT
        extends AbstractValidationIT<DateTimePickerElement> {
    private TestBenchElement dateInput;
    private TestBenchElement timeInput;

    @Before
    public void init() {
        super.init();
        dateInput = testField.$("input").first();
        timeInput = testField.$("input").last();
    }

    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void triggerBlur_assertValidity() {
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
        assertValidationCount(0);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
        assertValidationCount(0);
    }

    @Test
    public void required_changeInputTemporarily_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();
        dateInput.sendKeys("1", Keys.BACK_SPACE, Keys.ENTER);
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
        assertValidationCount(0);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        setValue("1/1/2000", "12:00");
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(timeInput, "");
        assertServerInvalid();
        assertServerInvalid();
        assertErrorMessage(INCOMPLETE_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(dateInput, "");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
        assertValidationCount(0);

        setInputValue(timeInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "");
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setInputValue(dateInput, "1/1/2000");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "11:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(dateInput, "2/2/2000");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "12:00");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(timeInput, "13:00");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(dateInput, "3/3/2000");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(timeInput, "11:00");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void max_changeDateInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setInputValue(dateInput, "3/3/2000");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "12:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(dateInput, "2/2/2000");
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(timeInput, "13:00");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "12:00");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(timeInput, "11:00");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(dateInput, "1/1/2000");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(timeInput, "13:00");
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        setValue("1/1/2000", "10:00");
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        setValue("1/1/2000", "INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "10:00");
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);

        setInputValue(dateInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void badInput_setDateInputValue_blur_assertValidity() {
        setInputValue(dateInput, "INVALID");
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void badInput_setTimeInputValue_blur_assertValidity() {
        setInputValue(timeInput, "INVALID");
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        setInputValue(dateInput, "INVALID");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void badInput_setDateInputValue_blur_clearValue_assertValidity() {
        setInputValue(dateInput, "INVALID");
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void badInput_setTimeInputValue_blur_clearValue_assertValidity() {
        setInputValue(timeInput, "INVALID");
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void incompleteInput_assertValidity() {
        setInputValue(dateInput, "1/1/2000");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(INCOMPLETE_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void incompleteInput_changeToValidValue_assertValidity() {
        setInputValue(dateInput, "1/1/2000");
        resetValidationCount();

        setInputValue(dateInput, "1/1/2001");
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(INCOMPLETE_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);

        setInputValue(timeInput, "10:00");
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void validInput_changeToIncompleteInput_assertValidity() {
        setValue("1/1/2001", "10:00");
        resetValidationCount();

        setInputValue(timeInput, "");
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(INCOMPLETE_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void incompleteInput_setDateInputValue_blur_assertValidity() {
        setInputValue(dateInput, "1/1/2000");
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(INCOMPLETE_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void incompleteInput_setTimeInputValue_blur_assertValidity() {
        setInputValue(timeInput, "10:00");
        timeInput.sendKeys(Keys.TAB);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(INCOMPLETE_INPUT_ERROR_MESSAGE);
        assertValidationCount(1);
    }

    @Test
    public void incompleteInput_setValue_clearValue_assertValidity() {
        setInputValue(dateInput, "1/1/2000");
        timeInput.sendKeys(Keys.ENTER);
        resetValidationCount();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Override
    protected void assertValidationCount(int expected) {
        super.assertValidationCount(expected);
    }

    @Test
    public void incompleteInput_setDateInputValue_blur_clearValue_assertValidity() {
        setInputValue(dateInput, "1/1/2000");
        dateInput.sendKeys(Keys.TAB);
        timeInput.sendKeys(Keys.TAB);
        resetValidationCount();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void incompleteInput_setTimeInputValue_blur_clearValue_assertValidity() {
        setInputValue(timeInput, "10:00");
        timeInput.sendKeys(Keys.TAB);
        resetValidationCount();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
        assertValidationCount(1);
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        setInputValue(dateInput, "INVALID");

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_attachAndInvalidate_preservesInvalidState() {
        detachField();
        attachAndInvalidateField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void detach_hide_attach_showAndInvalidate_preservesInvalidState() {
        detachField();
        hideField();
        attachField();
        showAndInvalidateField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        setInputValue(dateInput, "INVALID");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    protected DateTimePickerElement getTestField() {
        return $(DateTimePickerElement.class).first();
    }

    private void setValue(String dateValue, String timeValue) {
        setInputValue(dateInput, dateValue);
        dateInput.sendKeys(Keys.TAB);
        setInputValue(timeInput, timeValue);
    }

    private void setInputValue(TestBenchElement input, String value) {
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        input.sendKeys(value, Keys.ENTER);
    }
}
