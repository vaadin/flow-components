package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LockIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
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
        putCellContent("C3", "value");
        Assert.assertEquals("value", spreadsheet.getCellAt("C3").getValue());
    }

}
