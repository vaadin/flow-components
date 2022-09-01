package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-picker/date-picker-locale")
public class DatePickerLocaleIT extends AbstractComponentIT {

    @Test
    public void datePickerWithValueAndLocale_assertDisplayedValue() {
        open();

        DatePickerElement frenchLocalePicker = $(DatePickerElement.class)
                .id("picker-with-value-and-french-locale");
        Assert.assertEquals("Should display the correct value when using French locale",
                "03/05/2018", frenchLocalePicker.getInputValue());

        DatePickerElement koreanLocalePicker = $(DatePickerElement.class)
                .id("picker-with-value-and-korean-locale");
        Assert.assertEquals(
                "Should display the correct value when using Korean locale",
                "2018. 5. 3.", koreanLocalePicker.getInputValue());

        DatePickerElement polishLocalePicker = $(DatePickerElement.class)
                .id("picker-with-value-and-polish-locale");
        Assert.assertEquals(
                "Should display the correct value when using Polish locale",
                "3.05.2018", polishLocalePicker.getInputValue());

        for (LogEntry logEntry : getWarningEntries()) {
            Assert.assertThat(
                    "Expected only [Deprecation] warnings in the logs",
                    logEntry.getMessage(), CoreMatchers.containsString(
                            "'lit-element' module entrypoint is deprecated."));
            Assert.assertThat(logEntry.getMessage(),
                    CoreMatchers.containsString("deprecated"));
        }
    }

    @Test
    public void datePicker_setDate_setLocale_assertDisplayedValue() {
        open();

        DatePickerElement picker = $(DatePickerElement.class).id("picker");
        picker.setDate(LocalDate.of(2018, 4, 23));

        $("button").id("picker-set-uk-locale").click();
        Assert.assertEquals("23/04/2018", picker.getInputValue());
    }

    @Test
    public void datePickerWithValue_setLocale_assertDisplayedValue() {
        open();

        DatePickerElement picker = $(DatePickerElement.class)
                .id("picker-with-value");

        $("button").id("picker-with-value-set-uk-locale").click();
        Assert.assertEquals("23/04/2018", picker.getInputValue());
    }

    @Test
    public void datePickerWithValueAndLocale_clickInside_clickOutside_assertNoErrors() {
        open();

        checkLogsForErrors();
        WebElement picker = findElement(
                By.id("picker-with-value-and-polish-locale"));
        // trigger the validation on the from clientside
        picker.click();
        executeScript("document.body.click()");

        checkLogsForErrors();
    }

    @Test
    public void datePickerWithLocale_setDate_clickOutside_assertNoWarnings() {
        open();

        DatePickerElement picker = $(DatePickerElement.class)
                .id("picker-with-german-locale");

        picker.setDate(LocalDate.of(1985, 1, 10));
        findElement(By.tagName("body")).click();

        Assert.assertTrue("No new warnings should have appeared in the logs",
                getWarningEntries().isEmpty());

        Assert.assertEquals("10.1.1985", picker.getInputValue());
    }

    private void assertNoWarnings() {
        for (LogEntry logEntry : getWarningEntries()) {
            Assert.assertThat(
                    "Expected only [Deprecation] warnings in the logs",
                    logEntry.getMessage(), CoreMatchers.containsString(
                            "'lit-element' module entrypoint is deprecated."));
            Assert.assertThat(logEntry.getMessage(),
                    CoreMatchers.containsString("deprecated"));
        }
    }

    private List<LogEntry> getWarningEntries() {
        LogEntries logs = driver.manage().logs().get("browser");
        return logs.getAll().stream()
                .filter(log -> log.getLevel().equals(Level.WARNING))
                .filter(log -> !log.getMessage().contains("iron-icon"))
                .collect(Collectors.toList());
    }
}
