package com.vaadin.addon.spreadsheet.test;

import com.vaadin.addon.spreadsheet.SpreadsheetUtil;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ParseNumberTest {

    @Test
    public void testNumberParsingWithEnLocale() {
        Locale locale = new Locale("en");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        assertNull(result);
    }

    @Test
    public void testNumberParsingWithFiLocale() {
        Locale locale = new Locale("fi");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        assertNull(result);
    }

    @Test
    public void testNumberParsingWithItLocale() {
        Locale locale = new Locale("it");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        assertNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        assertNotNull(result);
    }

}
