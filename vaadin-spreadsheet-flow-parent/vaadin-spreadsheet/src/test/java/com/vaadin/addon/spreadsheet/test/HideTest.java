package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.testbench.parallel.Browser;

public class HideTest extends AbstractSpreadsheetTestCase {

    @Test
    public void testHideColumn() {
        headerPage.createNewSpreadsheet();

        sheetController.selectCell("C2");
        headerPage.loadTestFixture(TestFixtures.ColumnToggle);
        assertCellIsVisible("B2");
        assertCellIsHidden("C2");
        assertCellIsVisible("D2");

        sheetController.selectRegion("B2", "D2");
        headerPage.loadTestFixture(TestFixtures.ColumnToggle);
        assertCellIsHidden("B2");
        assertCellIsVisible("C2");
        assertCellIsHidden("D2");
    }

    @Test
    public void testHideRow() {
        headerPage.createNewSpreadsheet();
        skipBrowser("Fails on phantom JS, B3 is visible after hiding region", Browser.PHANTOMJS);

        sheetController.selectCell("B3");
        headerPage.loadTestFixture(TestFixtures.RowToggle);
        assertCellIsVisible("B2");
        assertCellIsHidden("B3");
        assertCellIsVisible("B4");

        sheetController.selectRegion("B2", "B4");
        headerPage.loadTestFixture(TestFixtures.RowToggle);
        assertCellIsHidden("B2");
        assertCellIsVisible("B3");
        assertCellIsHidden("B4");
    }

    private void assertCellIsHidden(String cell) {
        try {
            assertEquals("Cell " + cell + " should be HIDDEN.", "none",
                    sheetController.getCellStyle(cell, "display"));
        } catch (AssertionError error) {
            assertEquals("Cell " + cell + " should be HIDDEN.", "0px",
                    sheetController.getCellStyle(cell, "height"));
        }
    }

    private void assertCellIsVisible(String cell) {
        assertNotEquals("Cell " + cell + " should be VISIBLE.", "none",
                sheetController.getCellStyle(cell, "display"));
        assertNotEquals("Cell " + cell + " should be VISIBLE.", "0px",
                sheetController.getCellStyle(cell, "height"));
    }
}
