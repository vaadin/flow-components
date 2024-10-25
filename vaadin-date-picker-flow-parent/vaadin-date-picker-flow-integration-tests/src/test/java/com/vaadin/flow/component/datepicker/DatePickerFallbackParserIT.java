/*
 * Copyright 2000-2024 Vaadin Ltd.
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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link DatePickerViewDemoPage}.
 */
@TestPath("vaadin-date-picker/fallback-parser")
public class DatePickerFallbackParserIT extends AbstractComponentIT {
    private DatePickerElement datePicker;
    private TestBenchElement valueChangeLog;

    @Before
    public void init() {
        open();
        datePicker = $(DatePickerElement.class).first();
        valueChangeLog = $("div").id("value-change-log");
    }

    @Test
    public void enterDateShortcut_assertValueChange() {
        datePicker.sendKeys("tomorrow", Keys.ENTER);
        assertValueChange(null, LocalDate.now().plusDays(1));
    }

    private void assertValueChange(LocalDate expectedOldValue,
            LocalDate expectedNewValue) {
        List<TestBenchElement> records = valueChangeLog.$("div").all();
        Assert.assertEquals(1, records.size());

        String[] parts = records.get(0).getText().split(",");
        LocalDate actualEventOldValue = parts[0].equals("null") ? null
                : LocalDate.parse(parts[0]);
        LocalDate actualEventNewValue = parts[1].equals("null") ? null
                : LocalDate.parse(parts[1]);
        LocalDate actualComponentValue = parts[2].equals("null") ? null
                : LocalDate.parse(parts[2]);

        Assert.assertEquals(expectedOldValue, actualEventOldValue);
        Assert.assertEquals(expectedNewValue, actualEventNewValue);
        Assert.assertEquals(expectedNewValue, actualComponentValue);

        $("button").id("clear-value-change-log").click();
    }

    private void assertNoValueChange() {
        Assert.assertEquals("", valueChangeLog.getText());
    }
}
