package com.vaadin.addon.spreadsheet.command;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class CellValueCommand extends SpreadsheetCommand {

    class CellValue {
        public final int row;
        public final int col;
        public Object value;

        public CellValue(int row, int col, Object value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }

    class CellRangeValue {
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
     */
    public CellValueCommand(Spreadsheet spreadsheet) {
        super(spreadsheet);
        CellReference selectedCellReference = spreadsheet
                .getSelectedCellReference();
        selectedCellRow = selectedCellReference.getRow();
        selectedcellCol = selectedCellReference.getCol();
        CellRangeAddress paintedCellRange = spreadsheet
                .getCellSelectionManager().getPaintedCellRange();
        if (paintedCellRange.getFirstColumn() != paintedCellRange
                .getLastColumn()
                || paintedCellRange.getFirstRow() != paintedCellRange
                        .getLastRow()) {
            selectedCellRange = new int[] { paintedCellRange.getFirstRow(),
                    paintedCellRange.getLastRow(),
                    paintedCellRange.getFirstColumn(),
                    paintedCellRange.getLastColumn() };
        } else {
            selectedCellRange = null;
        }
    }

    public void clearValues() {
        values.clear();
    }

    public void captureCellValues(CellReference... cellReferences) {
        for (CellReference cr : cellReferences) {
            values.add(new CellValue(cr.getRow(), cr.getCol(), getCellValue(cr)));
        }
    }

    @Override
    public CellReference getSelectedCellReference() {
        return new CellReference(selectedCellRow, selectedcellCol);
    }

    @Override
    public CellRangeAddress getPaintedCellRange() {
        return selectedCellRange == null ? null : new CellRangeAddress(
                selectedCellRange[0], selectedCellRange[1],
                selectedCellRange[2], selectedCellRange[3]);
    }

    public void captureCellRangeValues(CellRangeAddress... cellRanges) {
        for (CellRangeAddress cra : cellRanges) {
            if (cra != null) {
                int h = cra.getLastRow() - cra.getFirstRow() + 1;
                int w = cra.getLastColumn() - cra.getFirstColumn() + 1;
                Object[] v = new Object[h * w];
                int i = 0;
                for (int r = cra.getFirstRow(); r <= cra.getLastRow(); r++) {
                    for (int c = cra.getFirstColumn(); c <= cra.getLastColumn(); c++) {
                        v[i++] = getCellValue(r, c);
                    }
                }
                values.add(new CellRangeValue(cra.getFirstRow(), cra
                        .getLastRow(), cra.getFirstColumn(), cra
                        .getLastColumn(), v));
            }
        }
    }

    @Override
    public void execute() {
        updateValues();
    }

    private void updateValues() {
        for (Object o : values) {
            if (o instanceof CellValue) {
                CellValue cellValue = (CellValue) o;
                cellValue.value = updateCellValue(cellValue.row, cellValue.col,
                        cellValue.value);
            } else {
                CellRangeValue cellRangeValue = (CellRangeValue) o;
                int i = 0;
                for (int r = cellRangeValue.row1; r <= cellRangeValue.row2; r++) {
                    for (int c = cellRangeValue.col1; c <= cellRangeValue.col2; c++) {
                        cellRangeValue.values[i] = updateCellValue(r, c,
                                cellRangeValue.values[i]);
                        i++;
                    }
                }
            }
        }
        if (!spreadsheet.isRealoadingOnThisRoundtrip()) {
            spreadsheet.updateMarkedCells();
        }
    }

    protected Object updateCellValue(int row, int col, Object value) {
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
                if (!spreadsheet.isRealoadingOnThisRoundtrip()) {
                    spreadsheet.markCellAsDeleted(cell, false);
                }
            } else {
                cell.setCellValue((String) null);
                if (!spreadsheet.isRealoadingOnThisRoundtrip()) {
                    spreadsheet.markCellAsUpdated(cell, false);
                }
            }
        } else {
            if (value instanceof String) {
                if (((String) value).startsWith("=")) {
                    cell.setCellFormula(((String) value).substring(1));
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
            if (!spreadsheet.isRealoadingOnThisRoundtrip()) {
                spreadsheet.markCellAsUpdated(cell, false);
            }
        }
        return oldValue;
    }

    protected Object getCellValue(CellReference cell) {
        return getCellValue(cell.getRow(), cell.getCol());
    }

    protected Object getCellValue(int r, int c) {
        return getCellValue(getCell(r, c));
    }

    protected Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        } else {
            switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            case Cell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue();
            case Cell.CELL_TYPE_FORMULA:
                return "=" + cell.getCellFormula();
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                return null;
            }
        }
    }

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

}
