package com.vaadin.addon.spreadsheet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.cf.CellRangeUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.client.MergedRegion;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil;

/**
 * Class that handles details of which cells are selected.
 * 
 */
public class CellSelectionManager {

    protected final Spreadsheet spreadsheet;

    protected CellReference selectedCellReference;
    protected CellRangeAddress paintedCellRange;
    protected SelectionChangeEvent latestSelectionEvent;

    protected final ArrayList<CellRangeAddress> cellRangeAddresses = new ArrayList<CellRangeAddress>();
    protected final ArrayList<CellReference> individualSelectedCells = new ArrayList<CellReference>();

    public CellSelectionManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public void clear() {
        selectedCellReference = null;
        paintedCellRange = null;
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        latestSelectionEvent = null;
    }

    public CellReference getSelectedCellReference() {
        return selectedCellReference;
    }

    public CellRangeAddress getPaintedCellRange() {
        return paintedCellRange;
    }

    public List<CellReference> getIndividualSelectedCells() {
        return individualSelectedCells;
    }

    public List<CellRangeAddress> getCellRangeAddresses() {
        return cellRangeAddresses;
    }

    public SelectionChangeEvent getLatestSelectionEvent() {
        return latestSelectionEvent;
    }

    public boolean isCellInsideSelection(int column, int row) {
        CellReference cellReference = new CellReference(row - 1, column - 1);
        boolean inside = cellReference.equals(selectedCellReference)
                || individualSelectedCells.contains(cellReference);
        if (!inside) {
            for (CellRangeAddress cra : cellRangeAddresses) {
                if (cra.isInRange(row - 1, column - 1)) {
                    inside = true;
                    break;
                }
            }
        }
        return inside;
    }

    /**
     * Reloads the current selection, but does not take non-coherent selection
     * into account - discards multiple cell ranges and individually selected
     * cells.
     */
    protected void reloadCurrentSelection() {
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        if (paintedCellRange != null) {
            if (selectedCellReference != null) {
                if (paintedCellRange.isInRange(selectedCellReference.getRow(),
                        selectedCellReference.getCol())) {
                    handleCellRangeSelection(selectedCellReference,
                            paintedCellRange);
                } else {
                    paintedCellRange = null;
                    handleCellAddressChange(selectedCellReference.getCol() + 1,
                            selectedCellReference.getRow() + 1);
                }
            } else {
                handleCellRangeSelection(
                        new CellReference(paintedCellRange.getFirstRow(),
                                paintedCellRange.getFirstColumn()),
                        paintedCellRange);
            }
        } else if (selectedCellReference != null) {
            handleCellAddressChange(selectedCellReference.getCol() + 1,
                    selectedCellReference.getRow() + 1);
        } else {
            handleCellAddressChange(1, 1);
        }
    }

    /* respond to client with the selected cells formula if any */
    protected void onCellSelected(int column, int row,
            boolean discardOldRangeSelection) {
        CellReference cellReference = new CellReference(row - 1, column - 1);
        CellReference previousCellReference = selectedCellReference;
        if (!cellReference.equals(previousCellReference)
                || discardOldRangeSelection
                && (!cellRangeAddresses.isEmpty() || !individualSelectedCells
                        .isEmpty())) {
            handleCellSelection(column, row);
            selectedCellReference = cellReference;
            spreadsheet.loadCustomEditorOnSelectedCell();
            if (discardOldRangeSelection) {
                cellRangeAddresses.clear();
                individualSelectedCells.clear();
                paintedCellRange = spreadsheet.createCorrectCellRangeAddress(
                        column, column, row, row);
            }
            fireNewSelectionChangeEvent();
        }
    }

