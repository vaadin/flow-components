/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.flow.component.combobox.testbench.ComboBoxElement;
import com.vaadin.flow.component.timepicker.testbench.TimePickerElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.timepicker.demo.TimePickerView;
import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Integration tests for the {@link TimePickerView}.
 */
public class TimePickerIT extends ComponentDemoTest {

    @Before
    public void init() {
        $(TimePickerElement.class).waitForFirst();
    }

    @Test
    public void selectTimeOnSimpleTimePicker() {
        WebElement picker = layout.findElement(By.id("simple-picker"));
        WebElement message = layout.findElement(By.id("simple-picker-message"));

        executeScript("arguments[0].value = '10:08'", picker);

        waitUntil(driver -> message.getText().contains("Hour: 10\nMinute: 8"));

        executeScript("arguments[0].value = ''", picker);

        waitUntil(driver -> "No time is selected".equals(message.getText()));
    }

    @Test
    public void selectTimeOnDisabledTimePicker() {
        WebElement picker = layout.findElement(By.id("disabled-picker"));
        WebElement message = layout
                .findElement(By.id("disabled-picker-message"));

        executeScript("arguments[0].value = '10:15'", picker);
        Assert.assertEquals(
                "The message should not be shown for the disabled picker", "",
                message.getText());
    }

    @Test
    public void timePickerWithDifferentStep() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("step-setting-picker");
        picker.scrollIntoView();
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

        picker.openDropDown();

        Assert.assertEquals("Item in the dropdown is not correct", "12:30 AM",
                picker.getItemText(1));
        picker.closeDropDown();
    }

    @Test
    public void timePickerWithMinAndMaxSetting() {
        TimePickerElement picker = $(TimePickerElement.class)
                .id("time-picker-min-max");
        picker.scrollIntoView();
        picker.openDropDown();
        Assert.assertEquals("The first item in the dropdown should be the min value",
                "5:00 AM", picker.getItemText(0));
        Assert.assertEquals("The last item in the dropdown should be the max value",
                "6:00 PM", picker.getLastItemText());
    }

    private void selectStep(String step) {
        ComboBoxElement comboBox = $(ComboBoxElement.class).id("step-picker");
        selectFromComboBox(comboBox, step);
        waitForElementNotPresent(By.tagName("vaadin-combo-box-overlay"));
    }

    private void validatePickerValue(TimePickerElement picker, String value) {
        Assert.assertEquals("Invalid time picker value", value,
                picker.getValue());
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-time-picker");
    }

    public static void selectFromComboBox(ComboBoxElement comboBox,
            String text) {
        try {
            comboBox.selectByText(text);
        } catch (IllegalArgumentException iae) {
            // ignore due to issues in the TB element for CB
            // the element selects the correct item, but then throws IAE
            // because it tries to validate the selection. Probably regression
            // with CB 2.0 ?
        }

        Assert.assertEquals("The current value is not incorrect",
                text.toLowerCase(), comboBox.$("vaadin-text-field").first()
                        .getPropertyString("value").toLowerCase());

        comboBox.getCommandExecutor().executeScript("arguments[0].close()",
                comboBox);
    }
}
