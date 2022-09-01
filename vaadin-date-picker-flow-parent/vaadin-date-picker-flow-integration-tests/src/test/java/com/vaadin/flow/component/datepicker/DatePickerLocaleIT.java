package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-picker/date-picker-locale")
public class DatePickerLocaleIT extends AbstractComponentIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void datePickerWithValueAndLocale_assertDisplayedValue() {
        DatePickerElement chinaLocalePicker = $(DatePickerElement.class)
                .id("picker-with-value-and-china-locale");
        Assert.assertEquals("Should display the value using China date format",
                "2018/5/3", chinaLocalePicker.getInputValue());

        DatePickerElement frenchLocalePicker = $(DatePickerElement.class)
                .id("picker-with-value-and-french-locale");
        Assert.assertEquals("Should display the value using French date format",
                "03/05/2018", frenchLocalePicker.getInputValue());

        DatePickerElement koreanLocalePicker = $(DatePickerElement.class)
                .id("picker-with-value-and-korean-locale");
        Assert.assertEquals("Should display the value using Korean date format",
                "2018. 5. 3.", koreanLocalePicker.getInputValue());

        DatePickerElement polishLocalePicker = $(DatePickerElement.class)
                .id("picker-with-value-and-polish-locale");
        Assert.assertEquals("Should display the value using Polish date format",
                "3.05.2018", polishLocalePicker.getInputValue());

        assertNoWarnings();
    }

    @Test
    public void datePickerWithValueAndLocale_blur_assertDisplayedValue() {
        DatePickerElement picker = $(DatePickerElement.class)
                .id("picker-with-value-and-polish-locale");
        // trigger the validation on the from clientside
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should display the value using Polish date format",
                "3.05.2018", picker.getInputValue());

        checkLogsForErrors();

        assertNoWarnings();
    }

    @Test
    public void datePicker_setValue_setLocale_assertDisplayedValue() {
        DatePickerElement picker = $(DatePickerElement.class).id("picker");

        picker.setDate(LocalDate.of(2018, Month.MAY, 3));
        $("button").id("picker-set-uk-locale").click();
        Assert.assertEquals("Should display the value using UK date format",
                "03/05/2018", picker.getInputValue());

        assertNoWarnings();
    }

    @Test
    public void datePickerWithValue_setLocale_assertDisplayedValue() {
        DatePickerElement picker = $(DatePickerElement.class)
                .id("picker-with-value");

        $("button").id("picker-with-value-set-uk-locale").click();
        Assert.assertEquals("Should display the value using UK date format",
                "03/05/2018", picker.getInputValue());

        assertNoWarnings();
    }

    @Test
    public void datePickerWithLocale_setInputValue_blur_assertDisplayedValue() {
        DatePickerElement picker = $(DatePickerElement.class)
                .id("picker-with-german-locale");

        picker.setInputValue("3.5.2018");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should display the value using German date format",
                "3.5.2018", picker.getInputValue());

        assertNoWarnings();
    }

    private List<LogEntry> getWarningEntries() {
        LogEntries logs = driver.manage().logs().get("browser");
        return logs.getAll().stream()
                .filter(log -> log.getLevel().equals(Level.WARNING))
                .filter(log -> !log.getMessage().contains("iron-icon"))
                .filter(log -> !log.getMessage().contains("deprecated"))
                .filter(log -> !log.getMessage().contains("Lit is in dev mode"))
                .collect(Collectors.toList());
    }

    private void assertNoWarnings() {
        for (LogEntry logEntry : getWarningEntries()) {
            throw new AssertionError(String.format(
                    "Received a warning in browser log console, message: %s",
                    logEntry));
        }
    }
}
