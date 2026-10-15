/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Requests the client does not send while the workbook or the sheet is
 * protected are left alone when they come anyway, as are renames POI would
 * refuse and events missing a text field.
 */
class ClientRequestsAndProtectionTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet();
        spreadsheet.createNewSheet("Notes", 10, 10);
        spreadsheet.setActiveSheetIndex(0);
    }

    private void fire(String type, String json) {
        TestHelper.fireClientEvent(spreadsheet, type, json);
    }

    private String sheetName(int index) {
        return spreadsheet.getWorkbook().getSheetName(index);
    }

    @Test
    void renameToAnotherSheetsNameInAnotherCase_leavesTheName() {
        // POI refused it and failed the request.
        assertDoesNotThrow(() -> fire("sheetRenamed",
                "{\"sheetIndex\": 0, \"newName\": \"NOTES\"}"));

        assertEquals(spreadsheet.getActiveSheet().getSheetName(), sheetName(0));
        assertEquals("Notes", sheetName(1));
    }

    @Test
    void renameInAProtectedWorkbook_leavesTheName() {
        String before = sheetName(0);
        ((XSSFWorkbook) spreadsheet.getWorkbook()).lockStructure();

        fire("sheetRenamed", "{\"sheetIndex\": 0, \"newName\": \"Q4\"}");

        assertEquals(before, sheetName(0));
    }

    @Test
    void rename_isCarriedOut() {
        fire("sheetRenamed", "{\"sheetIndex\": 0, \"newName\": \"Q4\"}");

        assertEquals("Q4", sheetName(0));
    }

    @Test
    void newSheetInAProtectedWorkbook_isNotAdded() {
        ((XSSFWorkbook) spreadsheet.getWorkbook()).lockStructure();

        fire("sheetCreated", "{\"scrollLeft\": 0, \"scrollTop\": 0}");

        assertEquals(2, spreadsheet.getWorkbook().getNumberOfSheets());
    }

    @Test
    void rowResizeOnASheetThatLocksRowFormatting_isIgnored() {
        spreadsheet.setActiveSheetProtected("secret");

        fire("rowsResized", "{\"rowIndexes\": [2], \"newHeights\": [40]}");

        Row row = spreadsheet.getActiveSheet().getRow(1);
        if (row != null) {
            assertNotEquals(40f, row.getHeightInPoints());
        }
    }

    @Test
    void rowResizeOnASheetThatAllowsRowFormatting_isApplied() {
        spreadsheet.setActiveSheetProtected("secret");
        ((XSSFSheet) spreadsheet.getActiveSheet()).lockFormatRows(false);

        fire("rowsResized", "{\"rowIndexes\": [2], \"newHeights\": [40]}");

        assertEquals(40f,
                spreadsheet.getActiveSheet().getRow(1).getHeightInPoints());
    }

    @Test
    void columnResizeOnASheetThatLocksColumnFormatting_isIgnored() {
        int before = spreadsheet.getActiveSheet().getColumnWidth(1);
        spreadsheet.setActiveSheetProtected("secret");

        fire("columnResized", "{\"colIndexes\": [2], \"newWidths\": [120]}");

        assertEquals(before, spreadsheet.getActiveSheet().getColumnWidth(1));
    }

    @Test
    void columnAutofitOnASheetThatLocksColumnFormatting_isIgnored() {
        spreadsheet.createCell(0, 1, "a fairly long piece of text");
        int before = spreadsheet.getActiveSheet().getColumnWidth(1);
        spreadsheet.setActiveSheetProtected("secret");

        fire("onColumnAutofit", "{\"columnIndex\": 2}");

        assertEquals(before, spreadsheet.getActiveSheet().getColumnWidth(1));
    }

    @Test
    void eventsMissingTheirText_doNothing() {
        spreadsheet.createCell(0, 0, "kept");

        assertDoesNotThrow(() -> {
            fire("onPaste", "{}");
            fire("cellValueEdited", "{\"row\": 1, \"col\": 1}");
            fire("updateCellComment", "{\"col\": 1, \"row\": 1}");
            fire("actionOnCurrentSelection", "{}");
            fire("sheetAddressChanged", "{}");
        });

        assertEquals("kept", spreadsheet.getActiveSheet().getRow(0).getCell(0)
                .getStringCellValue());
        assertNull(spreadsheet.getActiveSheet().getRow(0).getCell(0)
                .getCellComment());
    }
}
