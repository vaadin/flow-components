/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;
import com.vaadin.testbench.TestBenchElement;

@TestPath("vaadin-date-picker/date-picker-locale")
public class DatePickerLocaleIT extends AbstractComponentIT {

    private static final String DATEPICKER_OVERLAY = "vaadin-date-picker-overlay";

    @Test
    public void testPickerWithValueAndLocaleFromServerSideDifferentCtor() {
        open();

        DatePickerElement localePicker = $(DatePickerElement.class)
                .id("locale-picker-server-with-value");
        WebElement displayText = localePicker.$(TestBenchElement.class)
                .id("input");

        Assert.assertEquals("Wrong initial date in field.", "2018/4/23",
                executeScript("return arguments[0].value", displayText));

        findElement(By.id("uk-locale")).click();
        Assert.assertEquals("Didn't have expected UK locale date.",
                "23/04/2018",
                executeScript("return arguments[0].value", displayText));

        assertText($(DatePickerElement.class).id("french-locale-date-picker"),
                "03/05/2018");

        for (LogEntry logEntry : getWarningEntries()) {
            Assert.assertThat(
                    "Expected only [Deprecation] warnings in the logs",
                    logEntry.getMessage(),
                    CoreMatchers.containsString("HTML Imports"));
            Assert.assertThat(logEntry.getMessage(),
                    CoreMatchers.containsString("deprecated"));
        }

        localePicker = $(DatePickerElement.class)
                .id("german-locale-date-picker");
        localePicker.setDate(LocalDate.of(1985, 1, 10));
        findElement(By.tagName("body")).click();

        Assert.assertTrue("No new warnings should have appeared in the logs",
                getWarningEntries().isEmpty());

        assertText(localePicker, "10.1.1985");

        assertText($(DatePickerElement.class).id("korean-locale-date-picker"),
                "2018. 5. 3.");

        assertText($(DatePickerElement.class).id("polish-locale-date-picker"),
                "3.05.2018");

    }

    private void assertText(DatePickerElement datePickerElement,
            String expected) {
        WebElement displayText = datePickerElement.$(TestBenchElement.class)
                .id("input");
        Assert.assertEquals("Didn't have expected locale date.", expected,
                executeScript("return arguments[0].value", displayText));
    }

    private List<LogEntry> getWarningEntries() {
        LogEntries logs = driver.manage().logs().get("browser");
        return logs.getAll().stream()
                .filter(log -> log.getLevel().equals(Level.WARNING))
                .collect(Collectors.toList());
    }

    @Test
    public void polishLocaleTest() {
        open();

        checkLogsForErrors();
        WebElement polishPicker = findElement(
                By.id("polish-locale-date-picker"));
        // trigger the validation on the from clientside
        polishPicker.click();
        executeScript("document.body.click()");

        checkLogsForErrors();
    }

    @Test
    public void noReferenceDate_usesCurrentDateAsReferenceDate() {
        open();
        DatePickerElement picker = $(DatePickerElement.class)
                .id("reference-date-picker");

        // Due to an issue with date-fns the reference date always refers to the
        // start of the year, so use that for testing bounds
        LocalDate startOfThisYear = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfYear());
        LocalDate endOfThisYear = LocalDate.now()
                .with(TemporalAdjusters.lastDayOfYear());

        // Test lower boundary
        LocalDate testDate = startOfThisYear.minusYears(50);
        enterShortYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test upper boundary
        testDate = endOfThisYear.plusYears(49);
        enterShortYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test full years
        testDate = LocalDate.of(2031, 1, 1);
        enterFullYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());

        testDate = LocalDate.of(30, 1, 1);
        enterFullYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());
    }

    @Test
    public void setCustomReferenceDate_usesCustomReferenceDate() {
        open();

        $("button").id("apply-custom-reference-date").click();
        DatePickerElement picker = $(DatePickerElement.class)
                .id("reference-date-picker");

        // Test lower boundary
        LocalDate testDate = LocalDate.of(1950, 1, 1);
        enterShortYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test upper boundary
        testDate = LocalDate.of(2049, 12, 31);
        enterShortYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test full years
        testDate = LocalDate.of(2031, 1, 1);
        enterFullYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());

        testDate = LocalDate.of(30, 1, 1);
        enterFullYearDate(picker, testDate);
        Assert.assertEquals(testDate, picker.getDate());
    }

    private void enterShortYearDate(DatePickerElement picker, LocalDate date) {
        String formattedDate = date
                .format(DateTimeFormatter.ofPattern("MM/dd/yy"));
        picker.setInputValue(formattedDate);
        picker.sendKeys(Keys.TAB);
    }

    private void enterFullYearDate(DatePickerElement picker, LocalDate date) {
        String formattedDate = date
                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        picker.setInputValue(formattedDate);
        picker.sendKeys(Keys.TAB);
    }
}
