package com.vaadin.flow.component.spreadsheet.tests;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class LocaleTest {

    private Spreadsheet spreadsheet;

    @Before
    public void init() {
        // make sure the default system locale will not be the same as the UI
        // locale used in the test. Otherwise the tests could be false
        // positives.
        Assert.assertNotEquals(Locale.GERMANY,
                Locale.getDefault(Locale.Category.FORMAT));

        final UI ui = new UI();
        ui.setLocale(Locale.GERMANY);
        UI.setCurrent(ui);

        spreadsheet = new Spreadsheet();
    }

    @Test
    public void default_getLocale_equalsUILocale() {
        Assert.assertEquals(Locale.GERMANY, spreadsheet.getLocale());
    }

    @Test
    public void default_getCellValueManagerDecimalSymbols_equalsUILocale() {
        var cellValueManagerDecimalSymbols = spreadsheet.getCellValueManager()
                .getOriginalValueDecimalFormat().getDecimalFormatSymbols();
        var expectedGermanySymbols = DecimalFormatSymbols
                .getInstance(Locale.GERMANY);

        Assert.assertEquals(expectedGermanySymbols,
                cellValueManagerDecimalSymbols);
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
