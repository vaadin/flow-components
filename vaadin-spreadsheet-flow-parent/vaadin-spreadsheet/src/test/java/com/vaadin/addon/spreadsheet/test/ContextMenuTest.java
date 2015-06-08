package com.vaadin.addon.spreadsheet.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.annotations.BrowserConfiguration;

public class ContextMenuTest extends Test1 {

    @BrowserConfiguration
    public List<DesiredCapabilities> getBrowsersToTest() {
        // PhantomJS doesn't support right-click
        return getBrowsersExcludingPhantomJS();
    }

    @Test
    public void testSingleCell() {
        loadServerFixture("ACTIONS");

        SheetCellElement b2 = $(SpreadsheetElement.class).first().getCellAt(
                "B2");
        b2.click();

        b2.contextClick();
        contextMenu.clickItem("Number");

        Assert.assertEquals("42", b2.getValue());
    }

    @Test
    @Ignore("Fails with Firefox")
    public void testMultipleCells() {
        loadServerFixture("ACTIONS");

        sheetController.selectCell("B2");
        ctrl.selectCell("C3");
        shift.selectCell("D4");

        $(SpreadsheetElement.class).first().getCellAt("C3").contextClick();
        contextMenu.clickItem("Number");

        sheetController.selectCell("A1");

        sheetController.selectCell("B2");
        SheetCellElement c3 = $(SpreadsheetElement.class).first().getCellAt(
                "C3");
        new Actions(getDriver()).keyDown(Keys.CONTROL).click(c3)
                .keyUp(Keys.CONTROL).contextClick(c3).build().perform();
        contextMenu.clickItem("Double cell values");

        sheetController.selectCell("A1");

        Assert.assertEquals("84", sheetController.getCellContent("B2"));
        Assert.assertEquals("84", sheetController.getCellContent("C3"));
        Assert.assertEquals("42", sheetController.getCellContent("C4"));
        Assert.assertEquals("42", sheetController.getCellContent("D3"));
        Assert.assertEquals("42", sheetController.getCellContent("D4"));
    }

    @Test
    @Ignore("Fails with Firefox")
    public void testHeaders() throws InterruptedException {
        loadServerFixture("ACTIONS");

        $(SpreadsheetElement.class).first().getColumnHeader(3).contextClick();
        contextMenu.clickItem("Column action");

        Assert.assertEquals("first column",
                sheetController.getCellContent("C3"));
        Assert.assertEquals("last column", sheetController.getCellContent("C4"));
    }
}
