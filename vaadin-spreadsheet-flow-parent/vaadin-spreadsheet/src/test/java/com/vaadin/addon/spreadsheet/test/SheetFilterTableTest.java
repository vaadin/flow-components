package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.fixtures.TestFixtures;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

/**
 * Test for sheet filter table.
 *
 */
public class SheetFilterTableTest extends AbstractSpreadsheetTestCase {

    SpreadsheetPage spreadsheetPage;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        spreadsheetPage = headerPage.createNewSpreadsheet();
    }

    @Test
    public void filter_changeSheet_hideFilter() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        headerPage.loadTestFixture(TestFixtures.SpreadsheetTable);
        spreadsheet.addSheet("2");
        spreadsheet.selectSheetAt(1);
        assertFalse("Cell B2 should not have a filter",
                spreadsheet.getCellAt(2, 2).hasPopupButton());
    }

    @Test
    public void filter_removeTable_hideFilter() {
        SpreadsheetElement spreadsheet = $(SpreadsheetElement.class).first();
        headerPage.loadTestFixture(TestFixtures.SpreadsheetTable);
        SheetCellElement cell = spreadsheet.getCellAt("B2");
        cell.contextClick();
        spreadsheet.getContextMenu().getItem("Delete Table B2:F6").click();
        assertFalse("Cell B2 should not have a filter", cell.hasPopupButton());
    }
}