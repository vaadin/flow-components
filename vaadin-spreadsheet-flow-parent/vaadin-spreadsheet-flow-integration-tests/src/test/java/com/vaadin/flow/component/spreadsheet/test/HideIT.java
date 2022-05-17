package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import org.junit.Before;
import org.junit.Test;

public class HideIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        createNewSpreadsheet();
    }

    @Test
    public void testHideColumn() {
        selectCell("C2");
        loadTestFixture(TestFixtures.ColumnToggle);
        assertCellIsVisible("B2");
        assertCellIsHidden("C2");
        assertCellIsVisible("D2");

        selectRegion("B2", "D2");
        loadTestFixture(TestFixtures.ColumnToggle);
        assertCellIsHidden("B2");
        assertCellIsVisible("C2");
        assertCellIsHidden("D2");
    }

    @Test
    public void testHideRow() {
        selectCell("B3");
        loadTestFixture(TestFixtures.RowToggle);
        assertCellIsVisible("B2");
        assertCellIsHidden("B3");
        assertCellIsVisible("B4");

        selectRegion("B2", "B4");
        loadTestFixture(TestFixtures.RowToggle);
        assertCellIsHidden("B2");
        assertCellIsVisible("B3");
        assertCellIsHidden("B4");
    }

    private void assertCellIsHidden(String cell) {
        assertEquals("Cell " + cell + " should be HIDDEN.", "none",
                getCellStyle(cell, "display"));
    }

    private void assertCellIsVisible(String cell) {
        assertNotEquals("Cell " + cell + " should be VISIBLE.", "none",
                getCellStyle(cell, "display"));
        assertNotEquals("Cell " + cell + " should be VISIBLE.", "0px",
                getCellStyle(cell, "height"));
    }
}