/*
 * Copyright 2000-2021 Vaadin Ltd.
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

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import java.time.LocalDate;
import java.time.LocalTime;

@TestPath("vaadin-date-time-picker/date-time-picker-step")
public class DateTimePickerStepIT extends AbstractComponentIT {

    DateTimePickerElement picker;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-time-picker"));
        picker = $(DateTimePickerElement.class).first();
    }

    @Test
    public void setValueShouldFormatWithMinutePrecisionByDefault() {
        TestBenchElement setValue = $("button")
                .id("set-date-time-value");

        setValue.click();

        Assert.assertEquals("3:20 PM", picker.getTimePresentation());
    }

    @Test
    public void setValueAfterSettingSecondPrecisionShouldFormatWithSecondPrecision() {
        TestBenchElement setSecondPrecision = $("button")
                .id("set-second-precision");
        TestBenchElement setValue = $("button")
                .id("set-date-time-value");

        setSecondPrecision.click();
        setValue.click();

        Assert.assertEquals("3:20:30 PM", picker.getTimePresentation());
    }

    @Test
    public void setValueAfterSettingMillisecondPrecisionShouldFormatWithMillisecondPrecision() {
        TestBenchElement setMillisecondPrecision = $("button")
                .id("set-millisecond-precision");
        TestBenchElement setValue = $("button")
                .id("set-date-time-value");

        setMillisecondPrecision.click();
        setValue.click();

        Assert.assertEquals("3:20:30.123 PM", picker.getTimePresentation());
    }
}
