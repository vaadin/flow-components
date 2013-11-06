package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;

public class ContextMenuTest extends Test1 {

    @Test
    public void testSingleCell() {
        loadServerFixture("ACTIONS");

        c.selectCell("B2");

        mouse.rightClick(c.getCellElement("B2"));
        contextMenu.clickItem("Number");

        Assert.assertEquals("42", c.getCellContent("B2"));
    }

    @Test
    public void testMultipleCells() {
        loadServerFixture("ACTIONS");

        c.selectCell("B2");
        ctrl.selectCell("C3");
        shift.selectCell("D4");

        mouse.rightClick(c.getCellElement("C3"));
        contextMenu.clickItem("Number");

        c.selectCell("B2");
        ctrl.selectCell("C3");
        mouse.rightClick(c.getCellElement("C3"));
        contextMenu.clickItem("Double cell values");

        Assert.assertEquals("84", c.getCellContent("B2"));
        Assert.assertEquals("84", c.getCellContent("C3"));
        Assert.assertEquals("42", c.getCellContent("C4"));
        Assert.assertEquals("42", c.getCellContent("D3"));
        Assert.assertEquals("42", c.getCellContent("D4"));
    }

    @Test
    public void testHeaders() {
        loadServerFixture("ACTIONS");

        mouse.rightClick(driver.findElement(c.columnToXPath("C")));
        contextMenu.clickItem("Column action");

        Assert.assertEquals("first column", c.getCellContent("C3"));
        Assert.assertEquals("last column", c.getCellContent("C4"));
    }
}
