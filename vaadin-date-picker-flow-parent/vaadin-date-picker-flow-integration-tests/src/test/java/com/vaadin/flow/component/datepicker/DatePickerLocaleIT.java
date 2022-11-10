package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;

import com.vaadin.flow.component.datepicker.testbench.DatePickerElement;
import com.vaadin.tests.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-date-picker/date-picker-locale")
public class DatePickerLocaleIT extends AbstractComponentIT {

    private DatePickerElement picker;

    @Before
    public void init() {
        open();
        picker = $(DatePickerElement.class).id("picker");
    }

    @Test
    public void datePickerWithValue_setLocale_assertDisplayedValue() {
        picker.setDate(LocalDate.of(2018, Month.MAY, 3));

        applyLocale(Locale.CHINA);
        Assert.assertEquals("Should display the value using China date format",
                "2018/5/3", picker.getInputValue());

        applyLocale(Locale.FRENCH);
        Assert.assertEquals("Should display the value using French date format",
                "03/05/2018", picker.getInputValue());

        applyLocale(new Locale("ko", "KR"));
        Assert.assertEquals("Should display the value using Korean date format",
                "2018. 5. 3.", picker.getInputValue());

        applyLocale(new Locale("pl", "PL"));
        Assert.assertEquals("Should display the value using Polish date format",
                "3.05.2018", picker.getInputValue());

        applyLocale(new Locale("bg"));
        Assert.assertEquals(
                "Should display the value using Bulgarian date format",
                "3.05.2018 г.", picker.getInputValue());

        assertNoWarnings();
    }

    @Test
    public void datePickerWithLocale_enterValue_assertParsedValue() {
        LocalDate mayThird = LocalDate.of(2018, Month.MAY, 3);

        applyLocale(Locale.CHINA);
        picker.setInputValue("2018/5/3");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should parse the value using China date format",
                mayThird, picker.getDate());

        picker.setDate(null);
        applyLocale(Locale.FRENCH);
        picker.setInputValue("03/05/2018");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should parse the value using French date format",
                mayThird, picker.getDate());

        picker.setDate(null);
        applyLocale(new Locale("ko", "KR"));
        picker.setInputValue("2018. 5. 3.");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should parse the value using Korean date format",
                mayThird, picker.getDate());

        picker.setDate(null);
        applyLocale(new Locale("pl", "PL"));
        picker.setInputValue("3.05.2018");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should parse the value using Polish date format",
                mayThird, picker.getDate());

        picker.setDate(null);
        applyLocale(new Locale("bg"));
        picker.setInputValue("3.05.2018 г.");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals(
                "Should parse the value using Bulgarian date format", mayThird,
                picker.getDate());

        assertNoWarnings();
    }

    @Test
    public void datePickerWithValueAndLocale_blur_assertDisplayedValue() {
        picker.setDate(LocalDate.of(2018, Month.MAY, 3));
        applyLocale(new Locale("pl", "PL"));

        // trigger the validation on the from clientside
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should display the value using Polish date format",
                "3.05.2018", picker.getInputValue());

        checkLogsForErrors();

        assertNoWarnings();
    }

    @Test
    public void datePicker_setValue_setLocale_assertDisplayedValue() {
        picker.setDate(LocalDate.of(2018, Month.MAY, 3));

        applyLocale(Locale.UK);

        Assert.assertEquals("Should display the value using UK date format",
                "03/05/2018", picker.getInputValue());
        assertNoWarnings();
    }

    @Test
    public void datePicker_setInvalidLocale_warningIsShown() {
        assertNoWarnings();

        applyLocale(new Locale("i", "i", "i"));

        waitUntil(driver -> getWarningEntries().toString().contains(
                "The locale is not supported, using default locale setting(en-US)."));
    }

    @Test
    public void datePicker_setInvalidLocale_defaultUSLocaleIsUsed() {
        applyLocale(new Locale("i", "i", "i"));
        picker.setDate(LocalDate.of(2018, Month.MAY, 3));

        Assert.assertEquals(
                "Should display the value using the default US date format",
                "5/3/2018", picker.getInputValue());
    }

    @Test
    public void datePicker_setUnsupportedLocale_warningIsShown() {
        assertNoWarnings();

        applyLocale(new Locale("th", "TH"));

        waitUntil(driver -> getWarningEntries().toString().contains(
                "The locale is not supported, using default locale setting(en-US)."));
    }

    @Test
    public void datePicker_setUnsupportedLocale_defaultUSLocaleIsUsed() {
        picker.setDate(LocalDate.of(2018, Month.MAY, 3));

        List<Locale> unsupportedLocales = List.of(new Locale("ar", "SA"),
                new Locale("th", "TH"), new Locale("fa"), new Locale("ks"));

        unsupportedLocales.forEach(unsupportedLocale -> {
            applyLocale(unsupportedLocale);

            Assert.assertEquals(
                    "Should display the value using the default US date format for locale "
                            + unsupportedLocale,
                    "5/3/2018", picker.getInputValue());
        });
    }

    @Test
    public void datePickerWithLocale_setInputValue_blur_assertDisplayedValue() {
        applyLocale(Locale.GERMAN);

        picker.setInputValue("3.5.2018");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("Should display the value using German date format",
                "3.5.2018", picker.getInputValue());

        assertNoWarnings();
    }

    @Test
    public void datePickerWithLocale_setInputValue_blur_defaultReferenceDateIsUsed() {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        int testYear1 = (currentYear + 51) % 100;
        int adjustedYear1 = getAdjustedYear(currentYear, testYear1);

        picker.setInputValue("02/27/" + testYear1);
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/" + adjustedYear1, picker.getInputValue());

        int testYear2 = (currentYear + 49) % 100;
        int adjustedYear2 = getAdjustedYear(currentYear, testYear2);
        picker.setInputValue("02/27/" + testYear2);
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/" + adjustedYear2, picker.getInputValue());

        picker.setInputValue("02/27/2031");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/2031", picker.getInputValue());

        picker.setInputValue("02/27/0030");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/0030", picker.getInputValue());
    }

    @Test
    public void datePickerWithLocale_setCustomReferenceDate_setInputValue_blur_customReferenceDateIsUsed() {
        $("button").id("apply-custom-reference-date").click();

        picker.setInputValue("02/27/31");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/1931", picker.getInputValue());

        picker.setInputValue("02/27/29");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/2029", picker.getInputValue());

        picker.setInputValue("02/27/2031");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/2031", picker.getInputValue());

        picker.setInputValue("02/27/0030");
        picker.sendKeys(Keys.TAB);
        Assert.assertEquals("2/27/0030", picker.getInputValue());
    }

    private void applyLocale(Locale locale) {
        TestBenchElement localeInput = $("input").id("locale-input");
        localeInput.setProperty("value", locale.toString());
        localeInput.dispatchEvent("change",
                Collections.singletonMap("bubbles", true));

        TestBenchElement applyLocale = $("button").id("apply-locale");
        applyLocale.click();
    }

    private List<LogEntry> getWarningEntries() {
        LogEntries logs = driver.manage().logs().get("browser");
        return logs.getAll().stream()
                .filter(log -> log.getLevel().equals(Level.WARNING))
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

    private int getAdjustedYear(int currentYear, int testYear) {
        int adjustedYear = testYear + (currentYear / 100) * 100;
        if (currentYear < adjustedYear - 50) {
            return adjustedYear - 100;
        }
        if (currentYear > adjustedYear + 50) {
            return adjustedYear + 100;
        }
        return adjustedYear;
    }
}
