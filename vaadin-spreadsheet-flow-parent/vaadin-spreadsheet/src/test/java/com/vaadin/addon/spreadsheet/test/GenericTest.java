package com.vaadin.addon.spreadsheet.test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class GenericTest extends AbstractSpreadsheetTestCase {

    @Test
    public void testKeyboardNavigation() {
        headerPage.createNewSpreadsheet();
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        a1.setValue("X");

        new Actions(getDriver())
                .sendKeys(Keys.ARROW_RIGHT).sendKeys(Keys.ARROW_RIGHT).sendKeys(Keys.ARROW_DOWN)
                .sendKeys(Keys.ARROW_DOWN).sendKeys(Keys.ARROW_LEFT)
                .sendKeys(Keys.ARROW_UP).sendKeys("Y").sendKeys(Keys.RETURN).sendKeys(Keys.ENTER).build().perform();

        final SheetCellElement c2 = $(SpreadsheetElement.class).first()
                .getCellAt("C2");
        Assert.assertEquals("X", a1.getValue());
        Assert.assertEquals("Y", c2.getValue());
    }

    @Test
    public void testDates() {
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.US);
        headerPage.createNewSpreadsheet();
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");
        final SheetCellElement a2 = $(SpreadsheetElement.class).first()
                .getCellAt("A2");
        final SheetCellElement b1 = $(SpreadsheetElement.class).first()
                .getCellAt("B1");
        a1.setValue("=TODAY()");
        a2.setValue("6/7/2009");
        b1.setValue("=A1+3");

        testBench(driver).waitForVaadin();
        Calendar start = new GregorianCalendar(1900, Calendar.JANUARY, 0);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR, 1);
        Long today = new Long((now.getTime().getTime() - start.getTime()
                .getTime()) / (1000 * 60 * 60 * 24));

        Assert.assertEquals(today.toString(), a1.getValue());
        Assert.assertEquals(today + 3, Long.parseLong(b1.getValue()));
    }

    @Test
    public void testFormats() {
        //TODO Vaadin8 use setLocale instead of setLocaleForNativeSElect
        //When https://github.com/vaadin/framework8-issues/issues/477 is fixed
        setLocale(Locale.US);
        SpreadsheetPage spreadsheet = headerPage.createNewSpreadsheet();
        headerPage.loadTestFixture(TestFixtures.Formats);
        final SheetCellElement a1 = $(SpreadsheetElement.class).first()
                .getCellAt("A1");

        Assert.assertEquals("example", spreadsheet.getCellValue("B2"));
        Assert.assertEquals("example", spreadsheet.getCellValue("C2"));
        Assert.assertEquals("example", spreadsheet.getCellValue("D2"));
        Assert.assertEquals("example", spreadsheet.getCellValue("E2"));
        Assert.assertEquals("example", spreadsheet.getCellValue("F2"));

        Assert.assertEquals("38247.12269", spreadsheet.getCellValue("B3"));
        Assert.assertEquals("38247.12", spreadsheet.getCellValue("C3"));
        Assert.assertEquals("3824712.27%", spreadsheet.getCellValue("D3"));
        Assert.assertEquals("17-Sep-04", spreadsheet.getCellValue("E3"));
        Assert.assertEquals("3.82E+04", spreadsheet.getCellValue("F3"));

        Assert.assertEquals("3.1415", spreadsheet.getCellValue("B6"));
        Assert.assertEquals("3.14", spreadsheet.getCellValue("C6"));
        Assert.assertEquals("314.15%", spreadsheet.getCellValue("D6"));
        Assert.assertEquals("3-Jan-00", spreadsheet.getCellValue("E6"));
        Assert.assertEquals("3.14E+00", spreadsheet.getCellValue("F6"));
    }

    @Test
    public void testStringCellType() {
        SpreadsheetPage spreadsheet = headerPage.createNewSpreadsheet();
        headerPage.loadTestFixture(TestFixtures.Formats);
        final SheetCellElement b2 = $(SpreadsheetElement.class).first()
                .getCellAt("B2");

        b2.setValue("example");
        Assert.assertEquals("example", spreadsheet.getCellValue("B2"));

        b2.setValue("12");
        Assert.assertEquals("12", spreadsheet.getCellValue("B2"));

        b2.setValue("example 2");
        Assert.assertEquals("example 2", spreadsheet.getCellValue("B2"));
    }
}
