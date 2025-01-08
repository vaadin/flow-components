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
package com.vaadin.flow.component.timepicker.tests.validation;

import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.BAD_INPUT_ERROR_MESSAGE;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.MAX_ERROR_MESSAGE;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.MIN_ERROR_MESSAGE;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.REQUIRED_BUTTON;
import static com.vaadin.flow.component.timepicker.tests.validation.BasicValidationPage.REQUIRED_ERROR_MESSAGE;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-time-picker/validation/basic")
public class BasicValidationIT extends AbstractValidationIT<TimePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
        assertErrorMessage(null);
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
        assertErrorMessage(null);
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.selectByText("12:00");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");

        testField.selectByText("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);

        testField.selectByText("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.selectByText("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(REQUIRED_ERROR_MESSAGE);
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("10:00");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MIN_ERROR_MESSAGE);

        testField.selectByText("11:00");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.selectByText("12:00");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.selectByText("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("11:00", Keys.ENTER);

        testField.selectByText("12:00");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();
        assertErrorMessage(MAX_ERROR_MESSAGE);

        testField.selectByText("11:00");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.selectByText("10:00");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");

        testField.selectByText("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        testField.selectByText("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.selectByText("10:00");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");

        testField.selectByText("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        testField.selectByText("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
        assertErrorMessage("");
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        testField.selectByText("10:00");
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.selectByText("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
        assertErrorMessage(BAD_INPUT_ERROR_MESSAGE);

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
        assertErrorMessage("");
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.selectByText("10:00");
        testField.selectByText("");

        detachAndReattachField();

        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void webComponentCanNotModifyInvalidState() {
        assertWebComponentCanNotModifyInvalidState();

        detachAndReattachField();

        assertWebComponentCanNotModifyInvalidState();
    }

    @Test
    public void clientSideInvalidStateIsNotPropagatedToServer() {
        // Make the field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.selectByText("10:00");
        testField.selectByText("");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected TimePickerElement getTestField() {
        return $(TimePickerElement.class).first();
    }
}
