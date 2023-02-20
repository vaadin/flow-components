package com.vaadin.flow.component.timepicker.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.timepicker.tests.ClearValuePage.CLEAR_BUTTON;

@TestPath("vaadin-time-picker/clear-value")
public class ClearValueIT extends AbstractComponentIT {
    private TimePickerElement timePicker;

    @Before
    public void init() {
        open();
        timePicker = $(TimePickerElement.class).first();
    }

    @Test
    public void setInputValue_clearValue_inputValueIsEmpty() {
        timePicker.selectByText("12:00 PM");
        Assert.assertEquals("12:00 PM", timePicker.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timePicker.getTimePickerInputValue());
    }

   // @Test
    public void setBadInputValue_clearValue_inputValueIsEmpty() {
        timePicker.selectByText("INVALID");
        Assert.assertEquals("INVALID", timePicker.getTimePickerInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", timePicker.getTimePickerInputValue());
    }
}
