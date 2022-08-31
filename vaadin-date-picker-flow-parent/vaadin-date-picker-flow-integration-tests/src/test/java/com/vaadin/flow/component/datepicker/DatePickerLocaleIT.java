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
    public void datePickerWithValueAndLocaleFromServerSide_assertDisplayedValue() {
        open();

        Assert.assertEquals("03/05/2018", $(DatePickerElement.class)
                .id("french-locale-date-picker").getInputValue());

        Assert.assertEquals("2018. 5. 3.", $(DatePickerElement.class)
                .id("korean-locale-date-picker").getInputValue());

        Assert.assertEquals("3.05.2018", $(DatePickerElement.class)
                .id("polish-locale-date-picker").getInputValue());

        DatePickerElement localePicker = $(DatePickerElement.class)
                .id("german-locale-date-picker");
        localePicker.setDate(LocalDate.of(1985, 1, 10));
        findElement(By.tagName("body")).click();

        Assert.assertTrue("No new warnings should have appeared in the logs",
                getWarningEntries().isEmpty());

        Assert.assertEquals("10.1.1985", localePicker.getInputValue());

        assertNoWarnings();

    }

    @Test
    public void datePicker_setDifferentLocales_assertDisplayedValue() {
        open();

        DatePickerElement picker = $(DatePickerElement.class).id("picker");

        picker.setDate(LocalDate.of(2018, 4, 23));

        $("button").id("picker-set-uk-locale").click();
        Assert.assertEquals("23/04/2018", picker.getInputValue());

        $("button").id("picker-set-pl-locale").click();
        Assert.assertEquals("23.04.2018", picker.getInputValue());

        $("button").id("picker-set-sv-locale").click();
        Assert.assertEquals("2018-04-23", picker.getInputValue());

        assertNoWarnings();
    }

    @Test
    public void datePickerWithValue_setDifferentLocales_assertDisplayedValue() {
        open();

        DatePickerElement picker = $(DatePickerElement.class)
                .id("picker-with-value");

        $("button").id("picker-set-uk-locale").click();
        Assert.assertEquals("23/04/2018", picker.getInputValue());

        $("button").id("picker-set-pl-locale").click();
        Assert.assertEquals("23.04.2018", picker.getInputValue());

        $("button").id("picker-set-sv-locale").click();
        Assert.assertEquals("2018-04-23", picker.getInputValue());

        assertNoWarnings();
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
}
