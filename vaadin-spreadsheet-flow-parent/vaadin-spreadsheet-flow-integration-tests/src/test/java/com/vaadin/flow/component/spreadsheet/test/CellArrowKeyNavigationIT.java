package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;

/**
 * Arrow key navigation tests.
 */
public class CellArrowKeyNavigationIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        createNewSpreadsheet();
    }

    @Test
    public void shouldNotChangeCellWhenEditingAndArrowRightKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.F2).build().perform(); // edit
                                                                      // mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_RIGHT).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldNotChangeCellWhenEditingAndArrowLeftKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.F2).build().perform(); // edit
                                                                      // mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_LEFT).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldNotChangeCellWhenEditingAndArrowUpKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.F2).build().perform(); // edit
                                                                      // mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_UP).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldNotChangeCellWhenEditingAndArrowDownKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.F2).build().perform(); // edit
                                                                      // mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_DOWN).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldNotChangeCellWhenDoubleClickEditingAndArrowRightKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("A1");
        getSpreadsheet().getCellAt("B2").doubleClick(); // edit mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_RIGHT).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldNotChangeCellWhenDoubleClickEditingAndArrowLeftKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("A1");
        getSpreadsheet().getCellAt("B2").doubleClick(); // edit mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_LEFT).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldNotChangeCellWhenDoubleClickEditingAndArrowDownKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("A1");
        getSpreadsheet().getCellAt("B2").doubleClick(); // edit mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_DOWN).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldNotChangeCellWhenDoubleClickEditingAndArrowUpKeyIsPressed() {
        final SheetCellElement b2 = getSpreadsheet().getCellAt("B2");
        b2.setValue("123");

        selectCell("A1");
        getSpreadsheet().getCellAt("B2").doubleClick(); // edit mode
        new Actions(getDriver()).sendKeys(Keys.ARROW_UP).build().perform();
        new Actions(getDriver()).sendKeys(Keys.ESCAPE).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B2").isCellSelected());
    }

    @Test
    public void shouldSelectCellToTheRightWhenSingleClickAndArrowRightKeyIsPressed() {
        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.NUMPAD1).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD2).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD3).build().perform();

        new Actions(getDriver()).sendKeys(Keys.ARROW_RIGHT).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("C2").isCellSelected());
    }

    @Test
    public void shouldSelectCellToTheLeftWhenSingleClickAndArrowLeftKeyIsPressed() {
        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.NUMPAD1).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD2).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD3).build().perform();

        new Actions(getDriver()).sendKeys(Keys.ARROW_LEFT).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("A2").isCellSelected());
    }

    @Test
    public void shouldSelectCellToTheTopWhenSingleClickAndArrowUpKeyIsPressed() {
        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.NUMPAD1).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD2).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD3).build().perform();

        new Actions(getDriver()).sendKeys(Keys.ARROW_UP).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B1").isCellSelected());
    }

    @Test
    public void shouldSelectCellToTheBottomWhenSingleClickAndArrowDownKeyIsPressed() {
        selectCell("B2");
        new Actions(getDriver()).sendKeys(Keys.NUMPAD1).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD2).build().perform();
        new Actions(getDriver()).sendKeys(Keys.NUMPAD3).build().perform();

        new Actions(getDriver()).sendKeys(Keys.ARROW_DOWN).build().perform();

        Assert.assertTrue(getSpreadsheet().getCellAt("B3").isCellSelected());
    }
}
