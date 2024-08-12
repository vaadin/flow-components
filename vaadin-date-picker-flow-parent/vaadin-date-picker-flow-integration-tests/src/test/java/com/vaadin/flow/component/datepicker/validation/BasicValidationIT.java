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

import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.CLEAR_VALUE_BUTTON;
import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.MAX_INPUT;
import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.MIN_INPUT;
import static com.vaadin.flow.component.datepicker.validation.BasicValidationPage.REQUIRED_BUTTON;

import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.validation.AbstractValidationIT;

@TestPath("vaadin-date-picker/validation/basic")
public class BasicValidationIT extends AbstractValidationIT<DatePickerElement> {
    @Test
    public void fieldIsInitiallyValid() {
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void triggerBlur_assertValidity() {
        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_triggerBlur_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.sendKeys(Keys.TAB);
        assertValidationCount(0);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void required_changeValue_assertValidity() {
        $("button").id(REQUIRED_BUTTON).click();

        testField.setInputValue("1/1/2022");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();
    }

    @Test
    public void min_changeValue_assertValidity() {
        $("input").id(MIN_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        testField.setInputValue("2/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setInputValue("3/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("4/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void max_changeValue_assertValidity() {
        $("input").id(MAX_INPUT).sendKeys("2022-03-01", Keys.ENTER);

        testField.setInputValue("4/1/2022");
        assertValidationCount(1);
        assertClientInvalid();
        assertServerInvalid();

        testField.setInputValue("3/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("2/1/2022");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void badInput_changeValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setInputValue("1/1/2022");
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();

        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        testField.setInputValue("");
        assertValidationCount(1);
        assertClientValid();
        assertServerValid();
    }

    @Test
    public void setValue_clearValue_assertValidity() {
        testField.setInputValue("1/1/2022");
        assertServerValid();
        assertClientValid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void badInput_setValue_clearValue_assertValidity() {
        testField.setInputValue("INVALID");
        assertValidationCount(1);
        assertServerInvalid();
        assertClientInvalid();

        $("button").id(CLEAR_VALUE_BUTTON).click();
        assertValidationCount(1);
        assertServerValid();
        assertClientValid();
    }

    @Test
    public void detach_attach_preservesInvalidState() {
        // Make field invalid
        $("button").id(REQUIRED_BUTTON).click();
        testField.setInputValue("1/1/2022");
        testField.setInputValue("");

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
        testField.setInputValue("1/1/2022");
        testField.setInputValue("");

        executeScript("arguments[0].invalid = false", testField);

        assertServerInvalid();
    }

    @Override
    protected DatePickerElement getTestField() {
        return $(DatePickerElement.class).first();
    }
}
