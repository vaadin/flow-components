/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.SpreadsheetUtil;

class ParseNumberTest {

    @Test
    void testNumberParsingWithEnLocale() {
        Locale locale = new Locale("en");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        Assertions.assertNull(result);
    }

    @Test
    void testNumberParsingWithFiLocale() {
        Locale locale = new Locale("fi");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        Assertions.assertNull(result);
    }

    @Test
    void testNumberParsingWithItLocale() {
        Locale locale = new Locale("it");

        Double result = SpreadsheetUtil.parseNumber(null, locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("s42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42s", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("42", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,3", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 3", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4E2", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.2E2", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,2E2", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4.002", locale);
        Assertions.assertNotNull(result);

        result = SpreadsheetUtil.parseNumber("4 002.42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4,002.42", locale);
        Assertions.assertNull(result);

        result = SpreadsheetUtil.parseNumber("4.002,42", locale);
        Assertions.assertNotNull(result);
    }
}
