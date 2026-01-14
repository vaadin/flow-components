/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class HideIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
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

    /**
     * Test that hiding rows programmatically loads rows from below the fold.
     *
     * When a sheet has many rows (150) but only the first visible ones are
     * loaded (~55), hiding most of those visible rows should trigger loading of
     * rows below the fold to fill the viewport.
     */
    @Test
    public void hideRows_shouldLoadRowsBelowFold() {
        // Load the fixture which creates 150 rows and adds an action to hide
        // rows 10-99
        loadTestFixture(TestFixtures.HideSecondRow);

        // Verify some initial rows are visible
        assertCellIsVisible("B3");
        assertCellIsVisible("B10");

        // Trigger the hide action via context menu
        var spreadsheet = getSpreadsheet();
        spreadsheet.getCellAt("B10").contextClick();
        spreadsheet.getContextMenu().getItem("Hide rows 10-99").click();

        // Wait for the update to complete
        waitUntil(driver -> {
            try {
                // Row 10 should now be hidden
                return "none".equals(getCellStyle("B10", "display"));
            } catch (Exception e) {
                return false;
            }
        }, 5);

        // Rows 10-99 should be hidden
        assertCellIsHidden("B10");
        assertCellIsHidden("B50");

        // Row 100+ should now be visible (loaded from below the fold)
        // This is the key assertion - these rows were not loaded initially
        // but should be loaded after hiding the visible rows
        assertCellIsVisible("B100");
        assertCellIsVisible("B110");

        // The cell content should be present (row number was set as value)
        Assert.assertEquals("100", getCellContent("B100"));
        Assert.assertEquals("110", getCellContent("B110"));
    }

    private void assertCellIsHidden(String cell) {
        Assert.assertEquals("Cell " + cell + " should be HIDDEN.", "none",
                getCellStyle(cell, "display"));
    }

    private void assertCellIsVisible(String cell) {
        Assert.assertNotEquals("Cell " + cell + " should be VISIBLE.", "none",
                getCellStyle(cell, "display"));
        Assert.assertNotEquals("Cell " + cell + " should be VISIBLE.", "0px",
                getCellStyle(cell, "height"));
    }
}
