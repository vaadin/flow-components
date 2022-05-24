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
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.testbench.TestBenchElement;
import com.vaadin.testbench.TestBenchTestCase;

import static org.junit.Assert.assertTrue;

/**
 * Integration tests for the {@link DatePickerViewDemoPage}.
 */
@TestPath("vaadin-date-picker-test-demo")
public class DatePickerIT extends AbstractComponentIT {

    private static final String DATEPICKER_OVERLAY = "vaadin-date-picker-overlay";
    private TestBenchTestCase layout;

    @Before
    public void init() {
        open();
        waitForElementPresent(By.tagName("vaadin-date-picker"));
        layout = this;
    }

    @Test
    public void selectDateOnSimpleDatePicker() {
        WebElement picker = layout.findElement(By.id("simple-picker"));
        WebElement message = layout.findElement(By.id("simple-picker-message"));

        executeScript("arguments[0].value = '1985-01-10'", picker);

        waitUntil(driver -> message.getText()
                .contains("Day: 10\nMonth: 1\nYear: 1985"));

        executeScript("arguments[0].value = ''", picker);

        waitUntil(driver -> "No date is selected".equals(message.getText()));
    }

    @Test
    public void selectDateOnMinMaxDatePicker() {
        WebElement picker = layout.findElement(By.id("min-and-max-picker"));
        WebElement message = layout
                .findElement(By.id("min-and-max-picker-message"));

        LocalDate now = LocalDate.now();
        executeScript("arguments[0].value = arguments[1]", picker,
                now.toString());

        Assert.assertEquals("The selected date should be considered valid",
                false, executeScript("return arguments[0].invalid", picker));

        waitUntil(driver -> message.getText()
                .contains(("Day: " + now.getDayOfMonth() + "\nMonth: "
                        + now.getMonthValue() + "\nYear: " + now.getYear())));

        executeScript("arguments[0].value = ''", picker);

        waitUntil(driver -> "No date is selected".equals(message.getText()));

        Assert.assertEquals("The empty date should be considered valid", false,
                executeScript("return arguments[0].invalid", picker));

        LocalDate invalid = now.minusYears(1);

        executeScript("arguments[0].value = arguments[1]", picker,
                invalid.toString());

        Assert.assertEquals("The selected date should be considered invalid",
                true, executeScript("return arguments[0].invalid", picker));
    }

    @Test
    public void selectDateOnFinnishDatePicker() {
        WebElement picker = layout.findElement(By.id("finnish-picker"));
        WebElement message = layout
                .findElement(By.id("finnish-picker-message"));

        executeScript("arguments[0].value = '1985-01-10'", picker);

        waitUntil(driver -> "Day of week: torstai\nMonth: tammiku"
                .equals(message.getText()));

        executeScript("arguments[0].value = ''", picker);

        waitUntil(driver -> "No date is selected".equals(message.getText()));

        executeScript("arguments[0].setAttribute(\"opened\", true)", picker);
        waitForElementPresent(By.tagName(DATEPICKER_OVERLAY));

        TestBenchElement overlay = $(DATEPICKER_OVERLAY).first();
        TestBenchElement content = overlay.$("*").id("content");
        TestBenchElement overlayContent = content.$("*").id("overlay-content");
        WebElement todayButton = overlayContent.$("*").id("todayButton");

        waitUntil(driver -> "tänään".equals(todayButton.getText()));
    }

    @Test
    public void selectDatesOnLinkedDatePickers() {
        WebElement startPicker = layout.findElement(By.id("start-picker"));
        WebElement endPicker = layout.findElement(By.id("end-picker"));
        WebElement message = layout.findElement(By.id("start-and-end-message"));

        executeScript("arguments[0].value = '1985-01-10'", startPicker);

        waitUntil(driver -> "Select the ending date".equals(message.getText()));

        Assert.assertEquals(
                "The min date at the end date picker should be 1985-01-11",
                true, executeScript("return arguments[0].min === '1985-01-11'",
                        endPicker));

        executeScript("arguments[0].value = '1985-01-20'", endPicker);

        waitUntil(driver -> "Selected period:\nFrom 1985-01-10 to 1985-01-20"
                .equals(message.getText()));

        Assert.assertEquals(
                "The max date at the start date picker should be 1985-01-19",
                true, executeScript("return arguments[0].max === '1985-01-19'",
                        startPicker));

        executeScript("arguments[0].value = ''", startPicker);
        waitUntil(
                driver -> "Select the starting date".equals(message.getText()));
    }