    /*
     * Cell selected from address field -> need to update sheet selection & &
     * function field
     */
    protected void onSheetAddressChanged(String value) {
        try {
            if (value.contains(":")) {
                CellRangeAddress cra = spreadsheet
                        .createCorrectCellRangeAddress(value);
                // need to check the range for merged regions
                MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                        spreadsheet.getMergedRegionContainer(),
                        cra.getFirstRow() + 1, cra.getLastRow() + 1,
                        cra.getFirstColumn() + 1, cra.getLastColumn() + 1);
                if (region != null) {
                    cra = new CellRangeAddress(region.row1 - 1,
                            region.row2 - 1, region.col1 - 1, region.col2 - 1);
                }
                handleCellRangeSelection(cra);
                selectedCellReference = new CellReference(cra.getFirstRow(),
                        cra.getFirstColumn());
                paintedCellRange = cra;
                cellRangeAddresses.clear();
                cellRangeAddresses.add(cra);
            } else {
                final CellReference cellReference = new CellReference(value);
                MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                        spreadsheet.getMergedRegionContainer(),
                        cellReference.getRow() + 1, cellReference.getRow() + 1,
                        cellReference.getCol() + 1, cellReference.getCol() + 1);
                if (region != null
                        && (region.col1 != region.col2 || region.row1 != region.row2)) {
                    CellRangeAddress cra = spreadsheet
                            .createCorrectCellRangeAddress(region.col1,
                                    region.col2, region.row1, region.row2);
                    handleCellRangeSelection(cra);
                    selectedCellReference = new CellReference(
                            cra.getFirstRow(), cra.getFirstColumn());
                    paintedCellRange = cra;
                    cellRangeAddresses.clear();
                    cellRangeAddresses.add(cra);
                } else {
                    handleCellAddressChange(cellReference.getCol() + 1,
                            cellReference.getRow() + 1);
                    paintedCellRange = spreadsheet
                            .createCorrectCellRangeAddress(
                                    cellReference.getCol() + 1,
                                    cellReference.getCol() + 1,
                                    cellReference.getRow() + 1,
                                    cellReference.getRow() + 1);
                    selectedCellReference = cellReference;
                    cellRangeAddresses.clear();
                }
            }
            individualSelectedCells.clear();
            spreadsheet.loadCustomEditorOnSelectedCell();
            fireNewSelectionChangeEvent();
        } catch (Exception e) {
            spreadsheet.getSpreadsheetRpcProxy().invalidCellAddress();
        }
    }

    /**
     * Reports the correct cell selection value (formula/data) and selection.
     * This method is called when the cell selection has changed via the address
     * field.
     * 
     * @param columnIndex
     *            1-based
     * @param rowIndex
     *            1-based
     */
    protected void handleCellAddressChange(int colIndex, int rowIndex) {
        if (rowIndex >= spreadsheet.getState().rows) {
            rowIndex = spreadsheet.getState().rows;
        }
        if (colIndex >= spreadsheet.getState().cols) {
            colIndex = spreadsheet.getState().cols;
        }
        MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                spreadsheet.getMergedRegionContainer(), rowIndex, rowIndex,
                colIndex, colIndex);
        if (region.col1 != region.col2 || region.row1 != region.row2) {
            handleCellRangeSelection(new CellRangeAddress(region.row1 - 1,
                    region.row2 - 1, region.col1 - 1, region.col2 - 1));
        } else {
            rowIndex = region.row1;
            colIndex = region.col1;
            Workbook workbook = spreadsheet.getWorkbook();
            final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                    .getRow(rowIndex - 1);
            if (row != null) {
                final Cell cell = row.getCell(colIndex - 1);
                if (cell != null) {
                    String value = "";
                    boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                    if (!spreadsheet.isCellHidden(cell)) {
                        if (formula) {
                            value = cell.getCellFormula();
                        } else {
                            value = spreadsheet.getCellValue(cell);
                        }
                    }
                    spreadsheet.getSpreadsheetRpcProxy().showSelectedCell(
                            colIndex, rowIndex, value, formula,
                            spreadsheet.isCellLocked(cell));
                } else {
                    spreadsheet.getSpreadsheetRpcProxy().showSelectedCell(
                            colIndex, rowIndex, "", false,
                            spreadsheet.isCellLocked(cell));
                }
            } else {
                spreadsheet.getSpreadsheetRpcProxy().showSelectedCell(colIndex,
                        rowIndex, "", false,
                        spreadsheet.isActiveSheetProtected());
            }
        }
    }

    protected void reSelectSelectedCell() {
        if (selectedCellReference != null) {
            handleCellSelection(selectedCellReference);
        }
    }

    protected void handleCellSelection(CellReference cellReference) {
        handleCellSelection(cellReference.getCol() + 1,
                cellReference.getRow() + 1);
    }

    /**
     * Reports the selected cell formula value, if any. This method is called
     * when the cell value has changed via sheet cell selection change.
     * 
     * This method can also be used when the selected cell has NOT changed but
     * the value it displays on the formula field might have changed and needs
     * to be updated.
     * 
     * @param columnIndex
     *            1-based
     * @param rowIndex
     *            1-based
     */
    protected void handleCellSelection(int columnIndex, int rowIndex) {
        Workbook workbook = spreadsheet.getWorkbook();
        final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                .getRow(rowIndex - 1);
        if (row != null) {
            final Cell cell = row.getCell(columnIndex - 1);
            if (cell != null) {
                String value = "";
                boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                if (!spreadsheet.isCellHidden(cell)) {
                    if (formula) {
                        value = cell.getCellFormula();
                    } else {
                        value = spreadsheet.getCellValue(cell);
                    }
                }
                spreadsheet.getSpreadsheetRpcProxy().showCellValue(value,
                        columnIndex, rowIndex, formula,
                        spreadsheet.isCellLocked(cell));
            } else {
                spreadsheet.getSpreadsheetRpcProxy().showCellValue("",
                        columnIndex, rowIndex, false,
                        spreadsheet.isCellLocked(cell));
            }
        } else {
            spreadsheet.getSpreadsheetRpcProxy().showCellValue("", columnIndex,
                    rowIndex, false, spreadsheet.isActiveSheetProtected());
        }
    }

    /**
     * handles the new cell range that was given in the address field, returns
     * the range and new selected cell formula/value (if any)
     */
    protected void handleCellRangeSelection(CellRangeAddress cra) {
        int row1 = cra.getFirstRow();
        int row2 = cra.getLastRow();
        int col1 = cra.getFirstColumn();
        int col2 = cra.getLastColumn();
        Workbook workbook = spreadsheet.getWorkbook();
        final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                .getRow(row1);
        if (row != null) {
            final Cell cell = row.getCell(col1);
            if (cell != null) {
                String value = "";
                boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                if (!spreadsheet.isCellHidden(cell)) {
                    if (formula) {
                        value = cell.getCellFormula();
                    } else {
                        value = spreadsheet.getCellValue(cell);
                    }
                }
                spreadsheet.getSpreadsheetRpcProxy().showSelectedCellRange(
                        col1 + 1, col2 + 1, row1 + 1, row2 + 1, value, formula,
                        spreadsheet.isCellLocked(cell));
            } else {
                spreadsheet.getSpreadsheetRpcProxy().showSelectedCellRange(
                        col1 + 1, col2 + 1, row1 + 1, row2 + 1, "", false,
                        spreadsheet.isCellLocked(cell));
            }
        } else {
            spreadsheet.getSpreadsheetRpcProxy().showSelectedCellRange(
                    col1 + 1, col2 + 1, row1 + 1, row2 + 1, "", false,
                    spreadsheet.isActiveSheetProtected());
        }
    }

    protected void handleCellRangeSelection(CellReference cr,
            CellRangeAddress cra) {
        int row1 = cra.getFirstRow();
        int row2 = cra.getLastRow();
        int col1 = cra.getFirstColumn();
        int col2 = cra.getLastColumn();
        Workbook workbook = spreadsheet.getWorkbook();
        final Row row = workbook.getSheetAt(workbook.getActiveSheetIndex())
                .getRow(cr.getRow());
        if (row != null) {
            final Cell cell = row.getCell(cr.getCol());
            if (cell != null) {
                String value = "";
                boolean formula = cell.getCellType() == Cell.CELL_TYPE_FORMULA;
                if (!spreadsheet.isCellHidden(cell)) {
                    if (formula) {
                        value = cell.getCellFormula();
                    } else {
                        value = spreadsheet.getCellValue(cell);
                    }
                }
                spreadsheet.getSpreadsheetRpcProxy().setSelectedCellAndRange(
                        cr.getCol() + 1, cr.getRow() + 1, col1 + 1, col2 + 1,
                        row1 + 1, row2 + 1, value, formula,
                        spreadsheet.isCellLocked(cell));
            } else {
                spreadsheet.getSpreadsheetRpcProxy().setSelectedCellAndRange(
                        cr.getCol() + 1, cr.getRow() + 1, col1 + 1, col2 + 1,
                        row1 + 1, row2 + 1, "", false,
                        spreadsheet.isCellLocked(cell));
            }
        } else {
            spreadsheet.getSpreadsheetRpcProxy().setSelectedCellAndRange(
                    cr.getCol() + 1, cr.getRow() + 1, col1 + 1, col2 + 1,
                    row1 + 1, row2 + 1, "", false,
                    spreadsheet.isActiveSheetProtected());
        }
        selectedCellReference = cr;
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        paintedCellRange = cra;
        if (col1 != col2 || row1 != row2) {
            cellRangeAddresses.add(cra);
        }
        fireNewSelectionChangeEvent();
    }

    protected void cellRangeSelected(CellRangeAddress cra) {
        onCellRangeSelected(cra.getFirstColumn() + 1, cra.getLastColumn() + 1,
                cra.getFirstRow() + 1, cra.getLastRow() + 1);
    }

    protected void onCellRangeSelected(int col1, int col2, int row1, int row2) {
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(col1,
                col2, row1, row2);
        paintedCellRange = cra;
        if (col1 != col2 || row1 != row2) {
            cellRangeAddresses.add(cra);
        }
        fireNewSelectionChangeEvent();
    }

    protected void onCellRangePainted(int selectedCellColumn,
            int selectedCellRow, int col1, int col2, int row1, int row2) {
        cellRangeAddresses.clear();
        individualSelectedCells.clear();

        selectedCellReference = new CellReference(selectedCellRow - 1,
                selectedCellColumn - 1);

        handleCellSelection(selectedCellColumn, selectedCellRow);

        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(col1,
                col2, row1, row2);
        paintedCellRange = cra;
        cellRangeAddresses.add(cra);

        fireNewSelectionChangeEvent();
    }

    protected void onCellAddToSelectionAndSelected(int column, int row) {
        boolean oldSelectedCellInRange = false;
        for (CellRangeAddress range : cellRangeAddresses) {
            if (range.isInRange(selectedCellReference.getRow(),
                    selectedCellReference.getCol())) {
                oldSelectedCellInRange = true;
                break;
            }
        }
        boolean oldSelectedCellInIndividual = false;
        for (CellReference cell : individualSelectedCells) {
            if (cell.equals(selectedCellReference)) {
                // it shouldn't be there yet(!)
                oldSelectedCellInIndividual = true;
                break;
            }
        }
        if (!oldSelectedCellInRange && !oldSelectedCellInIndividual) {
            individualSelectedCells.add(selectedCellReference);
        }
        handleCellSelection(column, row);
        selectedCellReference = new CellReference(row - 1, column - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        if (individualSelectedCells.contains(selectedCellReference)) {
            individualSelectedCells.remove(individualSelectedCells
                    .indexOf(selectedCellReference));
        }
        paintedCellRange = null;
        fireNewSelectionChangeEvent();
    }

    /* sent to client new selected cells formula if any */
    protected void onCellsAddedToRangeSelection(int col1, int col2, int row1,
            int row2) {
        CellRangeAddress newRange = spreadsheet.createCorrectCellRangeAddress(
                col1, col2, row1, row2);
        for (Iterator<CellReference> i = individualSelectedCells.iterator(); i
                .hasNext();) {
            CellReference cell = i.next();
            if (newRange.isInRange(cell.getRow(), cell.getCol())) {
                i.remove();
            }
        }

        cellRangeAddresses.add(newRange);
        paintedCellRange = null;
        fireNewSelectionChangeEvent();
    }

    /* sent to client new selected cells formula if any */
    protected void onRowSelected(int row, int firstColumnIndex) {
        handleCellSelection(firstColumnIndex, row);
        selectedCellReference = new CellReference(row - 1, firstColumnIndex - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(1,
                spreadsheet.getColumns(), row, row);
        paintedCellRange = cra;
        cellRangeAddresses.add(cra);
        fireNewSelectionChangeEvent();
    }

    /* sent to client new selected cells formula if any */
    protected void onRowAddedToRangeSelection(int row, int firstColumnIndex) {
        boolean oldSelectedCellInRange = false;
        for (CellRangeAddress range : cellRangeAddresses) {
            if (range.isInRange(selectedCellReference.getRow(),
                    selectedCellReference.getCol())) {
                oldSelectedCellInRange = true;
                break;
            }
        }
        if (!oldSelectedCellInRange) {
            individualSelectedCells.add(selectedCellReference);
        }
        handleCellSelection(firstColumnIndex, row);
        selectedCellReference = new CellReference(row - 1, firstColumnIndex - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.add(spreadsheet.createCorrectCellRangeAddress(1,
                spreadsheet.getColumns(), row, row));
        paintedCellRange = null;
        fireNewSelectionChangeEvent();
    }

    /* sent to client new selected cells formula if any */
    protected void onColumnSelected(int col, int firstRowIndex) {
        handleCellSelection(col, firstRowIndex);
        selectedCellReference = new CellReference(firstRowIndex - 1, col - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(col,
                col, 1, spreadsheet.getRows());
        paintedCellRange = cra;
        cellRangeAddresses.add(cra);
        fireNewSelectionChangeEvent();
    }

    /* sent to client new selected cells formula if any */
    protected void onColumnAddedToSelection(int column, int firstRowIndex) {
        boolean oldSelectedCellInRange = false;
        for (CellRangeAddress range : cellRangeAddresses) {
            if (range.isInRange(selectedCellReference.getRow(),
                    selectedCellReference.getCol())) {
                oldSelectedCellInRange = true;
                break;
            }
        }
        if (!oldSelectedCellInRange) {
            individualSelectedCells.add(selectedCellReference);
        }
        handleCellSelection(column, firstRowIndex);
        selectedCellReference = new CellReference(firstRowIndex - 1, column - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.add(spreadsheet.createCorrectCellRangeAddress(
                column, column, 1, spreadsheet.getRows()));
        paintedCellRange = null;
        fireNewSelectionChangeEvent();
    }

    // update selection if the new merged region effects selected cell
    protected void mergedRegionAdded(CellRangeAddress region) {
        boolean fire = false;
        if (region.isInRange(selectedCellReference.getRow(),
                selectedCellReference.getCol())) {
            if (selectedCellReference.getCol() != region.getFirstColumn()
                    || selectedCellReference.getRow() != region.getFirstRow()) {
                handleCellAddressChange(region.getFirstColumn() + 1,
                        region.getFirstRow() + 1);
            }
            selectedCellReference = new CellReference(region.getFirstRow(),
                    region.getFirstColumn());
            fire = true;
        }
        for (Iterator<CellRangeAddress> i = cellRangeAddresses.iterator(); i
                .hasNext();) {
            CellRangeAddress cra = i.next();
            if (CellRangeUtil.contains(region, cra)) {
                i.remove();
                fire = true;
            }
        }
        for (Iterator<CellReference> i = individualSelectedCells.iterator(); i
                .hasNext();) {
            CellReference cr = i.next();
            if (region.isInRange(cr.getRow(), cr.getCol())) {
                i.remove();
                fire = true;
            }
        }
        if (fire) {
            fireNewSelectionChangeEvent();
        }
    }

    // update selection if selected cell matches the region
    protected void mergedRegionRemoved(CellRangeAddress region) {
        if (region.isInRange(selectedCellReference.getRow(),
                selectedCellReference.getCol())) {
            cellRangeAddresses.add(region);
            fireNewSelectionChangeEvent();
        }
    }

    protected void fireNewSelectionChangeEvent() {
        CellRangeAddress selectedCellMergedRegion = null;
        MergedRegion region = spreadsheet.getMergedRegionContainer()
                .getMergedRegionStartingFrom(
                        selectedCellReference.getCol() + 1,
                        selectedCellReference.getRow() + 1);
        if (region != null) {
            selectedCellMergedRegion = new CellRangeAddress(region.row1 - 1,
                    region.row2 - 1, region.col1 - 1, region.col2 - 1);
            // if the only range is the merged region, clear ranges
            if (cellRangeAddresses.size() == 1
                    && cellRangeAddresses.get(0).formatAsString()
                            .equals(selectedCellMergedRegion.formatAsString())) {
                cellRangeAddresses.clear();
            }
        }
        if (latestSelectionEvent != null) {
            boolean changed = false;
            if (!latestSelectionEvent.getSelectedCellReference().equals(
                    selectedCellReference)) {
                changed = true;
            }
            if (!changed) {
                if (latestSelectionEvent.getIndividualSelectedCells().length != individualSelectedCells
                        .size()) {
                    changed = true;
                } else {
                    for (CellReference cr : latestSelectionEvent
                            .getIndividualSelectedCells()) {
                        if (!individualSelectedCells.contains(cr)) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            if (!changed) {
                if (latestSelectionEvent.getCellRangeAddresses().length != cellRangeAddresses
                        .size()) {
                    changed = true;
                } else {
                    for (CellRangeAddress cra : latestSelectionEvent
                            .getCellRangeAddresses()) {
                        if (!cellRangeAddresses.contains(cra)) {
                            changed = true;
                            break;
                        }
                    }
                }
            }
            if (!changed) {
                CellRangeAddress previouSelectedCellMergedRegion = latestSelectionEvent
                        .getSelectedCellMergedRegion();
                if ((previouSelectedCellMergedRegion == null && selectedCellMergedRegion != null)
                        || (previouSelectedCellMergedRegion != null && !previouSelectedCellMergedRegion
                                .equals(selectedCellMergedRegion))) {
                    changed = true;
                }
            }
            if (!changed) {
                return;
            }
        }
        final CellReference[] individualCellsArray = individualSelectedCells
                .toArray(new CellReference[individualSelectedCells.size()]);
        final CellRangeAddress[] cellRangesArray = cellRangeAddresses
                .toArray(new CellRangeAddress[cellRangeAddresses.size()]);
        latestSelectionEvent = new SelectionChangeEvent(spreadsheet,
                selectedCellReference, individualCellsArray,
                selectedCellMergedRegion, cellRangesArray);

        spreadsheet.fireEvent(latestSelectionEvent);
    }

}
