package com.vaadin.flow.component.spreadsheet.tests;

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
        UI.setCurrent(new UI());
        UI.getCurrent().setLocale(Locale.US);
        spreadsheet = new Spreadsheet();
    }

    @Test
    public void default_getLocale_equalsUILocale() {
        Assert.assertEquals(Locale.US, spreadsheet.getLocale());
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
