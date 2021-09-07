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
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@TestPath("vaadin-time-picker/time-picker-value-change")
public class TimePickerValueChangeIT extends AbstractComponentIT {

    TimePickerElement picker;
    TestBenchElement eventLog;

    @Before
    public void init() {
        open();
        $(TimePickerElement.class).waitForFirst();
        eventLog = $("div").id("change-log");
        picker = $(TimePickerElement.class).id("time-picker");
    }

    @Test
    public void setServerSideValueShouldTriggerSingleServerSideChangeEvent() {
        TestBenchElement setCurrentTimeButton = $("button")
                .id("set-current-time");

        setCurrentTimeButton.click();

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from server-side",
                entries[0].startsWith("source: server"));
    }

    @Test
    public void setServerSideValueWithSecondsPrecisionShouldTriggerSingleServerSideChangeEvent() {
        TestBenchElement setSecondsPrecisionButton = $("button")
                .id("set-seconds-precision");
        TestBenchElement setCurrentTimeButton = $("button")
                .id("set-current-time");

        setSecondsPrecisionButton.click();
        setCurrentTimeButton.click();

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from server-side",
                entries[0].startsWith("source: server"));
    }

    @Test
    public void setServerSideValueWithMillisPrecisionShouldTriggerSingleServerSideChangeEvent() {
        TestBenchElement setMillisPrecisionButton = $("button")
                .id("set-millis-precision");
        TestBenchElement setCurrentTimeButton = $("button")
                .id("set-current-time");

        setMillisPrecisionButton.click();
        setCurrentTimeButton.click();

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from server-side",
                entries[0].startsWith("source: server"));
    }

    @Test
    public void setClientSideValueShouldTriggerSingleClientSideChangeEvent() {
        picker.setValue("10:08");

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from client-side",
                entries[0].startsWith("source: client"));
    }

    private String[] getChangeLogEntries() {
        return eventLog.getText().split("\n");
    }
}
