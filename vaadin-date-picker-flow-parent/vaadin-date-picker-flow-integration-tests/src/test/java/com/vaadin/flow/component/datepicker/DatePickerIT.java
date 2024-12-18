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
import java.time.Month;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;

/**
 * Integration tests for the {@link DatePickerViewDemoPage}.
 */
@TestPath("vaadin-date-picker-test-demo")
public class DatePickerIT extends AbstractComponentIT {

    private static final String DATEPICKER_OVERLAY = "vaadin-date-picker-overlay";

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
    public void selectDateOnFinnishDatePicker() {
        DatePickerElement picker = $(DatePickerElement.class)
                .id("finnish-picker");
        WebElement message = findElement(By.id("finnish-picker-message"));

        picker.setDate(LocalDate.of(1985, 1, 10));

        waitUntil(driver -> "Day of week: torstai\nMonth: tammiku"
                .equals(message.getText()));

        picker.clear();

        waitUntil(driver -> "No date is selected".equals(message.getText()));

        picker.open();

        WebElement overlay = $(DATEPICKER_OVERLAY).waitForFirst();
        WebElement todayButton = overlay
                .findElement(By.cssSelector("[slot=today-button]"));

        waitUntil(driver -> "tänään".equals(todayButton.getText()));
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
    public void selectDatesOnCustomLocaleDatePickers() {
        DatePickerElement localePicker = $(DatePickerElement.class)
                .id("locale-change-picker");
        WebElement message = findElement(
                By.id("Customize-locale-picker-message"));
        localePicker.setDate(LocalDate.of(2018, 3, 27));

        waitUntil(driver -> message.getText()
                .contains("Day: 27\nMonth: 3\nYear: 2018\nLocale:"));

        Assert.assertEquals(
                "The format of the displayed date should be MM/DD/YYYY",
                "3/27/2018", localePicker.getInputValue());

        findElement(By.id("Locale-UK")).click();
        localePicker.setDate(LocalDate.of(2018, 3, 26));
        waitUntil(driver -> "Day: 26\nMonth: 3\nYear: 2018\nLocale: en_GB"
                .equals(message.getText()));

        Assert.assertEquals(
                "The format of the displayed date should be DD/MM/YYYY",
                "26/03/2018", localePicker.getInputValue());

        findElement(By.id("Locale-US")).click();
        localePicker.setDate(LocalDate.of(2018, 3, 25));
        waitUntil(driver -> "Day: 25\nMonth: 3\nYear: 2018\nLocale: en_US"
                .equals(message.getText()));
        Assert.assertEquals(
                "The format of the displayed date should be MM/DD/YYYY",
                "3/25/2018", localePicker.getInputValue());

        findElement(By.id("Locale-UK")).click();
        Assert.assertEquals(
                "The format of the displayed date should be DD/MM/YYYY",
                "25/03/2018", localePicker.getInputValue());
    }

    private void setDateAndAssert(DatePickerElement datePicker, LocalDate date,
            String expectedInputValue) {
        datePicker.setDate(date);
        Assert.assertEquals(expectedInputValue, datePicker.getInputValue());
    }

    @Test
    public void selectDatesBeforeYear1000() {
        DatePickerElement localePicker = $(DatePickerElement.class)
                .id("locale-change-picker");

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 7),
                "3/7/0900");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 7),
                "3/7/0087");

        $("button").id("Locale-UK").click();
        Assert.assertEquals("07/03/0087", localePicker.getInputValue());

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 6),
                "06/03/0900");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 6),
                "06/03/0087");

        $("button").id("Locale-US").click();
        Assert.assertEquals("3/6/0087", localePicker.getInputValue());

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 5),
                "3/5/0900");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 5),
                "3/5/0087");

        $("button").id("Locale-CHINA").click();
        Assert.assertEquals("0087/3/5", localePicker.getInputValue());

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 4),
                "0900/3/4");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 4),
                "0087/3/4");

        $("button").id("Locale-UK").click();
        Assert.assertEquals("04/03/0087", localePicker.getInputValue());
    }

    /**
     * Expects input value to change to expectedInputValue after setting it.
     */
    private void setInputValueAndAssert(DatePickerElement datePicker,
            String inputValue, String expectedInputValue,
            LocalDate expectedDate) {
        datePicker.setInputValue(inputValue);
        Assert.assertEquals(expectedInputValue, datePicker.getInputValue());
        Assert.assertEquals(expectedDate, datePicker.getDate());
    }

    /**
     * Expects input value to stay the same as it is set to.
     */
    private void setInputValueAndAssert(DatePickerElement datePicker,
            String inputValue, LocalDate expectedDate) {
        setInputValueAndAssert(datePicker, inputValue, inputValue,
                expectedDate);
    }

    @Test
    public void selectDatesBeforeYear1000SimulateUserInput() {
        DatePickerElement localePicker = $(DatePickerElement.class)
                .id("locale-change-picker");

        setInputValueAndAssert(localePicker, "3/7/0900",
                LocalDate.of(900, Month.MARCH, 7));

        setInputValueAndAssert(localePicker, "3/6/900", "3/6/0900",
                LocalDate.of(900, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "3/5/0087",
                LocalDate.of(87, Month.MARCH, 5));
        setInputValueAndAssert(localePicker, "3/6/87", "3/6/1987",
                LocalDate.of(1987, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "3/7/20", "3/7/2020",
                LocalDate.of(2020, Month.MARCH, 7));
        setInputValueAndAssert(localePicker, "3/8/0020",
                LocalDate.of(20, Month.MARCH, 8));

        $("button").id("Locale-UK").click();
        Assert.assertEquals("08/03/0020", localePicker.getInputValue());

        setInputValueAndAssert(localePicker, "7/3/0900", "07/03/0900",
                LocalDate.of(900, Month.MARCH, 7));

        setInputValueAndAssert(localePicker, "6/3/900", "06/03/0900",
                LocalDate.of(900, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "5/3/0087", "05/03/0087",
                LocalDate.of(87, Month.MARCH, 5));
        setInputValueAndAssert(localePicker, "6/3/87", "06/03/1987",
                LocalDate.of(1987, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "7/3/20", "07/03/2020",
                LocalDate.of(2020, Month.MARCH, 7));
        setInputValueAndAssert(localePicker, "8/3/0020", "08/03/0020",
                LocalDate.of(20, Month.MARCH, 8));

        $("button").id("Locale-CHINA").click();
        Assert.assertEquals("0020/3/8", localePicker.getInputValue());

        setInputValueAndAssert(localePicker, "0900/3/7",
                LocalDate.of(900, Month.MARCH, 7));
        setInputValueAndAssert(localePicker, "900/3/6", "0900/3/6",
                LocalDate.of(900, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "0087/3/5",
                LocalDate.of(87, Month.MARCH, 5));
        setInputValueAndAssert(localePicker, "87/3/6", "1987/3/6",
                LocalDate.of(1987, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "20/3/7", "2020/3/7",
                LocalDate.of(2020, Month.MARCH, 7));
        setInputValueAndAssert(localePicker, "0020/3/8",
                LocalDate.of(20, Month.MARCH, 8));

        $("button").id("Locale-US").click();
        Assert.assertEquals("3/8/0020", localePicker.getInputValue());
    }

    @Test
    public void datePickerInsideDisabledParent_pickerIsDisabled() {
        WebElement picker = findElement(By.id("picker-inside-disabled-parent"));
        Assert.assertFalse(
                "The date picker should be disabled, when the parent component is disabled.",
                picker.isEnabled());
    }

    @Test
    public void datePickerInsideDisabledParent_enableParent_pickerIsEnabled() {
        WebElement picker = findElement(By.id("picker-inside-disabled-parent"));
        findElement(By.id("enable-parent")).click();
        Assert.assertTrue(
                "The date picker should be enabled after parent component is enabled.",
                picker.isEnabled());
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
