/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.io.Serializable;

import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAutoFilter;

/**
 * Rows shifted on the active sheet, as the two public {@code shiftRows}
 * overloads of {@code Spreadsheet} shift them. POI moves the rows' cells,
 * heights and merged regions; what is kept for the rows besides moves with
 * them, and the rows changed are shown again, as
 * {@link Spreadsheet#rowsChanged} shows them.
 */
final class RowShiftOrchestrator implements Serializable {

    private final Spreadsheet spreadsheet;

    RowShiftOrchestrator(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void shift(int startRow, int endRow, int n, boolean copyRowHeight,
            boolean resetOriginalRowHeight) {
        spreadsheet.getActiveSheet().shiftRows(startRow, endRow, n,
                copyRowHeight, resetOriginalRowHeight);
        // Rows moved past the last row shown are shown, as a load shows
        // them; nothing moved there, the sheet keeps its size.
        int rowsNeeded = spreadsheet.getActiveSheet().getLastRowNum() + 1;
        if (rowsNeeded > spreadsheet.getRows()) {
            spreadsheet.setMaxRows(rowsNeeded);
        }
        // POI moves the cells, heights and merged regions of the rows; their
        // pop-up buttons and invalid formula marks go with them.
        spreadsheet.popupButtons().shiftRows(startRow, endRow, n);
        spreadsheet.invalidFormulas().shiftActiveSheetRows(startRow, endRow, n);
        moveFiltersAndTables(startRow, endRow, n);
        // The rows moved, where they were and where they went.
        spreadsheet.rowsChanged(Math.min(startRow, startRow + n),
                Math.max(endRow, endRow + n));
    }

    /**
     * The filter of the sheet and its tables move with their rows, as the
     * ranges of conditional formatting do: POI moves neither, and the pop-up
     * buttons, moved with their cells, would be over other rows than those
     * filtered. A table shrunk keeps the rows it needs, so that the workbook
     * loads again.
     */
    private void moveFiltersAndTables(int startRow, int endRow, int n) {
        Sheet sheet = spreadsheet.getActiveSheet();
        Workbook workbook = sheet.getWorkbook();
        int sheetIndex = workbook.getSheetIndex(sheet);
        FormulaShifter shifter = FormulaShifter.createForRowShift(sheetIndex,
                sheet.getSheetName(), startRow, endRow, n,
                workbook.getSpreadsheetVersion());
        if (sheet instanceof XSSFSheet xssfSheet) {
            CTAutoFilter filter = xssfSheet.getCTWorksheet().getAutoFilter();
            if (filter != null) {
                filter.setRef(
                        SpreadsheetTable
                                .shifted(shifter, sheetIndex,
                                        CellRangeAddress
                                                .valueOf(filter.getRef()),
                                        SpreadsheetTable.FEWEST_ROWS)
                                .formatAsString());
            }
            for (XSSFTable table : xssfSheet.getTables()) {
                CellRangeAddress moved = SpreadsheetTable.shifted(shifter,
                        sheetIndex,
                        CellRangeAddress.valueOf(table.getCTTable().getRef()),
                        SpreadsheetTable.fewestRows(table));
                table.setArea(new AreaReference(
                        new CellReference(moved.getFirstRow(),
                                moved.getFirstColumn()),
                        new CellReference(moved.getLastRow(),
                                moved.getLastColumn()),
                        workbook.getSpreadsheetVersion()));
            }
        }
        spreadsheet.tables().shiftRows(sheet, shifter, sheetIndex, startRow,
                endRow, n);
    }

    /**
     * The value of {@link #rowAfterShift} for a row whose content is gone.
     */
    static final int OVERWRITTEN = -1;

    /**
     * Where the content of a row is after
     * {@code Sheet.shiftRows(startRow, endRow, n)}. The shifted rows move by
     * {@code n}. The rows they land on that are not shifted themselves are
     * overwritten: POI removes their content. Every other row stays where it
     * is. Rows are 0-based.
     *
     * @return the row the content is on after the shift, or
     *         {@link #OVERWRITTEN}
     */
    static int rowAfterShift(int row, int startRow, int endRow, int n) {
        if (startRow <= row && row <= endRow) {
            return row + n;
        }
        if (startRow + n <= row && row <= endRow + n) {
            return OVERWRITTEN;
        }
        return row;
    }
}
