package com.vaadin.flow.component.datepicker;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
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
    public void noReferenceDate_usesCurrentDateAsReferenceDate() {
        // Due to an issue with date-fns the reference date always refers to the
        // start of the year, so use that for testing bounds
        LocalDate startOfThisYear = LocalDate.now()
                .with(TemporalAdjusters.firstDayOfYear());
        LocalDate endOfThisYear = LocalDate.now()
                .with(TemporalAdjusters.lastDayOfYear());

        // Test lower boundary
        LocalDate testDate = startOfThisYear.minusYears(50);
        enterShortYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test upper boundary
        testDate = endOfThisYear.plusYears(49);
        enterShortYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test full years
        testDate = LocalDate.of(2031, 1, 1);
        enterFullYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());

        testDate = LocalDate.of(30, 1, 1);
        enterFullYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());
    }

    @Test
    public void setCustomReferenceDate_usesCustomReferenceDate() {
        $("button").id("apply-custom-reference-date").click();

        // Test lower boundary
        LocalDate testDate = LocalDate.of(1950, 1, 1);
        enterShortYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test upper boundary
        testDate = LocalDate.of(2049, 12, 31);
        enterShortYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());

        // Test full years
        testDate = LocalDate.of(2031, 1, 1);
        enterFullYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());

        testDate = LocalDate.of(30, 1, 1);
        enterFullYearDate(testDate);
        Assert.assertEquals(testDate, picker.getDate());
    }

    private void enterShortYearDate(LocalDate date) {
        String formattedDate = date
                .format(DateTimeFormatter.ofPattern("MM/dd/yy"));
        picker.setInputValue(formattedDate);
        picker.sendKeys(Keys.TAB);
    }

    private void enterFullYearDate(LocalDate date) {
        String formattedDate = date
                .format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        picker.setInputValue(formattedDate);
        picker.sendKeys(Keys.TAB);
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
