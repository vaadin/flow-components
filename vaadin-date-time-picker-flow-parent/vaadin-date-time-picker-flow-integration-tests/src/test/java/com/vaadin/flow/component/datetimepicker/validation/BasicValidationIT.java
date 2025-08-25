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

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-time-picker/validation/basic")
public class BasicValidationIT extends AbstractDateTimePickerValidationIT {

    @Test
    public void fieldIsInitiallyValid() {
        assertValid();
        assertErrorMessage(null);
    }

    @Test
    public void triggerBlur_assertValidity() {
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValid();
        assertNoValidation(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        clickElementWithJs(REQUIRED_BUTTON);

        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValid();
        assertNoValidation(null);
    }

    @Test
    public void required_changeInputTemporarily_triggerBlur_assertValidity() {
        clickElementWithJs(REQUIRED_BUTTON);
        getDateInput().sendKeys("1", Keys.BACK_SPACE, Keys.ENTER);
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValid();
        assertNoValidation(null);
    }

    @Test
    public void required_changeAndClearValueWithoutBlur_triggerBlur_assertValidity() {
        clickElementWithJs(REQUIRED_BUTTON);
        getDateInput().sendKeys("1/1/2000", Keys.ENTER);
        getDateInput().sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME),
                Keys.BACK_SPACE);
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void required_changeValue_assertValidity() {
        clickElementWithJs(REQUIRED_BUTTON);

        setValue("1/1/2000", "12:00");
        assertValidation(true, "");

        setInputValue(getTimeInput(), "");
        assertValidation(false, INCOMPLETE_INPUT_ERROR_MESSAGE);

        setInputValue(getDateInput(), "");
        assertValidation(false, REQUIRED_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "");
        assertInvalid();
        assertNoValidation(REQUIRED_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "");
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setInputValue(getDateInput(), "1/1/2000");
        assertValidation(false, MIN_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "11:00");
        assertValidation(false, MIN_ERROR_MESSAGE);

        setInputValue(getDateInput(), "2/2/2000");
        assertValidation(false, MIN_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "12:00");
        assertValidation(true, "");

        setInputValue(getTimeInput(), "13:00");
        assertValidation(true, "");

        setInputValue(getDateInput(), "3/3/2000");
        assertValidation(true, "");

        setInputValue(getTimeInput(), "11:00");
        assertValidation(true, "");
    }

    @Test
    public void max_changeDateInputValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2000-02-02T12:00", Keys.ENTER);

        setInputValue(getDateInput(), "3/3/2000");
        assertValidation(false, MAX_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "12:00");
        assertValidation(false, MAX_ERROR_MESSAGE);

        setInputValue(getDateInput(), "2/2/2000");
        assertValidation(true, "");

        setInputValue(getTimeInput(), "13:00");
        assertValidation(false, MAX_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "12:00");
        assertValidation(true, "");

        setInputValue(getTimeInput(), "11:00");
        assertValidation(true, "");

        setInputValue(getDateInput(), "1/1/2000");
        assertValidation(true, "");

        setInputValue(getTimeInput(), "13:00");
        assertValidation(true, "");
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        setValue("1/1/2000", "10:00");
        assertValidation(true, "");

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(true, "");
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        setValue("1/1/2000", "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        setInputValue(getTimeInput(), "10:00");
        assertValidation(true, "");

        setInputValue(getDateInput(), "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void badInput_setDateInputValue_blur_assertValidity() {
        setInputValue(getDateInput(), "INVALID");
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void badInput_setTimeInputValue_blur_assertValidity() {
        setInputValue(getTimeInput(), "INVALID");
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        setInputValue(getDateInput(), "INVALID");
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(true, "");
    }

    @Test
    public void badInput_setDateInputValue_blur_clearValue_assertValidity() {
        setInputValue(getDateInput(), "INVALID");
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(true, "");
    }

    @Test
    public void badInput_setTimeInputValue_blur_clearValue_assertValidity() {
        setInputValue(getTimeInput(), "INVALID");
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, BAD_INPUT_ERROR_MESSAGE);

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(true, "");
    }

    @Test
    public void incompleteInput_assertValidity() {
        setInputValue(getDateInput(), "1/1/2000");
        getDateInput().sendKeys(Keys.chord(Keys.SHIFT, Keys.TAB));
        assertValidation(false, INCOMPLETE_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void incompleteInput_changeToValidValue_assertValidity() {
        setInputValue(getDateInput(), "1/1/2000");
        resetValidationCount();

        setInputValue(getTimeInput(), "10:00");
        assertValidation(true, "");
    }

    @Test
    public void validInput_changeToIncompleteInput_assertValidity() {
        setValue("1/1/2001", "10:00");
        resetValidationCount();

        setInputValue(getTimeInput(), "");
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, INCOMPLETE_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void incompleteInput_setDateInputValue_blur_assertValidity() {
        setInputValue(getDateInput(), "1/1/2000");
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, INCOMPLETE_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void incompleteInput_setTimeInputValue_blur_assertValidity() {
        setInputValue(getTimeInput(), "10:00");
        getTimeInput().sendKeys(Keys.TAB);
        assertValidation(false, INCOMPLETE_INPUT_ERROR_MESSAGE);
    }

    @Test
    public void incompleteInput_setValue_clearValue_assertValidity() {
        setInputValue(getDateInput(), "1/1/2000");
        getTimeInput().sendKeys(Keys.ENTER);
        resetValidationCount();

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(true, "");
    }

    @Test
    public void incompleteInput_setDateInputValue_blur_clearValue_assertValidity() {
        setInputValue(getDateInput(), "1/1/2000");
        getDateInput().sendKeys(Keys.TAB);
        getTimeInput().sendKeys(Keys.TAB);
        resetValidationCount();

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(true, "");
    }

    @Test
    public void incompleteInput_setTimeInputValue_blur_clearValue_assertValidity() {
        setInputValue(getTimeInput(), "10:00");
        getTimeInput().sendKeys(Keys.TAB);
        resetValidationCount();

        clickElementWithJs(CLEAR_VALUE_BUTTON);
        assertValidation(true, "");
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        setInputValue(getDateInput(), "INVALID");

        detachAndReattachField();

        assertInvalid();
    }

    @Test
    public void detach_attachAndInvalidate_preservesInvalidState() {
        detachField();
        attachAndInvalidateField();

        assertInvalid();
    }

    @Test
    public void detach_hide_attach_showAndInvalidate_preservesInvalidState() {
        detachField();
        hideField();
        attachField();
        showAndInvalidateField();

        assertInvalid();
    }

    @Test
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        setInputValue(getDateInput(), "INVALID");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }
}
