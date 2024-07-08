/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.datetimepicker.ClearValuePage.CLEAR_BUTTON;

@TestPath("vaadin-date-time-picker/clear-value")
public class ClearValueIT extends AbstractComponentIT {
    private DateTimePickerElement dateTimePicker;
    private TestBenchElement dateInput;
    private TestBenchElement timeInput;

    @Before
    public void init() {
        open();
        dateTimePicker = $(DateTimePickerElement.class).first();
        dateInput = dateTimePicker.$("vaadin-date-time-picker-date-picker")
                .attribute("slot", "date-picker").first()
                .$("vaadin-date-time-picker-date-text-field").first().$("input")
                .first();
        timeInput = dateTimePicker.$("vaadin-date-time-picker-time-picker")
                .attribute("slot", "time-picker").first()
                .$("vaadin-combo-box-light").first()
                .$("vaadin-date-time-picker-time-text-field").first().$("input")
                .first();
    }

    @Test
    public void setDateInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("1/1/2022", Keys.ENTER);
        Assert.assertEquals("1/1/2022", dateInput.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getPropertyString("value"));
    }

    @Test
    public void setTimeInputValue_clearValue_inputValueIsEmpty() {
        timeInput.sendKeys("12:00 PM", Keys.ENTER);
        Assert.assertEquals("12:00 PM", timeInput.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timeInput.getPropertyString("value"));
    }

    @Test
    public void setDateAndTimeInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("1/1/2022", Keys.ENTER);
        timeInput.sendKeys("12:00 PM", Keys.ENTER);
        Assert.assertEquals("1/1/2022", dateInput.getPropertyString("value"));
        Assert.assertEquals("12:00 PM", timeInput.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getPropertyString("value"));
        Assert.assertEquals("", timeInput.getPropertyString("value"));
    }

    @Test
    public void badInput_setDateInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", dateInput.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getPropertyString("value"));
    }

    @Test
    public void badInput_setTimeInputValue_clearValue_inputValueIsEmpty() {
        timeInput.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", timeInput.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timeInput.getPropertyString("value"));
    }

    @Test
    public void badInput_setDateAndTimeInputValue_clearValue_inputValueIsEmpty() {
        dateInput.sendKeys("INVALID", Keys.ENTER);
        timeInput.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", dateInput.getPropertyString("value"));
        Assert.assertEquals("INVALID", timeInput.getPropertyString("value"));

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", dateInput.getPropertyString("value"));
        Assert.assertEquals("", timeInput.getPropertyString("value"));
    }
}
