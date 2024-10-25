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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;

import elemental.json.Json;
import elemental.json.JsonObject;

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
    public void enterShortcutValue_clearShortcutValue() {
        datePicker.setInputValue("newyear");
        assertValueChange("", "2024-01-01");
        assertInputValue("1/1/2024");

        datePicker.setInputValue("newyear");
        assertNoValueChange();
        assertInputValue("1/1/2024");

        datePicker.setInputValue("");
        assertValueChange("2024-01-01", "");
        assertInputValue("");
    }

    @Test
    public void enterUnparsableValue_enterShortcutValue() {
        datePicker.setInputValue("foo");
        assertNoValueChange();
        assertInputValue("foo");

        datePicker.setInputValue("newyear");
        assertValueChange("", "2024-01-01");
        assertInputValue("1/1/2024");
    }

    @Test
    public void enterParsableValue_enterShortcutValue() {
        datePicker.setInputValue("2/2/2000");
        assertValueChange("", "2000-02-02");
        assertInputValue("2/2/2000");

        datePicker.setInputValue("newyear");
        assertValueChange("2000-02-02", "2024-01-01");
        assertInputValue("1/1/2024");
    }

    private void assertValueChange(String expectedOldValue,
            String expectedNewValue) {
        List<TestBenchElement> records = valueChangeLog.$("div").all();
        Assert.assertEquals("ValueChangeEvent should be fired only once", 1,
                records.size());

        JsonObject record = Json.parse(records.get(0).getText());

        Assert.assertTrue("eventFromClient should be true",
                record.getBoolean("eventFromClient"));
        Assert.assertEquals("eventOldValue should contain old value",
                expectedOldValue, record.getString("eventOldValue"));
        Assert.assertEquals("eventNewValue should contain new value",
                expectedNewValue, record.getString("eventNewValue"));
        Assert.assertEquals("componentValue should contain new value",
                expectedNewValue, record.getString("componentValue"));
        Assert.assertEquals("componentValueProperty should contain new value",
                expectedNewValue, record.getString("componentValueProperty"));

        $("button").id("clear-value-change-log").click();
    }

    private void assertNoValueChange() {
        Assert.assertEquals("", valueChangeLog.getText());
    }

    private void assertInputValue(String expected) {
        Assert.assertEquals(expected, datePicker.getInputValue());
    }
}
