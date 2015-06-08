package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Ignore;
import org.junit.Test;

public class HideTest extends Test1 {

    @Test
    @Ignore("Fails in IE10, Firefox 24, Phantom")
    public void testHideColumn() {
        sheetController.selectCell("C2");
        loadServerFixture("TOGGLE_COLUMNS");
        assertCellIsVisible("B2");
        assertCellIsHidden("C2");
        assertCellIsVisible("D2");

        sheetController.selectCell("B2");
        shift.clickCell("D2");
        loadServerFixture("TOGGLE_COLUMNS");
        assertCellIsHidden("B2");
        assertCellIsVisible("C2");
        assertCellIsHidden("D2");
    }

    @Test
    @Ignore("Fails in Firefox 24, Phantom")
    public void testHideRow() {
        sheetController.selectCell("B3");
        loadServerFixture("TOGGLE_ROWS");
        assertCellIsVisible("B2");
        assertCellIsHidden("B3");
        assertCellIsVisible("B4");

        sheetController.selectCell("B2");
        shift.clickCell("B4");
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
