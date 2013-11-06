package com.vaadin.addon.spreadsheet.test;

import org.junit.Assert;
import org.junit.Test;

public class MergeTests extends Test1 {

    @Test
    public void testSelectionBug() {

        c.selectRegion("B2", "C3");
        loadServerFixture("MERGE_CELLS");

        c.selectRegion("C4", "D3");
        loadServerFixture("SELECTION");

        assertCellValue("D4", "SELECTED");
        assertCellValue("B4", "SELECTED");
        assertCellValue("D2", "SELECTED");
    }

    @Test
    public void testBasic() {

        c.selectCell("A1");
        c.insertAndRet("1");
        c.insertAndRet("2");

        c.selectCell("B1");
        c.insertAndRet("=A1+1");
        c.insertAndRet("=A2+1");

        c.selectRegion("A1", "A2");
        loadServerFixture("MERGE_CELLS");

        assertCellValue("B1", "2");
        assertCellValue("B2", "3");

        c.clickElement(c.mergedCell("A1"));
        c.insertAndRet("10");

        assertCellValue("B1", "11");
        assertCellValue("B2", "3");
    }

    @Test
    public void testContents() {

        c.putCellContent("A1", "A1 text");
        c.putCellContent("B1", "B1 text");

        c.selectRegion("A1", "B1");
        loadServerFixture("MERGE_CELLS");

        Assert.assertTrue("A1 text".equals(c.getMergedCellContent("A1")));
    }
}
