/**
 * Copyright 2000-2025 Vaadin Ltd.
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

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class LockIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void testLockedCells() {
        SpreadsheetElement spreadsheet = getSpreadsheet();

        // Lock cell C3
        selectCell("C3");
        loadTestFixture(TestFixtures.LockCell);

        spreadsheet.getCellAt("B2").setValue("value");

        // Toggle locked state of the region
        selectRegion("B2", "D4");
        loadTestFixture(TestFixtures.LockCell);

        // Assert that a locked cell cannot be edited
        Assert.assertEquals("locked", spreadsheet.getCellAt("B2").getValue());
        SheetCellElement lockedCell = spreadsheet.getCellAt("B2");
        selectCell("B2"); // work around Selenium issue with double-click
                          // targeting wrong cell
        lockedCell.doubleClick();
        Assert.assertFalse(spreadsheet.getCellValueInput().isDisplayed());

        // Assert that an unlocked cell can be edited
        Assert.assertEquals("unlocked", spreadsheet.getCellAt("C3").getValue());
        setCellValue("C3", "value");
        Assert.assertEquals("value", spreadsheet.getCellAt("C3").getValue());
    }

}
