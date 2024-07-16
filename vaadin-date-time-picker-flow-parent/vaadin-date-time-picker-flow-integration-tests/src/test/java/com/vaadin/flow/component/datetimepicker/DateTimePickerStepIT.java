/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datetimepicker;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@TestPath("vaadin-date-time-picker/date-time-picker-step")
public class DateTimePickerStepIT extends AbstractComponentIT {

    DateTimePickerElement initialStepsPicker;
    DateTimePickerElement changeStepsPicker;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-time-picker"));
        initialStepsPicker = $(DateTimePickerElement.class)
                .id("initial-steps-date-time-picker");
        changeStepsPicker = $(DateTimePickerElement.class)
                .id("change-steps-date-time-picker");
    }

    @Test
    public void setInitialStepsToMillisecondPrecisionShouldFormatWithMillisecondPrecision() {
        Assert.assertEquals("3:20:30.123 PM",
                initialStepsPicker.getTimePresentation());
    }

    @Test
    public void setValueShouldFormatWithMinutePrecisionByDefault() {
        TestBenchElement setValue = $("button").id("set-date-time-value");

        setValue.click();

        Assert.assertEquals("3:20 PM", changeStepsPicker.getTimePresentation());
    }

    @Test
    public void setValueAfterSettingSecondPrecisionShouldFormatWithSecondPrecision() {
        TestBenchElement setSecondPrecision = $("button")
                .id("set-second-precision");
        TestBenchElement setValue = $("button").id("set-date-time-value");

        setSecondPrecision.click();
        setValue.click();

        Assert.assertEquals("3:20:30 PM",
                changeStepsPicker.getTimePresentation());
    }

    @Test
    public void setValueAfterSettingMillisecondPrecisionShouldFormatWithMillisecondPrecision() {
        TestBenchElement setMillisecondPrecision = $("button")
                .id("set-millisecond-precision");
        TestBenchElement setValue = $("button").id("set-date-time-value");

        setMillisecondPrecision.click();
        setValue.click();

        Assert.assertEquals("3:20:30.123 PM",
                changeStepsPicker.getTimePresentation());
    }
}
