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

        List<LogEntry> logs = getWarningEntries();
        if (logs.size() != 0) {
            Assert.assertEquals(
                    "Expected only [Deprecation] warning should be in the logs",
                    1, logs.size());
            Assert.assertThat(logs.get(0).getMessage(),
                    CoreMatchers.containsString("HTML Imports"));
            Assert.assertThat(logs.get(0).getMessage(),
                    CoreMatchers.containsString("deprecated"));
        }

        localePicker = $(DatePickerElement.class)
                .id("german-locale-date-picker");
        localePicker.setDate(LocalDate.of(1985, 1, 10));
        findElement(By.tagName("body")).click();

        logs = getWarningEntries();

        Assert.assertTrue("No new warnings should have appeared in the logs",
                logs.isEmpty());

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
}
