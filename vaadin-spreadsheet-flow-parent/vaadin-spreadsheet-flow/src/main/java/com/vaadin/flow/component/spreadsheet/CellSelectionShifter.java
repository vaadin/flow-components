package com.vaadin.flow.component.spreadsheet;

/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.CellValueChangeEvent;
import com.vaadin.flow.component.spreadsheet.command.CellShiftValuesCommand;
import com.vaadin.flow.component.spreadsheet.command.CellValueCommand;

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

    private static final org.slf4j.Logger LOGGER = LoggerFactory
            .getLogger(CellSelectionShifter.class);

    private static final String rowShiftRegex = "[$]?[a-zA-Z]+[$]?\\d+";
    private static final Pattern rowShiftPattern = Pattern
            .compile(rowShiftRegex);
    private static final String stringSequenceRegex = "\\d+$";
    private static final Pattern stringSequencePattern = Pattern
            .compile(stringSequenceRegex);

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
     * @param r1
     *            Index of the starting row, 1-based
     * @param c1
     *            Index of the starting column, 1-based
     * @param r2
     *            Index of the ending row, 1-based
     * @param c2
     *            Index of the ending column, 1-based
     */
    public void onSelectionIncreasePainted(int r1, int c1, int r2, int c2) {
        final CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
        if (paintedCellRange != null) {
            if (spreadsheet.isRangeEditable(paintedCellRange) && spreadsheet
                    .isRangeEditable(r1 - 1, c1 - 1, r2 - 1, c2 - 1)) {
                CellRangeAddress changedCellRangeAddress = null;
                // store values
                CellValueCommand command = new CellShiftValuesCommand(
                        spreadsheet, false);
                if (c1 != paintedCellRange.getFirstColumn() + 1) {
                    // shift left
                    changedCellRangeAddress = new CellRangeAddress(r1 - 1,
                            r2 - 1, c1 - 1,
                            paintedCellRange.getFirstColumn() - 1);
                    command.captureCellRangeValues(changedCellRangeAddress);
                    shiftColumnsLeftInSelection(c1);
                    spreadsheet.updateMarkedCells();
                } else if (c2 != paintedCellRange.getLastColumn() + 1) {
                    // shift right
                    changedCellRangeAddress = new CellRangeAddress(r1 - 1,
                            r2 - 1, paintedCellRange.getLastColumn() + 1,
                            c2 - 1);
                    command.captureCellRangeValues(changedCellRangeAddress);
                    shiftColumnsRightInSelection(c2);
                    spreadsheet.updateMarkedCells();
                } else if (r1 != paintedCellRange.getFirstRow() + 1) {
                    // shift top
                    changedCellRangeAddress = new CellRangeAddress(r1 - 1,
                            paintedCellRange.getFirstRow() - 1, c1 - 1, c2 - 1);
                    command.captureCellRangeValues(changedCellRangeAddress);
                    shiftRowsUpInSelection(r1);
                    spreadsheet.updateMarkedCells();
                } else if (r2 != paintedCellRange.getLastRow() + 1) {
                    // shift bottom
                    changedCellRangeAddress = new CellRangeAddress(
                            paintedCellRange.getLastRow() + 1, r2 - 1, c1 - 1,
                            c2 - 1);
                    command.captureCellRangeValues(changedCellRangeAddress);
                    shiftRowsDownInSelection(r2);
                    spreadsheet.updateMarkedCells();
                }
                CellRangeAddress newPaintedCellRange = spreadsheet
                        .createCorrectCellRangeAddress(r1, c1, r2, c2);
                getCellSelectionManager().handleCellRangeSelection(
                        spreadsheet.getSelectedCellReference(),
                        newPaintedCellRange, false);
                spreadsheet.getSpreadsheetHistoryManager().addCommand(command);

                if (changedCellRangeAddress != null) {
                    fireCellValueChangeEvent(changedCellRangeAddress);
                }

            } else {
                // TODO should show some sort of error, saying that some
                // cells are locked so cannot shift
            }
        }
    }

    private void fireCellValueChangeEvent(CellRangeAddress region) {
        Set<CellReference> cells = new HashSet<CellReference>();
        for (int x = region.getFirstColumn(); x <= region
                .getLastColumn(); x++) {
            for (int y = region.getFirstRow(); y <= region.getLastRow(); y++) {
                cells.add(new CellReference(y, x));
            }
        }
        spreadsheet.fireEvent(new CellValueChangeEvent(spreadsheet, cells));
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
     * @param sequenceIncrement
     *            increment added to shifted cell value
     */
    protected void shiftCellValue(Cell shiftedCell, Cell newCell,
            boolean removeShifted, Double sequenceIncrement) {
        // clear the new cell first because it might have errors which prevent
        // it from being set to a new type
        if (newCell.getCellType() != CellType.BLANK
                || shiftedCell.getCellType() == CellType.BLANK) {
            newCell.setBlank();
        }
        spreadsheet.getSpreadsheetStyleFactory().cellStyleUpdated(newCell,
                true);
        switch (shiftedCell.getCellType()) {
        case FORMULA:
            shiftFormula(shiftedCell, newCell);
            break;
        case BOOLEAN:
            newCell.setCellValue(shiftedCell.getBooleanCellValue());
            break;
        case ERROR:
            newCell.setCellErrorValue(shiftedCell.getErrorCellValue());
            break;
        case NUMERIC:
            shiftNumeric(shiftedCell, newCell, sequenceIncrement);
            break;
        case STRING:
            shiftString(shiftedCell, newCell, sequenceIncrement);
            break;
        case BLANK:
            newCell.setBlank();
        default:
            break;
        }
        spreadsheet.getCellValueManager().cellUpdated(newCell);
        if (removeShifted) {
            shiftedCell.setCellValue((String) null);
            spreadsheet.getCellValueManager().cellDeleted(shiftedCell);
        }
    }

    /**
     * Set's cell value for the newCell. It will be the same as shiftedCell
     * unless sequenceIncrement is not null, in that case the last digits are
     * replaced
     *
     * @param shiftedCell
     *            Source cell
     * @param newCell
     *            Resulting new cell
     * @param sequenceIncrement
     *            not null to increase the number in source cell
     */
    private void shiftString(Cell shiftedCell, Cell newCell,
            Double sequenceIncrement) {
        if (sequenceIncrement != null) {
            int dif;
            if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                // shift column indexes
                dif = newCell.getColumnIndex() - shiftedCell.getColumnIndex();
            } else {
                dif = newCell.getRowIndex() - shiftedCell.getRowIndex();
            }

            Matcher matcher = stringSequencePattern
                    .matcher(shiftedCell.getStringCellValue());
            if (matcher.find()) {
                String base = shiftedCell.getStringCellValue().substring(0,
                        matcher.start());
                String currentValue = matcher.group();
                Double currVal = Double.parseDouble(currentValue);
                newCell.setCellValue(base
                        + (int) Math.abs(currVal + sequenceIncrement * dif));
            } else {
                newCell.setCellValue(shiftedCell.getStringCellValue());
            }
        } else {
            newCell.setCellValue(shiftedCell.getStringCellValue());
        }
    }

    /**
     * Set's cell value for the newCell. It will be the same as shiftedCell
     * unless sequenceIncrement is not null, in that case the value changes
     * depending on sequenceIncrement and cell distance
     *
     * @param shiftedCell
     *            Source cell
     * @param newCell
     *            Resulting new cell
     * @param sequenceIncrement
     *            not null to increase the number in source cell
     */
    private void shiftNumeric(Cell shiftedCell, Cell newCell,
            Double sequenceIncrement) {
        if (sequenceIncrement != null) {
            int dif;
            if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                // shift column indexes
                dif = newCell.getColumnIndex() - shiftedCell.getColumnIndex();
            } else {
                dif = newCell.getRowIndex() - shiftedCell.getRowIndex();
            }
            newCell.setCellValue(shiftedCell.getNumericCellValue()
                    + sequenceIncrement * dif);
        } else {
            newCell.setCellValue(shiftedCell.getNumericCellValue());
        }
    }

    /**
     * Set's cell value for the newCell. It will be the same as shiftedCell with
     * updated references.
     *
     * @param shiftedCell
     *            Source cell
     * @param newCell
     *            Resulting new cell
     */
    private void shiftFormula(Cell shiftedCell, Cell newCell) {
        try {
            if (shiftedCell.getColumnIndex() != newCell.getColumnIndex()) {
                // shift column indexes
                int collDiff = newCell.getColumnIndex()
                        - shiftedCell.getColumnIndex();
                Matcher matcher = rowShiftPattern
                        .matcher(shiftedCell.getCellFormula());
                String originalFormula = shiftedCell.getCellFormula();
                StringBuilder newFormula = new StringBuilder();
                int lastEnd = 0;
                while (matcher.find()) {
                    String s = matcher.group();
                    String replacement;
                    if (!s.startsWith("$")) {
                        String oldIndexString = s.replaceAll("[$]{0,1}\\d+",
                                "");

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
                int rowDiff = newCell.getRowIndex() - shiftedCell.getRowIndex();
                Matcher matcher = rowShiftPattern
                        .matcher(shiftedCell.getCellFormula());
                String originalFormula = shiftedCell.getCellFormula();
                StringBuilder newFormula = new StringBuilder();
                int lastEnd = 0;
                while (matcher.find()) {
                    String s = matcher.group();
                    String rowString = s.replaceAll("[$]{0,1}[a-zA-Z]+", "");
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
            LOGGER.debug(e.getMessage(), e);
            // TODO visualize shifting error
            newCell.setCellFormula(shiftedCell.getCellFormula());
        }
        spreadsheet.getCellValueManager().getFormulaEvaluator()
                .notifySetFormula(newCell);
    }

    /**
     * This method will be called when the user does a "shift" that decreases
     * the amount of selected cells.
     *
     * @param r
     *            Row index of the new last selected row, 1-based
     * @param c
     *            Column index of the new last selected column, 1-based
     */
    public void onSelectionDecreasePainted(int r, int c) {
        final CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
        if (paintedCellRange != null) {
            if (spreadsheet.isRangeEditable(paintedCellRange)) {
                CellValueCommand command = new CellShiftValuesCommand(
                        spreadsheet, true);
                CellRangeAddress changedCellRangeAddress = new CellRangeAddress(
                        r - 1, paintedCellRange.getLastRow(), c - 1,
                        paintedCellRange.getLastColumn());
                command.captureCellRangeValues(changedCellRangeAddress);
                getCellValueManager().removeCells(r, c,
                        paintedCellRange.getLastRow() + 1,
                        paintedCellRange.getLastColumn() + 1, false);
                // removedCells makes sure that removed cells are marked.
                spreadsheet.updateMarkedCells();
                // range selection was updated if NOT all cells were painted
                CellRangeAddress newPaintedCellRange = null;
                if (c != paintedCellRange.getFirstColumn() + 1) {
                    newPaintedCellRange = spreadsheet
                            .createCorrectCellRangeAddress(
                                    paintedCellRange.getFirstRow() + 1,
                                    paintedCellRange.getFirstColumn() + 1,
                                    paintedCellRange.getLastRow() + 1, c - 1);
                } else if (r != paintedCellRange.getFirstRow() + 1) {
                    newPaintedCellRange = spreadsheet
                            .createCorrectCellRangeAddress(
                                    paintedCellRange.getFirstRow() + 1,
                                    paintedCellRange.getFirstColumn() + 1,
                                    r - 1,
                                    paintedCellRange.getLastColumn() + 1);
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
                            selectedCellReference, newPaintedCellRange, false);
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
                fireCellValueChangeEvent(changedCellRangeAddress);
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
                        Double sequenceIncrement = getColumnSequenceIncrement(c,
                                r1, r2);
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false,
                                    sequenceIncrement);
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
                    getCellValueManager().removeCells(newRowIndex, c1,
                            newRowIndex, c2, true);
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
                        Double sequenceIncrement = getColumnSequenceIncrement(c,
                                r1, r2);
                        Cell shiftedCell = shiftedRow.getCell(c - 1);
                        Cell newCell = newRow.getCell(c - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = newRow.createCell(c - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false,
                                    sequenceIncrement);
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
                    getCellValueManager().removeCells(newRowIndex, c1,
                            newRowIndex, c2, true);
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
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                Double sequenceIncrement = getRowSequenceIncrement(rIndex, c1,
                        c2);
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c2 + 1 + (shiftedCellIndex - c1);
                    while (newCellIndex <= newRightMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false,
                                    sequenceIncrement);

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
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        for (int rIndex = r1; rIndex <= r2; rIndex++) {
            final Row row = activeSheet.getRow(rIndex - 1);
            if (row != null) {
                Double sequenceIncrement = getRowSequenceIncrement(rIndex, c1,
                        c2);
                for (int shiftedCellIndex = c1; shiftedCellIndex <= c2; shiftedCellIndex++) {
                    Cell shiftedCell = row.getCell(shiftedCellIndex - 1);
                    int newCellIndex = c1 - (shiftedCellIndex - c1) - 1;
                    while (newCellIndex >= newLeftMostColumn) {
                        Cell newCell = row.getCell(newCellIndex - 1);
                        if (shiftedCell != null) {
                            if (newCell == null) {
                                newCell = row.createCell(newCellIndex - 1);
                            }
                            shiftCellValue(shiftedCell, newCell, false,
                                    sequenceIncrement);
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
     * Returns the increment between all consecutive cells in row with rIndex
     * from column c1 to column c2
     *
     * @param rIndex
     *            Row index for the sequence recognition, 1-based
     * @param c1
     *            First column of the row to be considered, 1-based
     * @param c2
     *            Last column of the row to be considered, 1-based
     * @return common difference or null
     */
    private Double getRowSequenceIncrement(int rIndex, int c1, int c2) {
        Double result = null;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        final Row row = activeSheet.getRow(rIndex - 1);
        if (row != null) {
            Cell firstCell = row.getCell(c1 - 1);
            if (firstCell != null) {
                if (firstCell.getCellType() == CellType.STRING) {
                    return getSequenceIncrement(
                            getRowStringValues(row, c1, c2));
                } else if (firstCell.getCellType() == CellType.NUMERIC) {
                    return getSequenceIncrement(
                            getRowNumericValues(row, c1, c2));
                }
            }
        }
        return result;
    }

    /**
     * Returns an array with String values in column with columnIndex from row
     * r1 to row r2 in activeSheet until first non String cell or null value
     * Used by
     * {@link CellSelectionShifter#getColumnSequenceIncrement(int, int, int)}
     *
     * @param activeSheet
     *            Sheet where the cells are going to be taken from
     * @param columnIndex
     *            Defines the origin of the cell values to be returned, 1-based
     * @param r1
     *            First row of the column to be returned, 1-based
     * @param r2
     *            Last row of the column to be returned, 1-based
     * @return String array with values
     */
    private String[] getColumnStringValues(Sheet activeSheet, int columnIndex,
            int r1, int r2) {
        String[] result = new String[r2 - r1 + 1];
        Cell cell;
        Row row;
        for (int i = r1; i <= r2; i++) {
            row = activeSheet.getRow(i - 1);
            if (row != null) {
                cell = row.getCell(columnIndex - 1);
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    result[i - r1] = cell.getStringCellValue();
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Returns an array with Double values in column with columnIndex from row
     * r1 to row r2 in activeSheet until first non numeric cell or null value
     * Used by
     * {@link CellSelectionShifter#getColumnSequenceIncrement(int, int, int)}
     *
     * @param activeSheet
     *            Sheet where the cells are goint to be taken from
     * @param columnIndex
     *            Defines the origin of the cell values to be returned, 1-based
     * @param r1
     *            First row of the column to be returned, 1-based
     * @param r2
     *            Last row of the column to be returned, 1-based
     * @return Double array with values
     */
    private Double[] getColumnNumericValues(Sheet activeSheet, int columnIndex,
            int r1, int r2) {
        Double[] result = new Double[r2 - r1 + 1];
        Cell cell;
        Row row;
        for (int i = r1; i <= r2; i++) {
            row = activeSheet.getRow(i - 1);
            if (row != null) {
                cell = row.getCell(columnIndex - 1);
                if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                    result[i - r1] = cell.getNumericCellValue();
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Returns an array with String values in row from column c1 to column c2
     * until first non String cell or null value. Used by
     * {@link CellSelectionShifter#getRowSequenceIncrement(int, int, int)}
     *
     * @param row
     *            Row where the cells are going to be taken from
     * @param c1
     *            First column of the row to be returned, 1-based
     * @param c2
     *            Last column of the column to be returned, 1-based
     * @return String array with values
     */
    private String[] getRowStringValues(Row row, int c1, int c2) {
        String[] result = new String[c2 - c1 + 1];
        Cell cell;
        for (int i = c1; i <= c2; i++) {
            cell = row.getCell(i - 1);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                result[i - c1] = cell.getStringCellValue();
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Returns an array with Double values in row from column c1 to column c2
     * until first non Numeric cell or null value. Used by
     * {@link CellSelectionShifter#getRowSequenceIncrement(int, int, int)}
     *
     * @param row
     *            Row where the cells are going to be taken from
     * @param c1
     *            First column of the row to be returned, 1-based
     * @param c2
     *            Last column of the column to be returned, 1-based
     * @return Double array with values
     */
    private Double[] getRowNumericValues(Row row, int c1, int c2) {
        Double[] result = new Double[c2 - c1 + 1];
        Cell shiftedCell;
        for (int i = c1; i <= c2; i++) {
            shiftedCell = row.getCell(i - 1);
            if (shiftedCell != null
                    && shiftedCell.getCellType() == CellType.NUMERIC) {
                result[i - c1] = shiftedCell.getNumericCellValue();
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Returns the increment between all consecutive elements of values
     * parameter or null if there isn't
     *
     * @param values
     *            Double values to be considered for the sequence recognition
     * @return common difference or null
     */
    private Double getSequenceIncrement(Double[] values) {
        Double result = null;
        for (int i = 1; i < values.length; i++) {
            if (values[i] != null && values[i - 1] != null) {
                Double diff = values[i] - values[i - 1];
                if (result == null) {
                    result = diff;
                } else if (!result.equals(diff)) {
                    return null;
                }
            } else {
                return null;
            }
        }
        return result;
    }

    /**
     * Returns the increment between all consecutive elements of values
     * parameter or null if there isn't. Also checks that all elements have the
     * same constant String before the digits
     *
     * @param values
     *            String values to be considered for the sequence recognition
     * @return common difference or null
     */
    private Double getSequenceIncrement(String[] values) {
        Double result = null;
        String previousConstant = null;
        Double previousValue = null;
        Matcher matcher = stringSequencePattern.matcher(values[0]);
        if (matcher.find()) {
            previousConstant = values[0].substring(0, matcher.start());
            previousValue = Double.parseDouble(matcher.group());
        } else {
            return null;
        }
        if (values.length > 1) {
            for (int i = 1; i < values.length; i++) {
                String currentValue = values[i];
                if (currentValue != null
                        && currentValue.startsWith(previousConstant)) {
                    matcher = stringSequencePattern.matcher(currentValue);
                    if (matcher.find()) {
                        String constant = currentValue.substring(0,
                                matcher.start());
                        if (previousConstant.equals(constant)) {
                            Double value = Double.parseDouble(matcher.group());
                            Double diff = value - previousValue;
                            if (result == null) {
                                result = diff;
                            } else if (!result.equals(diff)) {
                                return null;
                            }
                            previousValue = value;
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        } else {
            return 1d;
        }
        return result;
    }

    /**
     * Returns the increment between all consecutive cells in column with cIndex
     * from row r1 to row r2
     *
     * @param cIndex
     *            Column index for the sequence recognition, 1-based
     *
     * @param r1
     *            First row of the column to be considered, 1-based
     * @param r2
     *            Last row of the column to be considered, 1-based
     * @return common difference or null
     */
    private Double getColumnSequenceIncrement(int cIndex, int r1, int r2) {
        Double result = null;
        Workbook workbook = spreadsheet.getWorkbook();
        final Sheet activeSheet = workbook
                .getSheetAt(workbook.getActiveSheetIndex());
        final Row row = activeSheet.getRow(r1 - 1);
        if (row != null) {
            Cell firstCell = row.getCell(cIndex - 1);
            if (firstCell != null) {
                if (firstCell.getCellType() == CellType.STRING) {
                    return getSequenceIncrement(
                            getColumnStringValues(activeSheet, cIndex, r1, r2));
                } else if (firstCell.getCellType() == CellType.NUMERIC) {
                    return getSequenceIncrement(getColumnNumericValues(
                            activeSheet, cIndex, r1, r2));
                }
            }
        }
        return result;
    }

    private CellValueManager getCellValueManager() {
        return spreadsheet.getCellValueManager();
    }

    private CellSelectionManager getCellSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }
}
