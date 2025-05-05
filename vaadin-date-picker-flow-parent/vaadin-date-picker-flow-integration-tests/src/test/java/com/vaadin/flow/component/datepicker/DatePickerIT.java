/*
 * Copyright 2000-2025 Vaadin Ltd.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

@TestPath("vaadin-date-picker/date-picker-test")
public class DatePickerIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-picker"));
    }

    @Test
    public void openSimpleDatePickerFromServer_overlayVisible() {
        scrollIntoViewAndClick(findElement(By.id("open-simple-picker")));
        waitForElementVisible(By.tagName("vaadin-date-picker-overlay"));
    }

    @Test
    public void selectDateOnSimpleDatePicker() {
        DatePickerElement picker = $(DatePickerElement.class)
                .id("simple-picker");
        WebElement message = findElement(By.id("simple-picker-message"));

        picker.setDate(LocalDate.of(1985, 1, 10));

        waitUntil(driver -> message.getText()
                .contains("Day: 10\nMonth: 1\nYear: 1985"));

        picker.clear();

        waitUntil(driver -> "No date is selected".equals(message.getText()));
    }

    @Test
    public void selectDateOnMinMaxDatePicker() {
        DatePickerElement picker = $(DatePickerElement.class)
                .id("min-and-max-picker");
        WebElement message = findElement(By.id("min-and-max-picker-message"));

        LocalDate now = LocalDate.now();
        picker.setDate(now);

        Assert.assertFalse("The selected date should be considered valid",
                picker.getPropertyBoolean("invalid"));

        waitUntil(driver -> message.getText()
                .contains(("Day: " + now.getDayOfMonth() + "\nMonth: "
                        + now.getMonthValue() + "\nYear: " + now.getYear())));

        picker.clear();

        waitUntil(driver -> "No date is selected".equals(message.getText()));

        Assert.assertFalse("The empty date should be considered valid",
                picker.getPropertyBoolean("invalid"));

        LocalDate invalid = now.minusYears(1);

        picker.setDate(invalid);

        Assert.assertTrue("The selected date should be considered invalid",
                picker.getPropertyBoolean("invalid"));
    }

    @Test
    public void selectDatesOnLinkedDatePickers() {
        DatePickerElement startPicker = $(DatePickerElement.class)
                .id("start-picker");
        DatePickerElement endPicker = $(DatePickerElement.class)
                .id("end-picker");
        WebElement message = findElement(By.id("start-and-end-message"));

        startPicker.setDate(LocalDate.of(1985, 1, 10));

        waitUntil(driver -> "Select the ending date".equals(message.getText()));

        Assert.assertEquals(
                "The min date at the end date picker should be 1985-01-11",
                "1985-01-11", endPicker.getProperty("min"));

        endPicker.setDate(LocalDate.of(1985, 1, 20));

        waitUntil(driver -> "Selected period:\nFrom 1985-01-10 to 1985-01-20"
                .equals(message.getText()));

        Assert.assertEquals(
                "The max date at the start date picker should be 1985-01-19",
                true, executeScript("return arguments[0].max === '1985-01-19'",
                        startPicker));

        startPicker.clear();
        waitUntil(
                driver -> "Select the starting date".equals(message.getText()));
    }

    @Test
    public void datePicker_OpenedChangeListener() {
        WebElement message = findElement(
                By.id("picker-with-opened-change-message"));

        DatePickerElement picker = $(DatePickerElement.class)
                .id("picker-with-opened-change");

        picker.click();
        waitUntil(drive -> "date picker was opened".equals(message.getText()));

        picker.sendKeys(Keys.ESCAPE);
        waitUntil(drive -> "date picker was closed".equals(message.getText()));
    }

}
