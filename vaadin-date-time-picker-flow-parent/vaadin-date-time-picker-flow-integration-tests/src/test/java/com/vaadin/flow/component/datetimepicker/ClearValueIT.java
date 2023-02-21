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
        dateInput = dateTimePicker.$("input").first();
        timeInput = dateTimePicker.$("input").last();
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
