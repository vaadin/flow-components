package com.vaadin.addon.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2015 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Add-On License 3.0
 * (CVALv3).
 * 
 * See the file license.html distributed with this software for more
 * information about licensing.
 * 
 * You should have received a copy of the CVALv3 along with this program.
 * If not, see <http://vaadin.com/license/cval-3>.
 * #L%
 */

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.command.CellShiftValuesCommand;
import com.vaadin.addon.spreadsheet.command.CellValueCommand;

/**
 * CellSelectionShifter is an utility class for Spreadsheet which handles cell
 * shift events.
 * 
 * Shifting is an Excel term and means the situation where the user has selected
 * one or more cells, and grabs the bottom right hand square of the selected
 * area to extend or curtail the selection and fill the new area with values
 * determined from the existing values.
 * 
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class CellSelectionShifter implements Serializable {

    private static final Logger LOGGER = Logger
            .getLogger(CellSelectionShifter.class.getName());

    private static final String rowShiftRegex = "[$]?[a-zA-Z]+[$]?\\d+";
    private static final Pattern rowShiftPattern = Pattern
            .compile(rowShiftRegex);

    private final Spreadsheet spreadsheet;

    /**
     * Creates a new CellShifter and ties it to the given Spreadsheet
     * 
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public CellSelectionShifter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * This method will be called when the user does a "shift" that increases
     * the amount of selected cells.
     * 
     * @param c1
     *            Index of the starting column, 1-based
     * @param c2
     *            Index of the ending column, 1-based
     * @param r1
     *            Index of the starting row, 1-based
     * @param r2
     *            Index of the ending row, 1-based
     */
    public void onSelectionIncreasePainted(int c1, int c2, int r1, int r2) {
        final CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
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

    /**
     * "Shifts" cell value. Shifting here is an Excel term and means the
     * situation where the user has selected one or more cells, and grabs the
     * bottom right hand square of the selected area to extend or curtail the
     * selection and fill the new area with values determined from the existing
     * values.
     * 
     * @param shiftedCell
     *            Source cell
     * @param newCell
     *            Resulting new cell
     * @param removeShifted
     *            true to remove the source cell at the end
     */
    protected void shiftCellValue(Cell shiftedCell, Cell newCell,
            boolean removeShifted) {
        // clear the new cell first because it might have errors which prevent
        // it from being set to a new type
        if (newCell.getCellType() != Cell.CELL_TYPE_BLANK
                || shiftedCell.getCellType() == Cell.CELL_TYPE_BLANK) {
            newCell.setCellType(Cell.CELL_TYPE_BLANK);
        }
        newCell.setCellType(shiftedCell.getCellType());
        newCell.setCellStyle(shiftedCell.getCellStyle());
        spreadsheet.getSpreadsheetStyleFactory()
                .cellStyleUpdated(newCell, true);
        if (shiftedCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            try {
                if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                    // shift column indexes
                    int collDiff = newCell.getColumnIndex()
                            - shiftedCell.getColumnIndex();
                    Matcher matcher = rowShiftPattern.matcher(shiftedCell
                            .getCellFormula());
                    String originalFormula = shiftedCell.getCellFormula();
                    StringBuilder newFormula = new StringBuilder();
                    int lastEnd = 0;
                    while (matcher.find()) {
                        String s = matcher.group();
                        String replacement;
                        if (!s.startsWith("$")) {
                            String oldIndexString = s.replaceAll(
                                    "[$]{0,1}\\d+", "");

                            int columnIndex = SpreadsheetUtil
                                    .getColHeaderIndex(oldIndexString);
                            columnIndex += collDiff;
                            replacement = s.replace(oldIndexString,
                                    SpreadsheetUtil.getColHeader(columnIndex));
                        } else {
                            // if column has a '$' reference shouldn't change
                            replacement = s;
                        }
                        newFormula.append(originalFormula.substring(lastEnd,
                                matcher.start()));
                        newFormula.append(replacement);
                        lastEnd = matcher.end();
                    }
                    newFormula.append(originalFormula.substring(lastEnd));
                    newCell.setCellFormula(newFormula.toString());
                } else { // shift row indexes
                    int rowDiff = newCell.getRowIndex()
                            - shiftedCell.getRowIndex();
                    Matcher matcher = rowShiftPattern.matcher(shiftedCell
                            .getCellFormula());
                    String originalFormula = shiftedCell.getCellFormula();
                    StringBuilder newFormula = new StringBuilder();
                    int lastEnd = 0;
                    while (matcher.find()) {
                        String s = matcher.group();
                        String rowString = s
                                .replaceAll("[$]{0,1}[a-zA-Z]+", "");
                        String replacement;
                        if (!rowString.startsWith("$")) {
                            int row = Integer.parseInt(rowString);
                            row += rowDiff;
                            replacement = s.replace(rowString,
                                    Integer.toString(row));
                        } else {
                            // if row has a '$' reference shouldn't change
                            replacement = s;
                        }
                        newFormula.append(originalFormula.substring(lastEnd,
                                matcher.start()));
                        newFormula.append(replacement);
                        lastEnd = matcher.end();
                    }
                    newFormula.append(originalFormula.substring(lastEnd));
                    newCell.setCellFormula(newFormula.toString());
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, e.getMessage(), e);
                // TODO visualize shifting error
                newCell.setCellFormula(shiftedCell.getCellFormula());
            }
            spreadsheet.getCellValueManager().getFormulaEvaluator()
                    .notifySetFormula(newCell);
        } else {
            switch (shiftedCell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                newCell.setCellValue(shiftedCell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                newCell.setCellValue(shiftedCell.getErrorCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                newCell.setCellValue(shiftedCell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                newCell.setCellValue(shiftedCell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_BLANK:
                // cell is cleared when type is set
            default:
                break;
            }
            spreadsheet.getCellValueManager().cellUpdated(newCell);
        }
        if (removeShifted) {
            shiftedCell.setCellValue((String) null);
            spreadsheet.getCellValueManager().cellDeleted(shiftedCell);
        }
    }

    /**
     * This method will be called when the user does a "shift" that decreases
     * the amount of selected cells.
     * 
     * @param c
     *            Column index of the new last selected column, 1-based
     * @param r
     *            Row index of the new last selected row, 1-based
     */
    public void onSelectionDecreasePainted(int c, int r) {
        final CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
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

    private void shiftRowsDownInSelection(int newLastRow) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
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
                            shiftCellValue(shiftedCell, newCell, false);
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

    private void shiftRowsUpInSelection(int newFirstRow) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
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
                            shiftCellValue(shiftedCell, newCell, false);
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

    private void shiftColumnsRightInSelection(int newRightMostColumn) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
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
                            shiftCellValue(shiftedCell, newCell, false);

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

    private void shiftColumnsLeftInSelection(int newLeftMostColumn) {
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
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
                            shiftCellValue(shiftedCell, newCell, false);
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

    private CellValueManager getCellValueManager() {
        return spreadsheet.getCellValueManager();
    }

    private CellSelectionManager getCellSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }
}
