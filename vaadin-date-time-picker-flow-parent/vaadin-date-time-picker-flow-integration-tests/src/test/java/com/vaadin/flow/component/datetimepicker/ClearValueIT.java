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
package com.vaadin.flow.component.datetimepicker;

import static com.vaadin.flow.component.datetimepicker.ClearValuePage.CLEAR_AND_SET_VALUE_BUTTON;
import static com.vaadin.flow.component.datetimepicker.ClearValuePage.CLEAR_BUTTON;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-date-time-picker/clear-value")
public class ClearValueIT extends AbstractComponentIT {
    private DateTimePickerElement dateTimePicker;
    private DatePickerElement dateInput;
    private TimePickerElement timeInput;

    @Before
    public void init() {
        open();
        dateTimePicker = $(DateTimePickerElement.class).first();
        dateInput = dateTimePicker.getDatePicker();
        timeInput = dateTimePicker.getTimePicker();
    }

    @Test
    public void setDateInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("1/1/2022", Keys.ENTER);
        Assert.assertEquals("1/1/2022", dateInput.getInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getInputValue());
    }

    @Test
    public void setTimeInputValue_clearValue_inputValueIsEmpty() {
        timeInput.sendKeys("12:00 PM", Keys.ENTER);
        Assert.assertEquals("12:00 PM", timeInput.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timeInput.getTimePickerInputValue());
    }

    @Test
    public void setDateAndTimeInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("1/1/2022", Keys.ENTER);
        timeInput.sendKeys("12:00 PM", Keys.ENTER);
        Assert.assertEquals("1/1/2022", dateInput.getInputValue());
        Assert.assertEquals("12:00 PM", timeInput.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getInputValue());
        Assert.assertEquals("", timeInput.getTimePickerInputValue());
    }

    @Test
    public void setDateAndTimeInputValue_clearAndSetSameValue_inputValueIsPresent() {
        dateInput.sendKeys("1/1/2022", Keys.ENTER);
        timeInput.sendKeys("12:00 PM", Keys.ENTER);
        Assert.assertEquals("1/1/2022", dateInput.getInputValue());
        Assert.assertEquals("12:00 PM", timeInput.getTimePickerInputValue());

        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("1/1/2022", dateInput.getInputValue());
        Assert.assertEquals("12:00 PM", timeInput.getTimePickerInputValue());
    }

    @Test
    public void badInput_setDateInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", dateInput.getInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getInputValue());
    }

    @Test
    public void badInput_setTimeInputValue_clearValue_inputValueIsEmpty() {
        timeInput.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", timeInput.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timeInput.getTimePickerInputValue());
    }

    @Test
    public void badInput_setDateAndTimeInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("INVALID", Keys.ENTER);
        timeInput.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", dateInput.getInputValue());
        Assert.assertEquals("INVALID", timeInput.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getInputValue());
        Assert.assertEquals("", timeInput.getTimePickerInputValue());
    }
}
