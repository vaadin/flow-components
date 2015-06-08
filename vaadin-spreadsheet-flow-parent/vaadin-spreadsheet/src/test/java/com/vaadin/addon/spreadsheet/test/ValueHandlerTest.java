package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Fails in all the browsers")
public class ValueHandlerTest extends Test1 {

    @Before
    public void loadFixture() {
        loadServerFixture("CELL_VALUE_HANDLER");
    }

    @Test
    public void testDoubleHandler() {
        sheetController.putCellContent("B3", "=B2+1");

        sheetController.putCellContent("B2", "3");
        sheetController.clickCell("A1");
        assertThat(sheetController.getCellContent("B2"), is("6"));
        assertThat(sheetController.getCellContent("B3"), is("7"));

        sheetController.putCellContent("B2", "3.14");
        sheetController.clickCell("A1");
        assertThat(sheetController.getCellContent("B2"), is("6.28"));
        assertThat(sheetController.getCellContent("B3"), is("7.28"));

        sheetController.putCellContent("B2", "text");
        sheetController.clickCell("A1");
        assertThat(sheetController.getCellContent("B2"), is("text"));
    }

    @Test
    public void testDateFormat() {
        sheetController.putCellContent("C2",
                dateTS(2000, GregorianCalendar.MARCH, 14));
        sheetController.clickCell("A1");
        assertThat(sheetController.getCellContent("C2"), is("14-Mar-00"));
        assertThat(sheetController.getCellContent("C3"), is("15-Mar-00"));

        sheetController.putCellContent("C2",
                dateTS(2005, GregorianCalendar.APRIL, 30));
        sheetController.clickCell("A1");
        assertThat(sheetController.getCellContent("C2"), is("30-Apr-05"));
        assertThat(sheetController.getCellContent("C3"), is("1-May-05"));

        sheetController.putCellContent("C2", "28-Jun-06");
        sheetController.clickCell("A1");
        assertThat(sheetController.getCellContent("C2"), is("28-Jun-06"));
        assertThat(sheetController.getCellContent("C3"), is("29-Jun-06"));
    }

    private String dateTS(int year, int month, int day) {
        return "" + new GregorianCalendar(year, month, day).getTimeInMillis();
    }
}
