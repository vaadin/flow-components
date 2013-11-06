package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;

public class LockTest extends Test1 {

    @Test
    public void testLockedCells() {
        c.putCellContent("B2", "value");

        c.selectCell("C3");
        shift.clickCell("D4");

        loadServerFixture("LOCK_SELECTED_CELLS");

        Assert.assertEquals("value", c.getCellContent("B2"));
        c.putCellContent("B2", "new value on locked cell");
        Assert.assertEquals("value", c.getCellContent("B2"));

        Assert.assertEquals("unlocked", c.getCellContent("C3"));
        c.putCellContent("C3", "value");
        Assert.assertEquals("value", c.getCellContent("C3"));
    }
}
