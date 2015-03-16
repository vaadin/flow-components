package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;

public class LockTest extends Test1 {

    @Test
    public void testLockedCells() {
        sheetController.putCellContent("B2", "value");

        sheetController.selectCell("C3");
        shift.clickCell("D4");

        loadServerFixture("LOCK_SELECTED_CELLS");

        Assert.assertEquals("value", sheetController.getCellContent("B2"));
        sheetController.putCellContent("B2", "new value on locked cell");
        Assert.assertEquals("value", sheetController.getCellContent("B2"));

        Assert.assertEquals("unlocked", sheetController.getCellContent("C3"));
        sheetController.putCellContent("C3", "value");
        Assert.assertEquals("value", sheetController.getCellContent("C3"));
    }
}
