package com.vaadin.flow.component.datepicker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

import static com.vaadin.flow.component.datepicker.ClearValuePage.CLEAR_BUTTON;
import static com.vaadin.flow.component.datepicker.ClearValuePage.CLEAR_AND_SET_VALUE_BUTTON;

@TestPath("vaadin-date-picker/clear-value")
public class ClearValueIT extends AbstractComponentIT {
    private DatePickerElement datePicker;

    @Before
    public void init() {
        open();
        datePicker = $(DatePickerElement.class).first();
    }

    @Test
    public void setInputValue_clearValue_inputValueIsEmpty() {
        datePicker.sendKeys("1/1/2022", Keys.ENTER);
        Assert.assertEquals("1/1/2022", datePicker.getInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", datePicker.getInputValue());
    }

    @Test
    public void setBadInputValue_clearValue_inputValueIsEmpty() {
        datePicker.sendKeys("INVALID", Keys.ENTER);
        Assert.assertEquals("INVALID", datePicker.getInputValue());

        $("button").id(CLEAR_BUTTON).click();
        Assert.assertEquals("", datePicker.getInputValue());
    }

    @Test
    public void badInput_setInputValue_clearAndSetValue_inputValueIsPresent() {
        datePicker.sendKeys("INVALID", Keys.ENTER);
        $("button").id(CLEAR_AND_SET_VALUE_BUTTON).click();
        Assert.assertEquals("1/1/2022", datePicker.getInputValue());
    }
}
