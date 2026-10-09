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
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Iterates a painted cell range and projects its contents into the four
 * possible "shift" directions — down, up, right, left — by repeatedly invoking
 * {@link CellValueShifter} on each (source, destination) pair.
 *
 * <p>
 * The four directional methods are structurally identical: walk the source
 * range, compute the row- or column-axis sequence increment via
 * {@link RangeSequenceDetector}, then for each cell in the source range fill
 * the destination range cells that align with it. Cells in the destination that
 * have no source counterpart get cleared (style reset + value cleared +
 * {@code cellDeleted} notification).
 *
 * <p>
 * Indexing convention: all method parameters and internal coordinates are
 * <strong>1-based</strong>; POI calls subtract one. Holds the active sheet's
 * range via {@link Spreadsheet#getCellSelectionManager}, mutates cells through
 * the active workbook, and uses {@link Spreadsheet#getCellValueManager} for
 * change notifications and style refresh.
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
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        for (int shiftedRowIndex = r1; shiftedRowIndex <= r2; shiftedRowIndex++) {
            final Row shiftedRow = activeSheet.getRow(shiftedRowIndex - 1);
            int newRowIndex = r2 + 1 + (shiftedRowIndex - r1);
            while (newRowIndex <= newLastRow) {
                if (shiftedRow != null) {
                    Row newRow = activeSheet.getRow(newRowIndex - 1);
                    if (newRow == null) {
                        newRow = activeSheet.createRow(newRowIndex - 1);
                    }
                    for (int c = c1; c <= c2; c++) {
                        Double sequenceIncrement = RangeSequenceDetector
                                .getColumnIncrement(activeSheet, c, r1, r2);
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            cellValueShifter.shift(shiftedCell, newCell, false,
                                    sequenceIncrement);
                        } else if (newCell != null) {
                            clearCell(newCell);
                        }
                    }
                } else {
                    spreadsheet.getCellValueManager().removeCells(newRowIndex,
                            c1, newRowIndex, c2, true);
                }
                newRowIndex += r2 - r1 + 1;
            }
        }
    }

    void shiftRowsUp(int newFirstRow) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        for (int shiftedRowIndex = r1; shiftedRowIndex <= r2; shiftedRowIndex++) {
            final Row shiftedRow = activeSheet.getRow(shiftedRowIndex - 1);
            int newRowIndex = r1 - 1 - (shiftedRowIndex - r1);
            while (newRowIndex >= newFirstRow) {
                if (shiftedRow != null) {
                    Row newRow = activeSheet.getRow(newRowIndex - 1);
                    if (newRow == null) {
                        newRow = activeSheet.createRow(newRowIndex - 1);
                    }
                    for (int c = c1; c <= c2; c++) {
                        Double sequenceIncrement = RangeSequenceDetector
                                .getColumnIncrement(activeSheet, c, r1, r2);
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            cellValueShifter.shift(shiftedCell, newCell, false,
                                    sequenceIncrement);
                        } else if (newCell != null) {
                            clearCell(newCell);
                        }
                    }
                } else {
                    spreadsheet.getCellValueManager().removeCells(newRowIndex,
                            c1, newRowIndex, c2, true);
                }
                newRowIndex = newRowIndex - (r2 - r1) - 1;
            }
        }
    }

    void shiftColumnsRight(int newRightMostColumn) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                Double sequenceIncrement = RangeSequenceDetector
                        .getRowIncrement(activeSheet, rIndex, c1, c2);
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c2 + 1 + (shiftedCellIndex - c1);
                    while (newCellIndex <= newRightMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            cellValueShifter.shift(shiftedCell, newCell, false,
                                    sequenceIncrement);
                        } else if (newCell != null) {
                            clearCellReversed(newCell);
                        }
                        newCellIndex += (c2 - c1) + 1;
                    }
                }
            }
        }
    }

    void shiftColumnsLeft(int newLeftMostColumn) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                Double sequenceIncrement = RangeSequenceDetector
                        .getRowIncrement(activeSheet, rIndex, c1, c2);
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c1 - (shiftedCellIndex - c1) - 1;
                    while (newCellIndex >= newLeftMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            cellValueShifter.shift(shiftedCell, newCell, false,
                                    sequenceIncrement);
                        } else if (newCell != null) {
                            clearCellReversed(newCell);
                        }
                        newCellIndex = newCellIndex - (c2 - c1) - 1;
                    }
                }
            }
        }
    }

    private void clearCell(Cell newCell) {
        // Row variants: reset style first, then clear value, then notify.
        newCell.setCellStyle(null);
        spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(newCell,
                true);
        newCell.setCellValue((String) null);
        spreadsheet.getCellValueManager().cellDeleted(newCell);
    }

    private void clearCellReversed(Cell newCell) {
        // Column variants: clear value first, then reset style, then notify.
        // The order differs from the row variant above, as it always has, and
        // may matter for style-cache invalidation.
        newCell.setCellValue((String) null);
        spreadsheet.getCellValueManager().cellDeleted(newCell);
        newCell.setCellStyle(null);
        spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(newCell,
                true);
    }
}
