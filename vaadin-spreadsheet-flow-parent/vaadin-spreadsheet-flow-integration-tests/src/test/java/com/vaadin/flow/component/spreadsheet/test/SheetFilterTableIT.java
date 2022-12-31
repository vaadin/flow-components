package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.component.spreadsheet.testbench.SpreadsheetElement;
import com.vaadin.flow.component.spreadsheet.tests.fixtures.TestFixtures;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

@TestPath("vaadin-spreadsheet")
public class SheetFilterTableIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
    }

    @Test
    public void filter_changeSheet_hideFilter() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        loadTestFixture(TestFixtures.SpreadsheetTable);
        spreadsheet.addSheet();
        spreadsheet.selectSheetAt(1);
        assertFalse("Cell B2 should not have a filter",
                spreadsheet.getCellAt(2, 2).hasPopupButton());
    }

    @Test
    public void filter_removeTable_hideFilter() {
        final SpreadsheetElement spreadsheet = getSpreadsheet();
        loadTestFixture(TestFixtures.SpreadsheetTable);
        final SheetCellElement cell = spreadsheet.getCellAt("B2");
        cell.contextClick();
        spreadsheet.getContextMenu().getItem("Delete Table B2:F6").click();
        waitUntil(arg0 -> !cell.hasPopupButton());
    }

    @Test
    public void sheetWithFilterTable_rowIsRemoved_filterOptionsAvailable() {
        loadTestFixture(TestFixtures.SpreadsheetTable);
        final var cell = getSpreadsheet().getCellAt("B2");

        assertSelectAll(cell);

        contextClickOnRowHeader(4);
        clickItem("Delete row 4");

        assertSelectAll(cell);
    }

    private void assertSelectAll(SheetCellElement cell) {
        cell.popupButtonClick();
        Assert.assertTrue(hasOption("(Select All)"));
    }
}
