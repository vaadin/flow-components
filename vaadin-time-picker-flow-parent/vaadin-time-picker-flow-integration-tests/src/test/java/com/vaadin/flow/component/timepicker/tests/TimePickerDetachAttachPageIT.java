/*
 * Copyright 2000-2022 Vaadin Ltd.
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
    public void clientSideValidationIsOverriddenOnAttach() {
        assertTimePickerIsValidOnTab();

        // Detaching and attaching time picker
        toggleAttach.click();
        toggleAttach.click();

        assertTimePickerIsValidOnTab();
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

    private void assertTimePickerIsValidOnTab() {
        TimePickerElement timePicker = $(TimePickerElement.class)
                .id("time-picker");
        timePicker.sendKeys(Keys.TAB);
        Assert.assertFalse("Time picker should be valid after Tab",
                Boolean.parseBoolean(timePicker.getAttribute("invalid")));
    }
}
