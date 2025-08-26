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

import org.junit.Before;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.validation.AbstractValidationIT;

public abstract class AbstractDateTimePickerValidationIT
        extends AbstractValidationIT<DateTimePickerElement> {
    private TestBenchElement dateInput;
    private TestBenchElement timeInput;

    @Before
    @Override
    public void init() {
        super.init();
        dateInput = testField.$("input").first();
        timeInput = testField.$("input").last();
    }

    @Override
    protected DateTimePickerElement getTestField() {
        return $(DateTimePickerElement.class).first();
    }

    void setValue(String dateValue, String timeValue) {
        setInputValue(dateInput, dateValue);
        setInputValue(timeInput, timeValue);
    }

    void setInputValue(TestBenchElement input, String value) {
        input.focus();
        input.sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        input.sendKeys(value, Keys.ENTER);
    }

    void assertValidation(boolean valid, String errorMessage) {
        if (valid) {
            assertValid();
        } else {
            assertInvalid();
        }
        assertErrorMessage(errorMessage);
        assertValidationCount(1);
    }

    void assertNoValidation(String errorMessage) {
        assertErrorMessage(errorMessage);
        assertValidationCount(0);
    }

    TestBenchElement getDateInput() {
        return dateInput;
    }

    TestBenchElement getTimeInput() {
        return timeInput;
    }
}
