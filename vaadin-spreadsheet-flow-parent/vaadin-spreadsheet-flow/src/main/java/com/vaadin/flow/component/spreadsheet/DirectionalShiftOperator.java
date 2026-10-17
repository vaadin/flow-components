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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Fills from the painted cell range in one of four directions - down, up,
 * right, left - as dragging its corner does: each cell of the range is
 * repeated, or counted on as {@link RangeSequenceDetector} finds, into the
 * cells it lines up with, and a cell of the range that is empty empties them.
 * <p>
 * Rows and columns are 1-based here; POI calls subtract one. Up and left, the
 * range is repeated mirrored, the cell nearest the range filled from the cell
 * at its edge, as it always has been.
 */
final class DirectionalShiftOperator implements Serializable {

    private final Spreadsheet spreadsheet;
    private final CellValueShifter cellValueShifter;

    DirectionalShiftOperator(Spreadsheet spreadsheet,
            CellValueShifter cellValueShifter) {
        this.spreadsheet = spreadsheet;
        this.cellValueShifter = cellValueShifter;
    }

    void shiftRowsDown(int newLastRow) {
        fillRows(1, newLastRow);
    }

    void shiftRowsUp(int newFirstRow) {
        fillRows(-1, newFirstRow);
    }

    void shiftColumnsRight(int newRightMostColumn) {
        fillColumns(1, newRightMostColumn);
    }

    void shiftColumnsLeft(int newLeftMostColumn) {
        fillColumns(-1, newLeftMostColumn);
    }

    /**
     * Fills the rows below the range (direction 1) down to the last row, or
     * above it (direction -1) up to the first row, column by column.
     */
    private void fillRows(int direction, int lastRowFilled) {
        CellRangeAddress range = spreadsheet.getCellSelectionManager()
                .getSelectedCellRange();
        int r1 = range.getFirstRow() + 1;
        int r2 = range.getLastRow() + 1;
        int c1 = range.getFirstColumn() + 1;
        int c2 = range.getLastColumn() + 1;
        Sheet sheet = spreadsheet.getActiveSheet();
        Double[] increments = new Double[c2 - c1 + 1];
        for (int c = c1; c <= c2; c++) {
            increments[c - c1] = RangeSequenceDetector.getColumnIncrement(sheet,
                    c, r1, r2);
        }
        for (int source = r1; source <= r2; source++) {
            Row sourceRow = sheet.getRow(source - 1);
            int target = direction > 0 ? r2 + 1 + (source - r1)
                    : r1 - 1 - (source - r1);
            for (; direction > 0 ? target <= lastRowFilled
                    : target >= lastRowFilled; target += direction
                            * (r2 - r1 + 1)) {
                if (sourceRow == null) {
                    spreadsheet.getCellValueManager().removeCells(target, c1,
                            target, c2, true);
                    continue;
                }
                Row targetRow = sheet.getRow(target - 1);
                if (targetRow == null) {
                    targetRow = sheet.createRow(target - 1);
                }
                for (int c = c1; c <= c2; c++) {
                    fill(sourceRow.getCell(c - 1), targetRow, c - 1,
                            increments[c - c1]);
                }
            }
        }
    }

    /**
     * Fills the columns right of the range (direction 1) to the last column, or
     * left of it (direction -1) to the first column, row by row. A row with no
     * cells has none to fill from, nor any filled.
     */
    private void fillColumns(int direction, int lastColumnFilled) {
        CellRangeAddress range = spreadsheet.getCellSelectionManager()
                .getSelectedCellRange();
        int r1 = range.getFirstRow() + 1;
        int r2 = range.getLastRow() + 1;
        int c1 = range.getFirstColumn() + 1;
        int c2 = range.getLastColumn() + 1;
        Sheet sheet = spreadsheet.getActiveSheet();
        for (int r = r1; r <= r2; r++) {
            Row row = sheet.getRow(r - 1);
            if (row == null) {
                continue;
            }
            Double increment = RangeSequenceDetector.getRowIncrement(sheet, r,
                    c1, c2);
            for (int source = c1; source <= c2; source++) {
                int target = direction > 0 ? c2 + 1 + (source - c1)
                        : c1 - 1 - (source - c1);
                for (; direction > 0 ? target <= lastColumnFilled
                        : target >= lastColumnFilled; target += direction
                                * (c2 - c1 + 1)) {
                    fill(row.getCell(source - 1), row, target - 1, increment);
                }
            }
        }
    }

    /**
     * Fills the cell of the row at the 0-based column from the source cell, or
     * empties it, style and all, when there is no source cell.
     */
    private void fill(Cell source, Row targetRow, int column,
            Double increment) {
        Cell target = targetRow.getCell(column);
        if (source != null) {
            if (target == null) {
                target = targetRow.createCell(column);
            }
            cellValueShifter.shift(source, target, false, increment);
        } else if (target != null) {
            target.setCellStyle(null);
            spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(target,
                    true);
            target.setCellValue((String) null);
            spreadsheet.getCellValueManager().cellDeleted(target);
        }
    }
}
