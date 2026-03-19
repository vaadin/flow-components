/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.tests.MockUIExtension;

class LocaleTest {
    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    private Locale testLocale;

    @BeforeEach
    void init() {
        // Choose a test locale that differs from the system locale to ensure
        // we're actually testing that the UI locale is used, not the system
        // locale
        Locale systemLocale = Locale.getDefault(Locale.Category.FORMAT);
        testLocale = systemLocale.equals(Locale.GERMANY) ? Locale.FRANCE
                : Locale.GERMANY;

        ui.setLocale(testLocale);

        spreadsheet = new Spreadsheet();
    }

    @Test
    void default_getLocale_equalsUILocale() {
        Assertions.assertEquals(testLocale, spreadsheet.getLocale());
    }

    @Test
    void default_getCellValueManagerDecimalSymbols_equalsUILocale() {
        var cellValueManagerDecimalSymbols = spreadsheet.getCellValueManager()
                .getOriginalValueDecimalFormat().getDecimalFormatSymbols();
        var expectedSymbols = DecimalFormatSymbols.getInstance(testLocale);

        Assertions.assertEquals(expectedSymbols,
                cellValueManagerDecimalSymbols);
    }

    @Test
    void setNullLocale_throws() {
        Assertions.assertThrows(NullPointerException.class,
                () -> spreadsheet.setLocale(null));
    }

    @Test
    void setLocale_getLocale() {
        spreadsheet.setLocale(Locale.ITALIAN);
        Assertions.assertEquals(Locale.ITALIAN, spreadsheet.getLocale());
    }

}
