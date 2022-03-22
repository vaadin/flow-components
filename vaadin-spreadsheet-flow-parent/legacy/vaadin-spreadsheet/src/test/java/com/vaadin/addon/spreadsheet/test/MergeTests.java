package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.parallel.Browser;

public class MergeTests extends AbstractSpreadsheetTestCase {

    @Test
    public void testSelectionBug() {
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();

        sheetController.selectRegion("B2", "C3");
        headerPage.loadTestFixture(TestFixtures.MergeCells);

        sheetController.selectRegion("C4", "D3");
        headerPage.loadTestFixture(TestFixtures.Selection);

        assertEquals("SELECTED",spreadsheetElement.getCellAt("D4").getValue());
        assertEquals("SELECTED",spreadsheetElement.getCellAt("B4").getValue());
        assertEquals("SELECTED",spreadsheetElement.getCellAt("D2").getValue());
    }

    @Test
    public void testBasic() {
        skipBrowser("insertAndRet() does not work correctly in IE", Browser.IE9, Browser.IE10, Browser.IE11);
        headerPage.createNewSpreadsheet();
        final SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();
        spreadsheetElement.getCellAt("A1").setValue("1");
        spreadsheetElement.getCellAt("A2").setValue("2");


        spreadsheetElement.getCellAt("B1").setValue("=A1+1");
        spreadsheetElement.getCellAt("B2").setValue("=A2+1");


        sheetController.selectRegion("A1", "A2");
        headerPage.loadTestFixture(TestFixtures.MergeCells);
        assertEquals("2",spreadsheetElement.getCellAt("B1").getValue());
        assertEquals("3",spreadsheetElement.getCellAt("B2").getValue());

        final SheetCellElement a1 = spreadsheetElement.getCellAt("A1");
        a1.setValue("10");

        assertEquals("11",spreadsheetElement.getCellAt("B1").getValue());
        assertEquals("3",spreadsheetElement.getCellAt("B2").getValue());
    }

    @Test
    public void testContents() {
        headerPage.createNewSpreadsheet();
        sheetController.selectCell("A2");
        sheetController.putCellContent("A1", "A1 text");
        sheetController.putCellContent("B1", "B1 text");

        sheetController.selectRegion("A1", "B1");
        headerPage.loadTestFixture(TestFixtures.MergeCells);

        Assert.assertTrue("A1 text".equals(sheetController
                .getMergedCellContent("A1")));
    }

    /**
     * Ticket #17601
     */
    @Test
    public void testColumnAlignments() throws IOException {
        headerPage.loadFile("column_alignment_style.xlsx", this);

        compareScreen("column_alignments");
    }
}
