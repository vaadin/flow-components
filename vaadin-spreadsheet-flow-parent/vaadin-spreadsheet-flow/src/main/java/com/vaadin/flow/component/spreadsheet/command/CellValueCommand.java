package com.vaadin.flow.component.spreadsheet.command;

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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Command for changing the value of one or more cells.
 *
 * @author Vaadin Ltd.
 * @since 1.0
 */
@SuppressWarnings("serial")
public class CellValueCommand extends SpreadsheetCommand
        implements ValueChangeCommand {

    /**
     * Represents the coordinates and value of a single cell.
     */
    class CellValue implements Serializable {
        public final int row;
        public final int col;
        public Object value;

        public CellValue(int row, int col, Object value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }

    /**
     * Represents the coordinates and values of a range of cells.
     */
    class CellRangeValue implements Serializable {
        public final int row1;
        public final int row2;
        public final int col1;
        public final int col2;
        public final Object[] values;

        public CellRangeValue(int row1, int row2, int col1, int col2,
                Object[] values) {
            this.row1 = row1;
            this.row2 = row2;
            this.col1 = col1;
            this.col2 = col2;
            this.values = values;
        }
    }

    protected final List<Object> values = new ArrayList<Object>();
    protected final int selectedCellRow;
    protected final int selectedcellCol;
    protected final int[] selectedCellRange;

    /**
     * Sets the currently selected cell of the spreadsheet as the selected cell
     * and possible painted range for this command.
     *
     * @param spreadsheet
     *            Target spreadsheet
     */
    public CellValueCommand(Spreadsheet spreadsheet) {
        super(spreadsheet);
        CellReference selectedCellReference = spreadsheet
                .getSelectedCellReference();
        selectedCellRow = selectedCellReference.getRow();
        selectedcellCol = selectedCellReference.getCol();
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getSelectedCellRange();
        if (paintedCellRange != null && (paintedCellRange
                .getFirstColumn() != paintedCellRange.getLastColumn()
                || paintedCellRange.getFirstRow() != paintedCellRange
                        .getLastRow())) {
            selectedCellRange = new int[] { paintedCellRange.getFirstRow(),
                    paintedCellRange.getLastRow(),
                    paintedCellRange.getFirstColumn(),
                    paintedCellRange.getLastColumn() };
        } else {
            selectedCellRange = null;
        }
    }

    /**
     * Clears all values captured by this command.
     */
    public void clearValues() {
        values.clear();
    }

    /**
     * Capture values from cells defined in the given CellReference(s).
     *
     * @param cellReferences
     *            cell references to process
     */
    public void captureCellValues(CellReference... cellReferences) {
        for (CellReference cr : cellReferences) {
            values.add(
                    new CellValue(cr.getRow(), cr.getCol(), getCellValue(cr)));
        }
    }

    /**
     * Capture values from cells defined in the given CellRangeAddress(es).
     *
     * @param cellRanges
     *            cell ranges to process
     */
    public void captureCellRangeValues(CellRangeAddress... cellRanges) {
        for (CellRangeAddress cra : cellRanges) {
            if (cra != null) {
                int h = cra.getLastRow() - cra.getFirstRow() + 1;
                int w = cra.getLastColumn() - cra.getFirstColumn() + 1;
                Object[] v = new Object[h * w];
                int i = 0;
                for (int r = cra.getFirstRow(); r <= cra.getLastRow(); r++) {
                    for (int c = cra.getFirstColumn(); c <= cra
                            .getLastColumn(); c++) {
                        v[i++] = getCellValue(r, c);
                    }
                }
                values.add(
                        new CellRangeValue(cra.getFirstRow(), cra.getLastRow(),
                                cra.getFirstColumn(), cra.getLastColumn(), v));
            }
        }
    }

    @Override
    public CellReference getSelectedCellReference() {
        return new CellReference(selectedCellRow, selectedcellCol);
    }

    @Override
    public CellRangeAddress getPaintedCellRange() {
        return selectedCellRange == null ? null
                : new CellRangeAddress(selectedCellRange[0],
                        selectedCellRange[1], selectedCellRange[2],
                        selectedCellRange[3]);
    }

    @Override
    public void execute() {
        updateValues();
    }

    private void updateValues() {
        List<Cell> cellsToUpdate = new ArrayList<Cell>();

        for (Object o : values) {
            if (o instanceof CellValue) {
                CellValue cellValue = (CellValue) o;
                cellValue.value = updateCellValue(cellValue.row, cellValue.col,
                        cellValue.value, cellsToUpdate);
            } else {
                CellRangeValue cellRangeValue = (CellRangeValue) o;
                int i = 0;
                for (int r = cellRangeValue.row1; r <= cellRangeValue.row2; r++) {
                    for (int c = cellRangeValue.col1; c <= cellRangeValue.col2; c++) {
                        cellRangeValue.values[i] = updateCellValue(r, c,
                                cellRangeValue.values[i], cellsToUpdate);
                        i++;
                    }
                }
            }
        }
        if (!spreadsheet.isRerenderPending()) {
            spreadsheet.refreshCells(cellsToUpdate);
        }
    }

    /**
     * Sets the given value to the cell at the given coordinates.
     *
     * @param row
     *            Row index, 0-based
     * @param col
     *            Column index, 0-based
     * @param value
     *            Value to set to the cell
     * @param cellsToUpdate
     *            List of cells that need updating at the end. If the cell value
     *            is modified, the cell is added to this list.
     * @return Previous value of the cell or null if not available
     */
    protected Object updateCellValue(int row, int col, Object value,
            List<Cell> cellsToUpdate) {
        Cell cell = getCell(row, col);
        Object oldValue = getCellValue(cell);
        if (value == null && cell == null) {
            return null; // nothing to do
        }

        if (cell == null && value != null) {
            // create cell
            Row row2 = getSheet().getRow(row);
            if (row2 == null) {
                row2 = getSheet().createRow(row);
            }
            cell = row2.createCell(col);
        }

        if (value == null) { // delete
            if (cell == null || cell.getCellStyle().getIndex() == 0) {
                getSheet().getRow(row).removeCell(cell);
                if (!spreadsheet.isRerenderPending()) {
                    spreadsheet.markCellAsDeleted(cell, false);
                }
            } else {
                cell.setCellValue((String) null);
                if (!spreadsheet.isRerenderPending()) {
                    cellsToUpdate.add(cell);
                }
            }
        } else {
            if (value instanceof String) {
                if (((String) value).startsWith("=")) {
                    try {
                        cell.setCellFormula(((String) value).substring(1));
                    } catch (FormulaParseException fpe) {
                        cell.setCellValue((String) value);
                    }
                } else {
                    cell.setCellValue((String) value);
                }
            } else if (value instanceof Byte) {
                cell.setCellErrorValue((Byte) value);
            } else if (value instanceof Double) {
                cell.setCellValue((Double) value);
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            }
            if (!spreadsheet.isRerenderPending()) {
                cellsToUpdate.add(cell);
            }
        }
        return oldValue;
    }

    /**
     * Returns the current value for the cell referenced by the given cell
     * reference
     *
     * @param cell
     *            Reference to the cell
     * @return Current value of the cell or null if not available
     */
    protected Object getCellValue(CellReference cell) {
        return getCellValue(cell.getRow(), cell.getCol());
    }

    /**
     * Returns the current value of the cell at the given coordinates.
     *
     * @param r
     *            Row index, 0-based
     * @param c
     *            Column index, 0-based
     * @return Current value of the cell or null if not available
     */
    protected Object getCellValue(int r, int c) {
        return getCellValue(getCell(r, c));
    }

    /**
     * Returns the current value of the given Cell
     *
     * @param cell
     *            Target cell
     * @return Current value of the cell or null if not available
     */
    protected Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        } else {
            switch (cell.getCellType()) {
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case ERROR:
                return cell.getErrorCellValue();
            case FORMULA:
                return "=" + cell.getCellFormula();
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                return cell.getStringCellValue();
            default:
                return null;
            }
        }
    }

    /**
     * Returns the Cell at the given row and column.
     *
     * @param r
     *            Row index, 0-based
     * @param c
     *            Column index, 0-based
     * @return Cell at the given coordinates, null if not found
     */
    protected Cell getCell(int r, int c) {
        Row row = getSheet().getRow(r);
        if (row == null) {
            return null;
        } else {
            Cell cell = row.getCell(c);
            if (cell == null) {
                return null;
            } else {
                return cell;
            }
        }
    }

    @Override
    public Set<CellReference> getChangedCells() {
        Set<CellReference> changedCells = new HashSet<CellReference>();
        for (Object o : values) {
            if (o instanceof CellValue) {
                CellValue cellValue = (CellValue) o;
                changedCells
                        .add(new CellReference(cellValue.row, cellValue.col));
            } else {
                CellRangeValue cellRangeValue = (CellRangeValue) o;
                for (int r = cellRangeValue.row1; r <= cellRangeValue.row2; r++) {
                    for (int c = cellRangeValue.col1; c <= cellRangeValue.col2; c++) {
                        changedCells.add(new CellReference(r, c));
                    }
                }
            }
        }
        return changedCells;
    }

}
