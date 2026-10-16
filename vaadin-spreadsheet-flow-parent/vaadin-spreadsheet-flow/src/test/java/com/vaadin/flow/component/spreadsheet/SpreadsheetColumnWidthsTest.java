/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Columns given a width: the workbook holds the width the client is given, so
 * the column is as wide when the workbook is shown again. POI reads a width
 * back as a fraction of a pixel, and for one width in about forty that was cut
 * to the pixel below.
 */
class SpreadsheetColumnWidthsTest {

    @Test
    void everyWidth_isAsWideWhenAnXlsxWorkbookIsShownAgain() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet();

        assertEveryWidthAsWideWhenShownAgain(workbook);
    }

    @Test
    void everyWidth_isAsWideWhenAnXlsWorkbookIsShownAgain() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet();

        assertEveryWidthAsWideWhenShownAgain(workbook);
    }

    /**
     * HSSF reads a width of just the default width of the sheet as a column of
     * the default width, which is drawn wider. Eight characters is the default
     * of POI; on a sheet of ten, one width falls on the default width, and on
     * one of twenty, one falls a unit short of it.
     */
    @ParameterizedTest
    @ValueSource(ints = { 8, 10, 20 })
    void everyWidth_isReadBackFromAnXlsSheet_whateverItsDefaultWidth(
            int defaultWidth) {
        HSSFSheet sheet = new HSSFWorkbook().createSheet();
        sheet.setDefaultColumnWidth(defaultWidth);

        for (int pixels = 1; pixels <= SpreadsheetUtil.MAX_COLUMN_WIDTH_PX; pixels++) {
            sheet.setColumnWidth(0,
                    SpreadsheetUtil.columnWidthUnits(sheet, pixels));

            assertEquals(pixels, (int) sheet.getColumnWidthInPixels(0));
        }
    }

    private static void assertEveryWidthAsWideWhenShownAgain(
            Workbook workbook) {
        // As many columns at a time as an XLS sheet has, with a cell in the
        // last one, so the sheet is loaded that wide.
        int columns = SpreadsheetVersion.EXCEL97.getMaxColumns();
        workbook.getSheetAt(0).createRow(0).createCell(columns - 1);
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        int widest = SpreadsheetUtil.MAX_COLUMN_WIDTH_PX;
        for (int first = 1; first <= widest; first += columns) {
            int[] given = new int[columns];
            for (int column = 0; column < columns; column++) {
                given[column] = Math.min(first + column, widest);
                spreadsheet.setColumnWidth(column, given[column]);
            }

            assertArrayEquals(given, spreadsheet.getColW());
            assertArrayEquals(given, new Spreadsheet(workbook).getColW(),
                    "shown again");
        }
    }
}
