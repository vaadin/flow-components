package com.vaadin.addon.spreadsheet.test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;
import com.vaadin.testbench.annotations.RunLocally;
import com.vaadin.testbench.parallel.Browser;

@RunLocally(Browser.PHANTOMJS)
public class GenericTest extends Test1 {

    @Test
    public void testKeyboardNavigation() {

        SheetController c = keyboardSetup();

        c.selectCell("A2");
        c.selectCell("A1");

        c.insertAndRet("start").action(Keys.ARROW_UP)
                .action(Keys.ARROW_RIGHT).action(Keys.ARROW_RIGHT)
                .action(Keys.ARROW_RIGHT).action(Keys.ARROW_DOWN)
                .action(Keys.ARROW_DOWN).action(Keys.ARROW_LEFT)
.action(Keys.ARROW_UP)
                .insertAndRet("end");

        Assert.assertEquals("start", $(SpreadsheetElement.class).first()
                .getCellAt("A1").getValue());
        Assert.assertEquals("end", $(SpreadsheetElement.class).first()
                .getCellAt("C2").getValue());
    }

    @Test
    public void testDates() {

        SheetController c = keyboardSetup();
        new WebDriverWait(getDriver(), 20).until(ExpectedConditions
                .presenceOfElementLocated(By.className("sheet")));
        c.selectCell("A2");
        c.selectCell("A1");
        c.insertAndRet("=TODAY()");
        c.insertAndRet("6/7/2009");
        c.selectCell("B1");
        c.insertAndRet("=A1+3");

        testBench(driver).waitForVaadin();
        Calendar start = new GregorianCalendar(1900, Calendar.JANUARY, 0);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DAY_OF_YEAR, 1);
        Long today = new Long((now.getTime().getTime() - start.getTime()
                .getTime()) / (1000 * 60 * 60 * 24));
        c.selectCell("C1");
        Assert.assertEquals(today.toString(), c.getCellContent("A1"));
        Assert.assertEquals(today + 3, Long.parseLong(c.getCellContent("B1")));
    }

    @Test
    public void testFormats() {
        SheetController c = new SheetController(driver, testBench(driver),
                getDesiredCapabilities());

        newSheetAndLoadServerFixture("FORMATS");

        Assert.assertEquals("example", c.getCellContent("B2"));
        Assert.assertEquals("example", c.getCellContent("C2"));
        Assert.assertEquals("example", c.getCellContent("D2"));
        Assert.assertEquals("example", c.getCellContent("E2"));
        Assert.assertEquals("example", c.getCellContent("F2"));

        Assert.assertEquals("38247.1226851852", c.getCellContent("B3"));
        Assert.assertEquals("38247.12", c.getCellContent("C3"));
        Assert.assertEquals("3824712.27%", c.getCellContent("D3"));
        Assert.assertEquals("17-Sep-04", c.getCellContent("E3"));
        Assert.assertEquals("3.82E04", c.getCellContent("F3"));

        // These values are taken from LibreOffice
        // Assert.assertEquals("38247.0810185185",
        // sheetController.getCellContent("B3"));
        // Assert.assertEquals("38247.08",
        // sheetController.getCellContent("C3"));
        // Assert.assertEquals("3824708.10%",
        // sheetController.getCellContent("D3"));
        // Assert.assertEquals("17-Sep-04",
        // sheetController.getCellContent("E3"));
        // Assert.assertEquals("3.82E04", sheetController.getCellContent("F3"));

        Assert.assertEquals("3.1415", c.getCellContent("B6"));
        Assert.assertEquals("3.14", c.getCellContent("C6"));
        Assert.assertEquals("314.15%", c.getCellContent("D6"));
        Assert.assertEquals("3-Jan-00", c.getCellContent("E6"));
        Assert.assertEquals("3.14E00", c.getCellContent("F6"));
    }

    @Test
    public void testStringCellType() {
        SheetController c = keyboardSetup();

        c.selectCell("B2");
        c.insertAndRet("example");
        Assert.assertEquals("example", c.getCellContent("B2"));

        c.selectCell("B2");
        c.insertAndRet("12");
        Assert.assertEquals("12", c.getCellContent("B2"));

        c.selectCell("B2");
        c.insertAndRet("example 2");
        Assert.assertEquals("example 2", c.getCellContent("B2")); // TODO -
                                                                  // Fails with
                                                                  // rev 18
    }
}
