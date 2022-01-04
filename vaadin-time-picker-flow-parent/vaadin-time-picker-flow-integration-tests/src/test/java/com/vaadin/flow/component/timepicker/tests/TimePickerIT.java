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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

/**
 * Integration tests for the {@link TimePickerPage}.
 */
@TestPath("vaadin-time-picker/time-picker-it")
public class TimePickerIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        $(TimePickerElement.class).waitForFirst();
    }

    @After
    public void after() {
        checkLogsForErrors();
    }

    @Test
    public void selectTimeOnSimpleTimePicker() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("simple-picker");
        TestBenchElement message = $("div").id("simple-picker-message");

        picker.setValue("10:08");
        waitUntil(driver -> message.getText().contains("Hour: 10\nMinute: 8"));

        picker.setValue("");
        waitUntil(driver -> "No time is selected".equals(message.getText()));
    }

    @Test
    public void selectTimeOnAutoOpenDisabledTimePicker() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("autoopendisabled-picker");
        TestBenchElement message = $("div")
                .id("autoopendisabled-picker-message");

        picker.setValue("10:08");
        waitUntil(driver -> message.getText().contains("Hour: 10\nMinute: 8"));

        picker.setValue("");
        waitUntil(driver -> "No time is selected".equals(message.getText()));
        Assert.assertFalse(picker.isAutoOpen());
    }

    @Test
    public void selectTimeOnDisabledTimePicker() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("disabled-picker");
        TestBenchElement message = $("div").id("disabled-picker-message");

        picker.setValue("10:15");
        Assert.assertEquals(
                "The message should not be shown for the disabled picker", "",
                message.getText());
    }

    @Test
    public void timePickerWithDifferentStep() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("step-setting-picker");
        picker.openDropDown();
        Assert.assertEquals("Item in the dropdown is not correct", "1:00 AM",
                picker.getItemText(1));
        picker.closeDropDown();
        executeScript("arguments[0].value = '12:31'", picker);

        selectStep("0.5s");
        validatePickerValue(picker, "12:31:00.000");

        selectStep("10s");
        validatePickerValue(picker, "12:31:00");

        // for the auto formatting of the value to work, it needs to match the
        // new step
        executeScript("arguments[0].value = '12:30:00'", picker);
        selectStep("30m"); // using smaller step will cause the drop down to be
                           // big and then drop down iron list does magic that
                           // messes the item indexes
        validatePickerValue(picker, "12:30");
    }

    @Test
    public void timePickerWithMinAndMaxSetting() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("time-picker-min-max");
        picker.openDropDown();
        Assert.assertEquals(
                "The first item in the dropdown should be the min value",
                "5:00 AM", picker.getItemText(0));
        Assert.assertEquals(
                "The last item in the dropdown should be the max value",
                "4:00 PM", picker.getLastItemText());
    }

    @Test
    public void timePickerHelperText() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("time-picker-helper-text");
        Assert.assertEquals("Helper text", picker.getHelperText());

        $("button").id("button-clear-helper-text").click();
        Assert.assertEquals("", picker.getHelperText());
    }

    @Test
    public void timePickerHelperComponent() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("time-picker-helper-component");
        Assert.assertEquals("helper-component",
                picker.getHelperComponent().getAttribute("id"));

        $("button").id("button-clear-helper-component").click();
        Assert.assertNull(picker.getHelperComponent());
    }

    private void selectStep(String step) {
        NativeSelectElement select = $(NativeSelectElement.class)
                .id("step-picker");
        select.setValue(step);
    }

    private void validatePickerValue(TimePickerElement picker, String value) {
        Assert.assertEquals("Invalid time picker value", value,
                picker.getValue());
    }
}
