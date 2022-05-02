package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.GregorianCalendar;

public class ValueHandlerIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        createNewSpreadsheet();
        loadTestFixture(TestFixtures.ValueChangeHandler);
    }

    @Test
    public void testDoubleHandler() {
        selectCell("B3");
        getSpreadsheet().getCellAt("B3").setValue("=B2+1");

        getSpreadsheet().getCellAt("B2").setValue("3");

        Assert.assertEquals("6", getCellContent("B2"));
        Assert.assertEquals("7", getCellContent("B3"));

        getSpreadsheet().getCellAt("B2").setValue("314");

        Assert.assertEquals("628", getCellContent("B2"));
        Assert.assertEquals("629", getCellContent("B3"));

        getSpreadsheet().getCellAt("B2").setValue("text");
        Assert.assertEquals("text", getCellContent("B2"));
    }

    @Test
    @Ignore("Fails in all the browsers in framework Spreadsheet")
    public void testDateFormat() {
        selectCell("C2");
        getSpreadsheet().getCellAt("C2")
                .setValue(dateTS(2000, GregorianCalendar.MARCH, 14));
        selectCell("A1");
        Assert.assertEquals("14-Mar-00", getCellContent("C2"));
        Assert.assertEquals("15-Mar-00", getCellContent("C3"));

        getSpreadsheet().getCellAt("C2")
                .setValue(dateTS(2005, GregorianCalendar.APRIL, 30));
        selectCell("A1");
        Assert.assertEquals("30-Apr-05", getCellContent("C2"));
        Assert.assertEquals("1-May-05", getCellContent("C3"));

        getSpreadsheet().getCellAt("C2").setValue("28-Jun-06");
        selectCell("A1");
        Assert.assertEquals("28-Jun-06", getCellContent("C2"));
        Assert.assertEquals("29-Jun-06", getCellContent("C3"));
    }

    private String dateTS(int year, int month, int day) {
        return "" + new GregorianCalendar(year, month, day).getTimeInMillis();
    }
}
