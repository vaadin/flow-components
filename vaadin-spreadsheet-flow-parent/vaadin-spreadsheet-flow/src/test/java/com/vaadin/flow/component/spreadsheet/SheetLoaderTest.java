/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * What {@link SheetLoader} does as another workbook is shown, or a sheet added:
 * the last workbook's undo history goes, the client is told it is another
 * workbook, and a sheet added has the size asked for.
 */
class SheetLoaderTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        spreadsheet = new Spreadsheet();
        ui.add(spreadsheet);
    }

    private static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();
        return workbook;
    }

    private boolean toggle() {
        return spreadsheet.getElement().getProperty("workbookChangeToggle",
                false);
    }

    @Test
    void anotherWorkbookShown_hasNoUndoHistory() {
        spreadsheet.setSelection("A1");
        spreadsheet.getCellValueManager().onCellValueChange(1, 1, "typed");
        assertTrue(spreadsheet.getSpreadsheetHistoryManager().canUndo());

        spreadsheet.setWorkbook(workbook());

        assertFalse(spreadsheet.getSpreadsheetHistoryManager().canUndo());
    }

    @Test
    void eachWorkbookShown_flipsTheToggleTheClientMeasuresStylesAgainFor() {
        boolean first = toggle();

        spreadsheet.setWorkbook(workbook());
        boolean second = toggle();
        spreadsheet.setWorkbook(workbook());

        assertNotEquals(first, second);
        assertEquals(first, toggle());
    }

    @Test
    void aSheetAdded_hasTheRowsAndColumnsAskedFor_pastTheDefaultOnes() {
        // A sheet is shown with the default rows and columns at least, as
        // on main: more are what the size asked for gives.
        spreadsheet.createNewSheet("Third", 300, 60);

        assertEquals("Third", spreadsheet.getActiveSheet().getSheetName());
        assertEquals(300, spreadsheet.getRows());
        assertEquals(60, spreadsheet.getColumns());
        assertEquals(SpreadsheetFactory.DEFAULT_COL_WIDTH_UNITS,
                spreadsheet.getActiveSheet().getDefaultColumnWidth());
    }
}
