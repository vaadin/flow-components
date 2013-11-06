package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class ValueHandlerTest extends Test1 {

    @Before
    public void loadFixture() {
        loadServerFixture("CELL_VALUE_HANDLER");
    }

    @Test
    public void testDoubleHandler() {
        c.putCellContent("B3", "=B2+1");

        c.putCellContent("B2", "3");
        assertThat(c.getCellContent("B2"), is("6"));
        assertThat(c.getCellContent("B3"), is("7"));

        c.putCellContent("B2", "3.14");
        assertThat(c.getCellContent("B2"), is("6.28"));
        assertThat(c.getCellContent("B3"), is("7.28"));

        c.putCellContent("B2", "text");
        assertThat(c.getCellContent("B2"), is("text"));
    }

    @Test
    public void testDateFormat() {
        c.putCellContent("C2", dateTS(2000, GregorianCalendar.MARCH, 14));
        assertThat(c.getCellContent("C2"), is("14-Mar-00"));
        assertThat(c.getCellContent("C3"), is("15-Mar-00"));

        c.putCellContent("C2", dateTS(2005, GregorianCalendar.APRIL, 30));
        assertThat(c.getCellContent("C2"), is("30-Apr-05"));
        assertThat(c.getCellContent("C3"), is("1-May-05"));

        c.putCellContent("C2", "28-Jun-06");
        assertThat(c.getCellContent("C2"), is("28-Jun-06"));
        assertThat(c.getCellContent("C3"), is("29-Jun-06"));
    }

    private String dateTS(int year, int month, int day) {
        return "" + new GregorianCalendar(year, month, day).getTimeInMillis();
    }
}
