package com.vaadin.addon.spreadsheet.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.parallel.Browser;

public class MergeTests extends Test1 {

    @Test
    public void testSelectionBug() {

        sheetController.selectRegion("B2", "C3");
        loadServerFixture("MERGE_CELLS");

        sheetController.selectRegion("C4", "D3");
        loadServerFixture("SELECTION");

        assertCellValue("D4", "SELECTED");
        assertCellValue("B4", "SELECTED");
        assertCellValue("D2", "SELECTED");
    }

    @Test
    public void testBasic() {
        skipBrowser("insertAndRet() does not work correctly in IE", Browser.IE9, Browser.IE10, Browser.IE11);
        final SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();
        spreadsheetElement.getCellAt("A1").setValue("1");
        spreadsheetElement.getCellAt("A2").setValue("2");


        spreadsheetElement.getCellAt("B1").setValue("=A1+1");
        spreadsheetElement.getCellAt("B2").setValue("=A2+1");


        sheetController.selectRegion("A1", "A2");
        loadServerFixture("MERGE_CELLS");

        assertCellValue("B1", "2");
        assertCellValue("B2", "3");

        final SheetCellElement a1 = spreadsheetElement.getCellAt("A1");
        a1.setValue("10");

        assertCellValue("B1", "11");
        assertCellValue("B2", "3");
    }

    @Test
    public void testContents() {
        sheetController.selectCell("A2");
        sheetController.putCellContent("A1", "A1 text");
        sheetController.putCellContent("B1", "B1 text");

        sheetController.selectRegion("A1", "B1");
        loadServerFixture("MERGE_CELLS");

        Assert.assertTrue("A1 text".equals(sheetController
                .getMergedCellContent("A1")));
    }

    /**
     * Ticket #17601
     */
    @Test
    public void testColumnAlignments() throws IOException {
        loadSheetFile("column_alignment_style.xlsx");

        compareScreen("column_alignments");
    }
}
