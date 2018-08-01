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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.timepicker.demo.TimePickerView;
import com.vaadin.flow.demo.ComponentDemoTest;
import com.vaadin.testbench.TestBenchElement;

/**
 * Integration tests for the {@link TimePickerView}.
 */
public class TimePickerIT extends ComponentDemoTest {

    private static final String TIMEPICKER_OVERLAY = "vaadin-combo-box-overlay";

    @Before
    public void init() {
        waitForElementPresent(By.tagName("vaadin-time-picker"));
    }

    @Test
    public void selectTimeOnSimpleTimePicker() {
        WebElement picker = layout.findElement(By.id("simple-picker"));
        WebElement message = layout.findElement(By.id("simple-picker-message"));

        executeScript("arguments[0].value = '10:08'", picker);

        waitUntil(driver -> message.getText()
                .contains("Hour: 10\nMinute: 8"));

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
    public void TimePickerWithDifferentStep() {
        TestBenchElement picker = $(TestBenchElement.class)
                .id("step-setting-picker");
        openPickerDropDown(picker);
        waitForElementPresent(By.tagName("vaadin-combo-box-overlay"));
        Assert.assertEquals("Item in the dropdown is not correct", "01:00",
                findItemText(1));
        closePickerDropDown(picker);
        executeScript("arguments[0].value = '12:31'", picker);

        clickButtonAndAssertText(0.5, "12:31:00.000", picker);
        openPickerDropDown(picker);
        waitForElementNotPresent(By.tagName("vaadin-combo-box-overlay"));

        clickButtonAndAssertText(6.0, "12:31:00", picker);
        openPickerDropDown(picker);
        waitForElementNotPresent(By.tagName("vaadin-combo-box-overlay"));

        clickButtonAndAssertText(900.0, "12:31", picker);
        openPickerDropDown(picker);
        waitForElementPresent(By.tagName("vaadin-combo-box-overlay"));
        Assert.assertEquals("Item in the dropdown is not correct", "00:15",
                findItemText(1));
        closePickerDropDown(picker);
    }

    private String findItemText(int index) {
        return $("vaadin-combo-box-overlay").first()
                .$(TestBenchElement.class).id("content")
                .$(TestBenchElement.class).id("selector")
                .$("vaadin-combo-box-item").get(index)
                .$(TestBenchElement.class).id("content").getText();
    }

    private void openPickerDropDown(TestBenchElement picker) {
        TestBenchElement comboLight = picker.$("vaadin-combo-box-light").get(0);
        executeScript("arguments[0].open()", comboLight);
    }

    private void closePickerDropDown(TestBenchElement picker) {
        TestBenchElement comboLight = picker.$("vaadin-combo-box-light").get(0);
        executeScript("arguments[0].close()", comboLight);
        waitForElementNotPresent(By.tagName("vaadin-combo-box-overlay"));
    }

    private void clickButtonAndAssertText(double step, String expectedString,
            TestBenchElement picker) {
        findElement(By.id("step-" + step)).click();
        WebElement message = findElement(By.id("step-setting-picker-message"));
        Assert.assertEquals("The current step is incorrect",
                "Current Step:" + step, message.getText());
        Assert.assertEquals(
                "When step is " + step + ", the value should be "
                        + expectedString,
                expectedString, picker.getAttribute("value"));
    }

    @Override
    protected String getTestPath() {
        return ("/vaadin-time-picker");
    }
}
