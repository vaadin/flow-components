package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("helper-text-component")
public class DateTimePickerHelpersPageIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-time-picker"));
    }

    @Test
    public void assertHelperText(){
        DateTimePickerElement dtp = $(DateTimePickerElement.class).id("dtp-helper-text");
        Assert.assertEquals("Helper text", dtp.getHelperText());

        $("button").id("button-clear-helper-text").click();
        Assert.assertEquals("", dtp.getHelperText());
    }

    @Test
    public void assertHelperComponent(){
        DateTimePickerElement dtp = $(DateTimePickerElement.class).id("dtp-helper-component");
        Assert.assertEquals("helper-component", dtp.getHelperComponent().getAttribute("id"));

        $("button").id("button-clear-helper-component").click();
        Assert.assertNull(dtp.getHelperComponent());
    }
}
