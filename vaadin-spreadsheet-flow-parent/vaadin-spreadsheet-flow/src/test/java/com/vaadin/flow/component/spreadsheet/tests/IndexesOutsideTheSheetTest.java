/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;

/**
 * Rows and columns the client reports are limited to the ones the sheet has,
 * and to as many as one scroll loads.
 */
class IndexesOutsideTheSheetTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void init() {
        spreadsheet = new Spreadsheet(20, 10);
    }

    private void scroll(int firstRow, int firstColumn, int lastRow,
            int lastColumn) {
        TestHelper.fireClientEvent(spreadsheet, "onSheetScroll",
                "{\"firstRow\": " + firstRow + ", \"firstColumn\": "
                        + firstColumn + ", \"lastRow\": " + lastRow
                        + ", \"lastColumn\": " + lastColumn + "}");
    }

    @Test
    void scrollPastTheSheet_viewportStaysInsideTheSheet() {
        scroll(-5, 0, 5000, 300);

        assertEquals(1, spreadsheet.getFirstRow());
        assertEquals(1, spreadsheet.getFirstColumn());
        assertEquals(20, spreadsheet.getLastRow());
        assertEquals(10, spreadsheet.getLastColumn());
    }

    @Test
    void scrollEndingBeforeItStarts_viewportCoversTheFirstCell() {
        scroll(5, 4, 2, 1);

        assertEquals(5, spreadsheet.getFirstRow());
        assertEquals(5, spreadsheet.getLastRow());
        assertEquals(4, spreadsheet.getFirstColumn());
        assertEquals(4, spreadsheet.getLastColumn());
    }

    @Test
    void scrollPastTheSheet_onlyCellsOfTheSheetAreLoaded() {
        int[] askedRows = { Integer.MAX_VALUE, Integer.MIN_VALUE };
        int[] askedColumns = { Integer.MAX_VALUE, Integer.MIN_VALUE };
        spreadsheet.setSpreadsheetComponentFactory(
                new SpreadsheetComponentFactory() {
                    @Override
                    public Component getCustomComponentForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        askedRows[0] = Math.min(askedRows[0], rowIndex);
                        askedRows[1] = Math.max(askedRows[1], rowIndex);
                        askedColumns[0] = Math.min(askedColumns[0],
                                columnIndex);
                        askedColumns[1] = Math.max(askedColumns[1],
                                columnIndex);
                        return null;
                    }

                    @Override
                    public Component getCustomEditorForCell(Cell cell,
                            int rowIndex, int columnIndex,
                            Spreadsheet spreadsheet, Sheet sheet) {
                        return null;
                    }

                    @Override
                    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                            int columnIndex, Spreadsheet spreadsheet,
                            Sheet sheet, Component customEditor) {
                    }
                });
        askedRows[0] = askedColumns[0] = Integer.MAX_VALUE;
        askedRows[1] = askedColumns[1] = Integer.MIN_VALUE;

        scroll(-5, 0, 5000, 300);

        // The factory is asked with 0-based indexes.
        assertEquals(0, askedRows[0]);
        assertEquals(19, askedRows[1]);
        assertEquals(0, askedColumns[0]);
        assertEquals(9, askedColumns[1]);
    }

    @Test
    void scrollOverMoreRowsThanOneScrollLoads_viewportIsCutDown() {
        spreadsheet = new Spreadsheet(3000, 10);

        scroll(1, 1, 3000, 10);

        assertEquals(2000, spreadsheet.getLastRow());
        assertEquals(10, spreadsheet.getLastColumn());
    }

    @Test
    void hiddenRows_doNotCountTowardsTheRowsOneScrollLoads() {
        spreadsheet = new Spreadsheet(3500, 10);
        for (int row = 0; row < 1000; row++) {
            spreadsheet.setRowHidden(row, true);
        }

        scroll(1, 1, 3500, 10);

        // Rows 1001 to 3000 are the 2,000 shown.
        assertEquals(3000, spreadsheet.getLastRow());
    }

    @Test
    void scrollOverMoreColumnsThanOneScrollLoads_viewportIsCutDown() {
        spreadsheet = new Spreadsheet(20, 1500);

        scroll(1, 1, 20, 1500);

        assertEquals(20, spreadsheet.getLastRow());
        assertEquals(1000, spreadsheet.getLastColumn());
    }

    @Test
    void hiddenColumns_doNotCountTowardsTheColumnsOneScrollLoads() {
        spreadsheet = new Spreadsheet(20, 1600);
        for (int column = 0; column < 500; column++) {
            spreadsheet.setColumnHidden(column, true);
        }

        scroll(1, 1, 20, 1600);

        // Columns 501 to 1500 are the 1,000 shown.
        assertEquals(1500, spreadsheet.getLastColumn());
    }

    @Test
    void rowResizeOutsideTheSheet_isIgnored() {
        TestHelper.fireClientEvent(spreadsheet, "rowsResized",
                "{\"rowIndexes\": [-1, 0, 3, 5000],"
                        + " \"newHeights\": [30, 30, 30, 30]}");

        Sheet sheet = spreadsheet.getActiveSheet();
        assertEquals(30f, sheet.getRow(2).getHeightInPoints());
        assertNull(sheet.getRow(4999));
    }

    @Test
    void columnResizeOutsideTheSheet_isIgnored() {
        TestHelper.fireClientEvent(spreadsheet, "columnResized",
                "{\"colIndexes\": [-1, 0, 3, 300],"
                        + " \"newWidths\": [80, 80, 80, 80]}");

        assertEquals(80f,
                spreadsheet.getActiveSheet().getColumnWidthInPixels(2), 1f);
    }

    @Test
    void resizeWithEveryIndexOutsideTheSheet_addsNoUndoStep() {
        TestHelper.fireClientEvent(spreadsheet, "rowsResized",
                "{\"rowIndexes\": [0, 5000], \"newHeights\": [30, 30]}");
        TestHelper.fireClientEvent(spreadsheet, "columnResized",
                "{\"colIndexes\": [0, 300], \"newWidths\": [80, 80]}");

        assertFalse(spreadsheet.getSpreadsheetHistoryManager().canUndo());
    }
}
