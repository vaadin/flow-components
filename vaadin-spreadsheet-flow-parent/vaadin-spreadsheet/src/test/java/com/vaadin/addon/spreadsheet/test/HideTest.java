package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class HideTest extends Test1 {

    @Test
    public void testHideColumn() {
        c.selectCell("C2");
        loadServerFixture("TOGGLE_COLUMNS");
        assertCellIsVisible("B2");
        assertCellIsHidden("C2");
        assertCellIsVisible("D2");

        c.selectCell("B2");
        shift.clickCell("D2");
        loadServerFixture("TOGGLE_COLUMNS");
        assertCellIsHidden("B2");
        assertCellIsVisible("C2");
        assertCellIsHidden("D2");
    }

    @Test
    public void testHideRow() {
        c.selectCell("B3");
        loadServerFixture("TOGGLE_ROWS");
        assertCellIsVisible("B2");
        assertCellIsHidden("B3");
        assertCellIsVisible("B4");

        c.selectCell("B2");
        shift.clickCell("B4");
        loadServerFixture("TOGGLE_ROWS");
        assertCellIsHidden("B2");
        assertCellIsVisible("B3");
        assertCellIsHidden("B4");
    }

    private void assertCellIsHidden(String cell) {
        try {
            assertEquals("Cell " + cell + " should be HIDDEN.", "none",
                    c.getCellStyle(cell, "display"));
        } catch (AssertionError error) {
            assertEquals("Cell " + cell + " should be HIDDEN.", "0px",
                    c.getCellStyle(cell, "height"));
        }
    }

    private void assertCellIsVisible(String cell) {
        assertNotEquals("Cell " + cell + " should be VISIBLE.", "none",
                c.getCellStyle(cell, "display"));
        assertNotEquals("Cell " + cell + " should be VISIBLE.", "0px",
                c.getCellStyle(cell, "height"));
    }
}
