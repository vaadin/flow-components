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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class LocaleTest {

    private Spreadsheet spreadsheet;

    private Locale testLocale;

    private UI ui;

    @Before
    public void init() {
        // Choose a test locale that differs from the system locale to ensure
        // we're actually testing that the UI locale is used, not the system
        // locale
        Locale systemLocale = Locale.getDefault(Locale.Category.FORMAT);
        testLocale = systemLocale.equals(Locale.GERMANY) ? Locale.FRANCE
                : Locale.GERMANY;

        ui = new UI();
        ui.setLocale(testLocale);
        UI.setCurrent(ui);

        spreadsheet = new Spreadsheet();
    }

    @After
    public void tearDown() {
        UI.setCurrent(null);
        ui = null;
    }

    @Test
    public void default_getLocale_equalsUILocale() {
        Assert.assertEquals(testLocale, spreadsheet.getLocale());
    }

    @Test
    public void default_getCellValueManagerDecimalSymbols_equalsUILocale() {
        var cellValueManagerDecimalSymbols = spreadsheet.getCellValueManager()
                .getOriginalValueDecimalFormat().getDecimalFormatSymbols();
        var expectedSymbols = DecimalFormatSymbols.getInstance(testLocale);

        Assert.assertEquals(expectedSymbols, cellValueManagerDecimalSymbols);
    }

    @Test(expected = NullPointerException.class)
    public void setNullLocale_throws() {
        spreadsheet.setLocale(null);
    }

    @Test
    public void setLocale_getLocale() {
        spreadsheet.setLocale(Locale.ITALIAN);
        Assert.assertEquals(Locale.ITALIAN, spreadsheet.getLocale());
    }

}
