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

import java.time.LocalDate;
import java.time.LocalTime;

@TestPath("vaadin-date-time-picker/date-time-picker-value-change")
public class DateTimePickerValueChangeIT extends AbstractComponentIT {

    DateTimePickerElement picker;
    TestBenchElement eventLog;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-time-picker"));
        eventLog = $("div").id("change-log");
        picker = $(DateTimePickerElement.class).first();
    }

    @Test
    public void setServerSideValueShouldTriggerSingleServerSideChangeEvent() {
        TestBenchElement setCurrentDateTimeButton = $("button")
                .id("set-current-date-time");

        setCurrentDateTimeButton.click();

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from server-side",
                entries[0].startsWith("source: server"));
    }

    @Test
    public void setServerSideValueWhenReadonlyShouldTriggerSingleServerSideChangeEvent() {
        TestBenchElement setCurrentDateTimeButton = $("button")
                .id("set-current-date-time");
        TestBenchElement setReadonlyButton = $("button").id("set-readonly");

        setReadonlyButton.click();
        setCurrentDateTimeButton.click();

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from server-side",
                entries[0].startsWith("source: server"));
    }

    @Test
    public void setServerSideValueWithSecondsPrecisionShouldTriggerSingleServerSideChangeEvent() {
        TestBenchElement setSecondsPrecisionButton = $("button")
                .id("set-seconds-precision");
        TestBenchElement setCurrentDateTimeButton = $("button")
                .id("set-current-date-time");

        setSecondsPrecisionButton.click();
        setCurrentDateTimeButton.click();

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from server-side",
                entries[0].startsWith("source: server"));
    }

    @Test
    public void setServerSideValueWithMillisPrecisionShouldTriggerSingleServerSideChangeEvent() {
        TestBenchElement setMillisPrecisionButton = $("button")
                .id("set-millis-precision");
        TestBenchElement setCurrentDateTimeButton = $("button")
                .id("set-current-date-time");

        setMillisPrecisionButton.click();
        setCurrentDateTimeButton.click();

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from server-side",
                entries[0].startsWith("source: server"));
    }

    @Test
    public void setClientSideValueShouldTriggerSingleClientSideChangeEvent() {
        picker.setDate(LocalDate.now());
        picker.setTime(LocalTime.now());

        String[] entries = getChangeLogEntries();
        Assert.assertEquals(1, entries.length);
        Assert.assertTrue("Event should originate from client-side",
                entries[0].startsWith("source: client"));
    }

    private String[] getChangeLogEntries() {
        return eventLog.getText().split("\n");
    }
}
