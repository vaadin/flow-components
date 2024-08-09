/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.SpreadsheetUtil;

public class ParseNumberTest {

    @Test
    public void testNumberParsingWithEnLocale() {
        Locale locale = new Locale("en");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        Assert.assertNull(result);
    }

    @Test
    public void testNumberParsingWithFiLocale() {
        Locale locale = new Locale("fi");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        Assert.assertNull(result);
    }

    @Test
    public void testNumberParsingWithItLocale() {
        Locale locale = new Locale("it");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        Assert.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        Assert.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        Assert.assertNotNull(result);
    }
}