    @Test
    public void selectDatesOnCustomLocaleDatePickers() {
        WebElement localePicker = layout
                .findElement(By.id("locale-change-picker"));
        WebElement message = layout
                .findElement(By.id("Customize-locale-picker-message"));
        WebElement displayText = localePicker.findElement(By.tagName("input"));
        executeScript("arguments[0].value = '2018-03-27'", localePicker);

        waitUntil(driver -> message.getText()
                .contains("Day: 27\nMonth: 3\nYear: 2018\nLocale:"));

        Assert.assertEquals(
                "The format of the displayed date should be MM/DD/YYYY.", true,
                executeScript("return arguments[0].value === '3/27/2018'",
                        displayText));

        layout.findElement(By.id("Locale-UK")).click();
        executeScript("arguments[0].value = '2018-03-26'", localePicker);
        waitUntil(driver -> "Day: 26\nMonth: 3\nYear: 2018\nLocale: en_GB"
                .equals(message.getText()));

        Assert.assertEquals(
                "The format of the displayed date should be DD/MM/YYYY.", true,
                executeScript("return arguments[0].value === '26/03/2018'",
                        displayText));

        layout.findElement(By.id("Locale-US")).click();
        executeScript("arguments[0].value = '2018-03-25'", localePicker);
        waitUntil(driver -> "Day: 25\nMonth: 3\nYear: 2018\nLocale: en_US"
                .equals(message.getText()));
        Assert.assertEquals(
                "The format of the displayed date should be MM/DD/YYYY.", true,
                executeScript("return arguments[0].value === '3/25/2018'",
                        displayText));

        layout.findElement(By.id("Locale-UK")).click();
        assertTrue((Boolean) executeScript(
                "return arguments[0].value === '25/03/2018'", displayText));
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
        TestBenchElement message = $("div")
                .id("Customize-locale-picker-message");

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 7),
                "3/7/900");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 7),
                "3/7/87");

        $("button").id("Locale-UK").click();
        Assert.assertEquals("07/03/87", localePicker.getInputValue());

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 6),
                "06/03/900");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 6),
                "06/03/87");

        $("button").id("Locale-US").click();
        Assert.assertEquals("3/6/87", localePicker.getInputValue());

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 5),
                "3/5/900");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 5),
                "3/5/87");

        $("button").id("Locale-CHINA").click();
        Assert.assertEquals("87/3/5", localePicker.getInputValue());

        setDateAndAssert(localePicker, LocalDate.of(900, Month.MARCH, 4),
                "900/3/4");
        setDateAndAssert(localePicker, LocalDate.of(87, Month.MARCH, 4),
                "87/3/4");

        $("button").id("Locale-UK").click();
        Assert.assertEquals("04/03/87", localePicker.getInputValue());
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
        TestBenchElement message = $("div")
                .id("Customize-locale-picker-message");

        setInputValueAndAssert(localePicker, "3/7/0900", "3/7/900",
                LocalDate.of(900, Month.MARCH, 7));

        setInputValueAndAssert(localePicker, "3/6/900",
                LocalDate.of(900, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "3/5/0087", "3/5/87",
                LocalDate.of(87, Month.MARCH, 5));
        setInputValueAndAssert(localePicker, "3/6/87",
                LocalDate.of(87, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "3/7/20",
                LocalDate.of(20, Month.MARCH, 7));
        setInputValueAndAssert(localePicker, "3/8/0020", "3/8/20",
                LocalDate.of(20, Month.MARCH, 8));

        $("button").id("Locale-UK").click();
        Assert.assertEquals("08/03/20", localePicker.getInputValue());

        setInputValueAndAssert(localePicker, "7/3/0900", "07/03/900",
                LocalDate.of(900, Month.MARCH, 7));

        setInputValueAndAssert(localePicker, "6/3/900", "06/03/900",
                LocalDate.of(900, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "5/3/0087", "05/03/87",
                LocalDate.of(87, Month.MARCH, 5));
        setInputValueAndAssert(localePicker, "6/3/87", "06/03/87",
                LocalDate.of(87, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "7/3/20", "07/03/20",
                LocalDate.of(20, Month.MARCH, 7));
        setInputValueAndAssert(localePicker, "8/3/0020", "08/03/20",
                LocalDate.of(20, Month.MARCH, 8));

        $("button").id("Locale-CHINA").click();
        Assert.assertEquals("20/3/8", localePicker.getInputValue());

        setInputValueAndAssert(localePicker, "0900/3/7", "900/3/7",
                LocalDate.of(900, Month.MARCH, 7));

        setInputValueAndAssert(localePicker, "900/3/6",
                LocalDate.of(900, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "0087/3/5", "87/3/5",
                LocalDate.of(87, Month.MARCH, 5));
        setInputValueAndAssert(localePicker, "87/3/6",
                LocalDate.of(87, Month.MARCH, 6));
        setInputValueAndAssert(localePicker, "20/3/7",
                LocalDate.of(20, Month.MARCH, 7));
        setInputValueAndAssert(localePicker, "0020/3/8", "20/3/8",
                LocalDate.of(20, Month.MARCH, 8));

        $("button").id("Locale-US").click();
        Assert.assertEquals("3/8/20", localePicker.getInputValue());
    }
}
