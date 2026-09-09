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
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

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
 * @since 23.1
 */
public class CellSelectionShifter implements Serializable {

    private final Spreadsheet spreadsheet;
    private final CellValueShifter cellValueShifter;
    private final DirectionalShiftOperator directionalShiftOperator;

    /**
     * Creates a new CellShifter and ties it to the given Spreadsheet
     *
     * @param spreadsheet
     *            Target Spreadsheet
     */
    public CellSelectionShifter(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        this.cellValueShifter = new CellValueShifter(spreadsheet);
        this.directionalShiftOperator = new DirectionalShiftOperator(
                spreadsheet, cellValueShifter);
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
                    directionalShiftOperator.shiftColumnsLeft(c1);
                    spreadsheet.updateMarkedCells();
                } else if (c2 != paintedCellRange.getLastColumn() + 1) {
                    // shift right
                    changedCellRangeAddress = new CellRangeAddress(r1 - 1,
                            r2 - 1, paintedCellRange.getLastColumn() + 1,
                            c2 - 1);
                    command.captureCellRangeValues(changedCellRangeAddress);
                    directionalShiftOperator.shiftColumnsRight(c2);
                    spreadsheet.updateMarkedCells();
                } else if (r1 != paintedCellRange.getFirstRow() + 1) {
                    // shift top
                    changedCellRangeAddress = new CellRangeAddress(r1 - 1,
                            paintedCellRange.getFirstRow() - 1, c1 - 1, c2 - 1);
                    command.captureCellRangeValues(changedCellRangeAddress);
                    directionalShiftOperator.shiftRowsUp(r1);
                    spreadsheet.updateMarkedCells();
                } else if (r2 != paintedCellRange.getLastRow() + 1) {
                    // shift bottom
                    changedCellRangeAddress = new CellRangeAddress(
                            paintedCellRange.getLastRow() + 1, r2 - 1, c1 - 1,
                            c2 - 1);
                    command.captureCellRangeValues(changedCellRangeAddress);
                    directionalShiftOperator.shiftRowsDown(r2);
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
                cells.add(new CellReference(
                        spreadsheet.getActiveSheet().getSheetName(), y, x,
                        false, false));
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
        cellValueShifter.shift(shiftedCell, newCell, removeShifted,
                sequenceIncrement);
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
                    // if the decrease caused the selected cell to be out of
                    // painted range, move selected cell to first in range
                    if (!SpreadsheetUtil.isCellInRange(selectedCellReference,
                            newPaintedCellRange)) {
                        selectedCellReference = new CellReference(
                                spreadsheet.getActiveSheet().getSheetName(),
                                newPaintedCellRange.getFirstRow(),
                                newPaintedCellRange.getFirstColumn(), false,
                                false);
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

    private CellValueManager getCellValueManager() {
        return spreadsheet.getCellValueManager();
    }

    private CellSelectionManager getCellSelectionManager() {
        return spreadsheet.getCellSelectionManager();
    }
}
