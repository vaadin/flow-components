package com.vaadin.flow.component.spreadsheet.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

@TestPath("vaadin-spreadsheet")
public class GenericIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void testKeyboardNavigation() {
        final var a1 = getSpreadsheet().getCellAt("A1");
        a1.setValue("X");

        new Actions(getDriver()).sendKeys(Keys.ARROW_RIGHT)
                .sendKeys(Keys.ARROW_RIGHT).sendKeys(Keys.ARROW_DOWN)
                .sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ARROW_LEFT)
                .sendKeys(Keys.ARROW_UP).sendKeys("Y").sendKeys(Keys.RETURN)
                .sendKeys(Keys.ENTER).build().perform();

        final var c2 = getSpreadsheet().getCellAt("C2");
        Assert.assertEquals("X", a1.getValue());
        Assert.assertEquals("Y", c2.getValue());
    }

    @Test
    public void testDates() {
        setLocale(Locale.US);
        final var a1 = getSpreadsheet().getCellAt("A1");
        final var a2 = getSpreadsheet().getCellAt("A2");
        final var b1 = getSpreadsheet().getCellAt("B1");
        // FIXME: Test started failing because the first call to a1.setValue()
        // was adding content to the next line. Adding a random value first
        // seems to work around this issue.
        a1.setValue("SOMETHING");
        a1.setValue("=TODAY()");
        a2.setValue("6/7/2009");
        b1.setValue("=A1+3");

        Calendar start = new GregorianCalendar(1900, Calendar.JANUARY, 0);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR, 1);
        long today = (now.getTime().getTime() - start.getTime().getTime())
                / (1000 * 60 * 60 * 24);

        Assert.assertEquals(Long.toString(today), a1.getValue());
        Assert.assertEquals(today + 3, Long.parseLong(b1.getValue()));
    }

    @Test
    public void numericCell_newPercentageCell_cellStaysNumeric() {
        setLocale(Locale.US);
        // need to move selection so that fill indicator is not clicked while
        // selecting A2
        clickCell("F1");

        setCellValue("A2", "19");
        Assert.assertEquals("19", getCellValue("A2"));
        setCellValue("A3", "19%");
        Assert.assertEquals("19.00%", getCellValue("A3"));

        // force reload of the sheet
        addSheet();
        selectSheetAt(0);

        Assert.assertEquals("19", getCellValue("A2"));
        Assert.assertEquals("19.00%", getCellValue("A3"));
    }

    @Test
    public void percentageCell_newNumericCell_cellStaysNumeric() {
        setLocale(Locale.US);
        // need to move selection so that fill indicator is not clicked while
        // selecting A2
        clickCell("F1");

        setCellValue("A2", "19%");
        Assert.assertEquals("19.00%", getCellValue("A2"));
        setCellValue("A3", "19");
        Assert.assertEquals("19", getCellValue("A3"));

        // force reload of the sheet
        addSheet();
        selectSheetAt(0);

        Assert.assertEquals("19.00%", getCellValue("A2"));
        Assert.assertEquals("19", getCellValue("A3"));

    }

    @Ignore("Test is failing on TC probably because of a timezone issue")
    @Test
    public void testFormats() {
        setLocale(Locale.US);
        loadTestFixture(TestFixtures.Formats);
        final var a1 = getSpreadsheet().getCellAt("A1");

        Assert.assertEquals("example", getCellValue("B2"));
        Assert.assertEquals("example", getCellValue("C2"));
        Assert.assertEquals("example", getCellValue("D2"));
        Assert.assertEquals("example", getCellValue("E2"));
        Assert.assertEquals("example", getCellValue("F2"));

        Assert.assertEquals("38247.12269", getCellValue("B3"));
        Assert.assertEquals("38247.12", getCellValue("C3"));
        Assert.assertEquals("3824712.27%", getCellValue("D3"));
        Assert.assertEquals("17-Sep-04", getCellValue("E3"));
        Assert.assertEquals("3.82E+04", getCellValue("F3"));

        Assert.assertEquals("3.1415", getCellValue("B6"));
        Assert.assertEquals("3.14", getCellValue("C6"));
        Assert.assertEquals("314.15%", getCellValue("D6"));
        Assert.assertEquals("3-Jan-00", getCellValue("E6"));
        Assert.assertEquals("3.14E+00", getCellValue("F6"));
    }

    @Test
    public void testStringCellType() {
        loadTestFixture(TestFixtures.Formats);
        final var b2 = getSpreadsheet().getCellAt("B2");

        b2.setValue("example");
        Assert.assertEquals("example", getCellValue("B2"));

        b2.setValue("12");
        Assert.assertEquals("12", getCellValue("B2"));

        b2.setValue("example 2");
        Assert.assertEquals("example 2", getCellValue("B2"));
    }
}
