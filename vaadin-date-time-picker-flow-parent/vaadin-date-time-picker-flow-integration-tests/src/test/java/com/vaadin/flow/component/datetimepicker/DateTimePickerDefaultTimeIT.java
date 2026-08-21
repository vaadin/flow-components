/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.datetimepicker.testbench.DateTimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-date-time-picker/default-time")
public class DateTimePickerDefaultTimeIT extends AbstractComponentIT {

    private DateTimePickerElement picker;
    private TestBenchElement valueLog;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-time-picker"));
        picker = $(DateTimePickerElement.class).id("default-time-picker");
        valueLog = $("div").id("value-log");
    }

    @Test
    public void selectDate_defaultTimeFilledIn_valueSentToServer() {
        picker.setDate(LocalDate.of(2026, 1, 1));

        Assert.assertEquals(LocalTime.of(9, 0), picker.getTime());
        Assert.assertEquals("2026-01-01T09:00", valueLog.getText());
    }
}
