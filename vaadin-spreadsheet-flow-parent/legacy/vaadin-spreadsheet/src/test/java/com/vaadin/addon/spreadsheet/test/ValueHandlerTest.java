package com.vaadin.addon.spreadsheet.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;

public class ValueHandlerTest extends AbstractSpreadsheetTestCase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        headerPage.createNewSpreadsheet();
        headerPage.loadTestFixture(TestFixtures.ValueChangeHandler);
    }

    @Test
    public void testDoubleHandler() {
        sheetController.putCellContent("B3", "=B2+1");

        sheetController.putCellContent("B2", "3");

        assertThat(sheetController.getCellContent("B2"), is("6"));
        assertThat(sheetController.getCellContent("B3"), is("7"));

        sheetController.putCellContent("B2", "314");

        assertThat(sheetController.getCellContent("B2"), is("628"));
        assertThat(sheetController.getCellContent("B3"), is("629"));

        sheetController.putCellContent("B2", "text");
        assertThat(sheetController.getCellContent("B2"), is("text"));

    }

    @Test
    @Ignore("Fails in all the browsers")
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
