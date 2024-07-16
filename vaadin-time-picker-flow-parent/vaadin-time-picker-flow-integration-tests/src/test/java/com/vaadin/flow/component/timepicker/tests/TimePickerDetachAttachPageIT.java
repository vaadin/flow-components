/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.timepicker.tests;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;

/**
 * Integration tests for attaching / detaching time picker.
 */
@TestPath("vaadin-time-picker/time-picker-detach-attach")
public class TimePickerDetachAttachPageIT extends AbstractComponentIT {

    TestBenchElement toggleAttach;
    TestBenchElement setValue;
    TestBenchElement setLocale;

    @Before
    public void init() {
        open();
        toggleAttach = $("button").id("toggle-attached");
        setValue = $("button").id("set-value");
        setLocale = $("button").id("set-california-locale");
    }

    @Test
    public void formatShouldRespectLocaleAfterDetachAndReattach() {
        setValue.click();
        setLocale.click();
        TimePickerElement timePicker = $(TimePickerElement.class)
                .id("time-picker");
        Assert.assertEquals("2:00 a.m.", timePicker.getSelectedText());

        toggleAttach.click();
        toggleAttach.click();
        timePicker = $(TimePickerElement.class).id("time-picker");
        Assert.assertEquals("2:00 a.m.", timePicker.getSelectedText());
    }
}
