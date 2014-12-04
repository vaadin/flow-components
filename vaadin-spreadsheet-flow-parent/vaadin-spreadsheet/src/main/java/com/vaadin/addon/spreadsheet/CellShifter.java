package com.vaadin.addon.spreadsheet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.client.SpreadsheetState;
import com.vaadin.addon.spreadsheet.command.CellShiftValuesCommand;
import com.vaadin.addon.spreadsheet.command.CellValueCommand;

public class CellShifter implements Serializable {

    private final Spreadsheet spreadsheet;

    public CellShifter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public CellValueManager getCellValueManager() {
        return spreadsheet.getCellValueManager();
    }

    public CellSelectionManager getCellSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }

    /* the actual selected cell hasn't changed */
    public void onSelectionIncreasePainted(int c1, int c2, int r1, int r2) {
        final CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getPaintedCellRange();
        if (paintedCellRange != null) {
            if (spreadsheet.isRangeEditable(paintedCellRange)
                    && spreadsheet.isRangeEditable(c1 - 1, c2 - 1, r1 - 1,
                            r2 - 1)) {
                // store values
                CellValueCommand command = new CellShiftValuesCommand(
                        spreadsheet, false);
                if (c1 != paintedCellRange.getFirstColumn() + 1) {
                    // shift left
                    command.captureCellRangeValues(new CellRangeAddress(r1 - 1,
                            r2 - 1, c1 - 1,
                            paintedCellRange.getFirstColumn() - 1));
                    shiftColumnsLeftInSelection(c1);
                    spreadsheet.updateMarkedCells();
                } else if (c2 != paintedCellRange.getLastColumn() + 1) {
                    // shift right
                    command.captureCellRangeValues(new CellRangeAddress(r1 - 1,
                            r2 - 1, paintedCellRange.getLastColumn() + 1,
                            c2 - 1));
                    shiftColumnsRightInSelection(c2);
                    spreadsheet.updateMarkedCells();
                } else if (r1 != paintedCellRange.getFirstRow() + 1) {
                    // shift top
                    command.captureCellRangeValues(new CellRangeAddress(r1 - 1,
                            paintedCellRange.getFirstRow() - 1, c1 - 1, c2 - 1));
                    shiftRowsUpInSelection(r1);
                    spreadsheet.updateMarkedCells();
                } else if (r2 != paintedCellRange.getLastRow() + 1) {
                    // shift bottom
                    command.captureCellRangeValues(new CellRangeAddress(
                            paintedCellRange.getLastRow() + 1, r2 - 1, c1 - 1,
                            c2 - 1));
                    shiftRowsDownInSelection(r2);
                    spreadsheet.updateMarkedCells();
                }
                CellRangeAddress newPaintedCellRange = spreadsheet
                        .createCorrectCellRangeAddress(c1, c2, r1, r2);
                getCellSelectionManager().handleCellRangeSelection(
                        spreadsheet.getSelectedCellReference(),
                        newPaintedCellRange);
                getCellSelectionManager()
                        .cellRangeSelected(newPaintedCellRange);
                spreadsheet.getSpreadsheetHistoryManager().addCommand(command);

            } else {
                // TODO should show some sort of error, saying that some
                // cells are locked so cannot shift
            }
        }
    }

    /* the actual selected cell hasn't changed */
    public void onSelectionDecreasePainted(int c, int r) {
        final CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getPaintedCellRange();
        if (paintedCellRange != null) {
            if (spreadsheet.isRangeEditable(paintedCellRange)) {
                CellValueCommand command = new CellShiftValuesCommand(
                        spreadsheet, true);
                command.captureCellRangeValues(new CellRangeAddress(r - 1,
                        paintedCellRange.getLastRow(), c - 1, paintedCellRange
                                .getLastColumn()));
                getCellValueManager().removeCells(c,
                        paintedCellRange.getLastColumn() + 1, r,
                        paintedCellRange.getLastRow() + 1, false);
                // removedCells makes sure that removed cells are marked.
                spreadsheet.updateMarkedCells();
                // range selection was updated if NOT all cells were painted
                CellRangeAddress newPaintedCellRange = null;
                if (c != paintedCellRange.getFirstColumn() + 1) {
                    newPaintedCellRange = spreadsheet
                            .createCorrectCellRangeAddress(
                                    paintedCellRange.getFirstColumn() + 1,
                                    c - 1, paintedCellRange.getFirstRow() + 1,
                                    paintedCellRange.getLastRow() + 1);
                } else if (r != paintedCellRange.getFirstRow() + 1) {
                    newPaintedCellRange = spreadsheet
                            .createCorrectCellRangeAddress(
                                    paintedCellRange.getFirstColumn() + 1,
                                    paintedCellRange.getLastColumn() + 1,
                                    paintedCellRange.getFirstRow() + 1, r - 1);
                }
                if (newPaintedCellRange != null) {
                    CellReference selectedCellReference = spreadsheet
                            .getSelectedCellReference();
                    // if the decrease caused the seleced cell to be out of
                    // painted range, move selected cell to first in range
                    if (!SpreadsheetUtil.isCellInRange(selectedCellReference,
                            newPaintedCellRange)) {
                        selectedCellReference = new CellReference(
                                newPaintedCellRange.getFirstRow(),
                                newPaintedCellRange.getFirstColumn());
                    }
                    getCellSelectionManager().handleCellRangeSelection(
                            selectedCellReference, newPaintedCellRange);
                }
                // the selected cell might or might not have changed.. need to
                // call this so user can update possible custom editor value
                CellReference selectedCellReference = getCellSelectionManager()
                        .getSelectedCellReference();
                if ((c - 1) == selectedCellReference.getCol()
                        && (r - 1) == selectedCellReference.getRow()) {
                    spreadsheet.loadCustomEditorOnSelectedCell();
                }
                spreadsheet.getSpreadsheetHistoryManager().addCommand(command);
            } else {
                // TODO should show some sort of error, saying that some
                // cells are locked so cannot shift
            }
        }
    }

    protected void shiftRowsDownInSelection(int newLastRow) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getPaintedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
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
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            getCellValueManager().shiftCellValue(shiftedCell,
                                    newCell, false);
                        } else if (newCell != null) {
                            // update style to 0
                            newCell.setCellStyle(null);
                            spreadsheet.getSpreadsheetStyleFactory()
                                    .cellStyleUpdated(newCell, true);
                            newCell.setCellValue((String) null);
                            getCellValueManager().cellDeleted(newCell);
                        }
                    }
                } else {
                    getCellValueManager().removeCells(c1, c2, newRowIndex,
                            newRowIndex, true);
                }
                newRowIndex += r2 - r1 + 1;
            }
        }
    }

    protected void shiftRowsUpInSelection(int newFirstRow) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getPaintedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
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
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            getCellValueManager().shiftCellValue(shiftedCell,
                                    newCell, false);
                        } else if (newCell != null) {
                            // update style to 0
                            newCell.setCellStyle(null);
                            spreadsheet.getSpreadsheetStyleFactory()
                                    .cellStyleUpdated(newCell, true);
                            newCell.setCellValue((String) null);
                            getCellValueManager().cellDeleted(newCell);
                        }
                    }
                } else {
                    getCellValueManager().removeCells(c1, c2, newRowIndex,
                            newRowIndex, true);
                }
                newRowIndex = newRowIndex - (r2 - r1) - 1;
            }
        }
    }

    protected void shiftColumnsRightInSelection(int newRightMostColumn) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getPaintedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c2 + 1 + (shiftedCellIndex - c1);
                    while (newCellIndex <= newRightMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            getCellValueManager().shiftCellValue(shiftedCell,
                                    newCell, false);

                        } else if (newCell != null) {
                            newCell.setCellValue((String) null);
                            getCellValueManager().cellDeleted(newCell);
                            // update style to 0
                            newCell.setCellStyle(null);
                            spreadsheet.getSpreadsheetStyleFactory()
                                    .cellStyleUpdated(newCell, true);
                        }
                        newCellIndex += (c2 - c1) + 1;
                    }
                }
            }
        }
    }

    protected void shiftColumnsLeftInSelection(int newLeftMostColumn) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getPaintedCellRange();
        int r1 = paintedCellRange.getFirstRow() + 1;
        int r2 = paintedCellRange.getLastRow() + 1;
        int c1 = paintedCellRange.getFirstColumn() + 1;
        int c2 = paintedCellRange.getLastColumn() + 1;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook.getSheetAt(workbook
                .getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c1 - (shiftedCellIndex - c1) - 1;
                    while (newCellIndex >= newLeftMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            getCellValueManager().shiftCellValue(shiftedCell,
                                    newCell, false);
                        } else if (newCell != null) {
                            newCell.setCellValue((String) null);
                            getCellValueManager().cellDeleted(newCell);
                            // update style to 0
                            newCell.setCellStyle(null);
                            spreadsheet.getSpreadsheetStyleFactory()
                                    .cellStyleUpdated(newCell, true);
                        }
                        newCellIndex = newCellIndex - (c2 - c1) - 1;
                    }
                }
            }
        }
    }

    /**
     * See {@link Spreadsheet#shiftRows(int, int, int, boolean, boolean)}
     * 
     * @param startRow
     * @param endRow
     * @param n
     * @param copyRowHeight
     * @param resetOriginalRowHeight
     */
    public void shiftRows(int startRow, int endRow, int n,
            boolean copyRowHeight, boolean resetOriginalRowHeight) {
        Sheet sheet = spreadsheet.getActiveSheet();
        sheet.shiftRows(startRow, endRow, n, copyRowHeight,
                resetOriginalRowHeight);
        // need to resend the cell values to client
        // remove all cached cell data that is now empty
        int start = n < 0 ? endRow + n + 1 : startRow;
        int end = n < 0 ? endRow : startRow + n - 1;
        getCellValueManager().updateDeletedRowsInClientCache(start, end);
        // updateDeletedRowsInClientCache(start + 1, end + 1); this was a bug?
        int firstEffectedRow = n < 0 ? startRow + n : startRow;
        int lastEffectedRow = n < 0 ? endRow : endRow + n;
        SpreadsheetState state = spreadsheet.getState(false);
        if (copyRowHeight || resetOriginalRowHeight) {
            // might need to increase the size of the row heights array
            int oldLength = state.rowH.length;
            int neededLength = endRow + n + 1;
            if (n > 0 && oldLength < neededLength) {
                spreadsheet.getState().rowH = Arrays.copyOf(
                        spreadsheet.getState().rowH, neededLength);
            }
            for (int i = firstEffectedRow; i <= lastEffectedRow; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    if (row.getZeroHeight()) {
                        spreadsheet.getState().rowH[i] = 0f;
                    } else {
                        spreadsheet.getState().rowH[i] = row
                                .getHeightInPoints();
                    }
                } else {
                    spreadsheet.getState().rowH[i] = sheet
                            .getDefaultRowHeightInPoints();
                }
            }
        }
        spreadsheet.updateMergedRegions();
        spreadsheet.triggerImageReload();
        spreadsheet.getCellValueManager().updateVisibleCellValues();
        spreadsheet.updateMarkedCells();
        SpreadsheetStyleFactory styler = spreadsheet
                .getSpreadsheetStyleFactory();
        // need to shift the cell styles, clear and update
        // need to go -1 and +1 because of shifted borders..
        final ArrayList<Cell> cellsToUpdate = new ArrayList<Cell>();
        for (int r = (firstEffectedRow - 1); r <= (lastEffectedRow + 1); r++) {
            if (r < 0) {
                r = 0;
            }
            Row row = sheet.getRow(r);
            final Integer rowIndex = new Integer(r + 1);
            if (row == null) {
                if (state.hiddenRowIndexes.contains(rowIndex)) {
                    spreadsheet.getState().hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < spreadsheet.getState().cols; c++) {
                    styler.clearCellStyle(c, r);
                }
            } else {
                if (row.getZeroHeight()) {
                    spreadsheet.getState().hiddenRowIndexes.add(rowIndex);
                } else if (state.hiddenRowIndexes.contains(rowIndex)) {
                    spreadsheet.getState().hiddenRowIndexes.remove(rowIndex);
                }
                for (int c = 0; c < spreadsheet.getState().cols; c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        styler.clearCellStyle(c, r);
                    } else {
                        cellsToUpdate.add(cell);
                    }
                }
            }
        }
        for (Cell cell : cellsToUpdate) {
            styler.cellStyleUpdated(cell, false);
        }
        styler.loadCustomBorderStylesToState();
        CellReference selectedCellReference = getCellSelectionManager()
                .getSelectedCellReference();
        if (selectedCellReference.getRow() >= firstEffectedRow
                && selectedCellReference.getRow() <= lastEffectedRow) {
            getCellSelectionManager().onSheetAddressChanged(
                    selectedCellReference.formatAsString());
        }
    }
}
