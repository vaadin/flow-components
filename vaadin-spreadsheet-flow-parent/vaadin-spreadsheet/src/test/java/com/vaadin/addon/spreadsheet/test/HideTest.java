package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.vaadin.testbench.parallel.Browser;
import org.junit.Ignore;
import org.junit.Test;

public class HideTest extends Test1 {

    @Test
    public void testHideColumn() {
        sheetController.selectCell("C2");
        loadServerFixture("TOGGLE_COLUMNS");
        assertCellIsVisible("B2");
        assertCellIsHidden("C2");
        assertCellIsVisible("D2");

        sheetController.selectRegion("B2", "D2");
        loadServerFixture("TOGGLE_COLUMNS");
        assertCellIsHidden("B2");
        assertCellIsVisible("C2");
        assertCellIsHidden("D2");
    }

    @Test
    public void testHideRow() {
        skipBrowser("Fails on phantom JS, B3 is visible after hiding region", Browser.PHANTOMJS);

        sheetController.selectCell("B3");
        loadServerFixture("TOGGLE_ROWS");
        assertCellIsVisible("B2");
        assertCellIsHidden("B3");
        assertCellIsVisible("B4");

        sheetController.selectRegion("B2", "B4");
        loadServerFixture("TOGGLE_ROWS");
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
