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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.flow.component.spreadsheet.client.MergedRegion;
import com.vaadin.flow.component.spreadsheet.client.MergedRegionUtil;

/**
 * CellSelectionManager is an utility class for Spreadsheet, which handles
 * details of which cells are selected.
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
public class CellSelectionManager implements Serializable {

    private final Spreadsheet spreadsheet;

    private CellReference selectedCellReference;
    private CellRangeAddress paintedCellRange;
    private SelectionChangeEvent latestSelectionEvent;

    private final NamedRangeUtils namedRangeUtils;

    private final ArrayList<CellRangeAddress> cellRangeAddresses = new ArrayList<CellRangeAddress>();
    private final ArrayList<CellReference> individualSelectedCells = new ArrayList<CellReference>();

    /**
     * Creates a new CellSelectionManager and ties it to the given Spreadsheet
     *
     * @param spreadsheet
     */
    public CellSelectionManager(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
        namedRangeUtils = new NamedRangeUtils(spreadsheet);
    }

    /**
     * Clears all selection data
     */
    public void clear() {
        selectedCellReference = null;
        paintedCellRange = null;
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        latestSelectionEvent = null;
    }

    /**
     * Returns reference to the currently selected single cell OR in case of
     * multiple selections the last cell clicked OR in case of area select the
     * cell from which the area selection was started.
     *
     * @return CellReference to selection
     */
    public CellReference getSelectedCellReference() {
        return selectedCellReference;
    }

    /**
     * Returns the currently selected area in case there is only one area
     * selected.
     *
     * @return Single selected area
     */
    public CellRangeAddress getSelectedCellRange() {
        return paintedCellRange;
    }

    /**
     * Returns references to all individually selected cells.
     *
     * @return List of references to single cell selections
     */
    public List<CellReference> getIndividualSelectedCells() {
        return individualSelectedCells;
    }

    /**
     * Returns all selected areas.
     *
     * @return Selected areas
     */
    public List<CellRangeAddress> getCellRangeAddresses() {
        return cellRangeAddresses;
    }

    /**
     * Returns the latest selection event. May be null if no selections have
     * been done, or clear() has been called prior to calling this method.
     *
     * @return Latest SelectionChangeEvent
     */
    public SelectionChangeEvent getLatestSelectionEvent() {
        return latestSelectionEvent;
    }

    boolean isCellInsideSelection(int row, int column) {
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
                            paintedCellRange, true);
                } else {
                    paintedCellRange = null;
                    handleCellAddressChange(selectedCellReference.getRow() + 1,
                            selectedCellReference.getCol() + 1, false);
                }
            } else {
                handleCellRangeSelection(paintedCellRange);
            }
        } else if (selectedCellReference != null) {
            handleCellAddressChange(selectedCellReference.getRow() + 1,
                    selectedCellReference.getCol() + 1, false);
        } else {
            handleCellAddressChange(1, 1, false);
        }
    }

    /**
     * Sets/adds the cell at the given coordinates as/to the current selection.
     *
     * @param row
     *            Row index, 1-based
     * @param column
     *            Column index, 1-based
     * @param discardOldRangeSelection
     *            true to discard previous selections, false to add to the
     *            current selection
     */
    protected void onCellSelected(int row, int column,
            boolean discardOldRangeSelection) {
        CellReference cellReference = new CellReference(row - 1, column - 1);
        CellReference previousCellReference = selectedCellReference;
        if (!cellReference.equals(previousCellReference)
                || discardOldRangeSelection && (!cellRangeAddresses.isEmpty()
                        || !individualSelectedCells.isEmpty())) {
            handleCellSelection(row, column);
            selectedCellReference = cellReference;
            spreadsheet.loadCustomEditorOnSelectedCell();
            if (discardOldRangeSelection) {
                cellRangeAddresses.clear();
                individualSelectedCells.clear();
                paintedCellRange = spreadsheet.createCorrectCellRangeAddress(
                        row, column, row, column);
            }
            ensureClientHasSelectionData();
            fireNewSelectionChangeEvent();
        }
    }

    /**
     * This is called when the sheet's address field has been changed and the
     * sheet selection and function field must be updated.
     *
     * @param value
     *            New value of the address field
     */
    protected void onSheetAddressChanged(String value,
            boolean initialSelection) {
        try {
            if (namedRangeUtils.isNamedRange(value)) {
                namedRangeUtils.onNamedRange(value);
            } else if (value.contains(":")) {
                CellRangeAddress cra = spreadsheet
                        .createCorrectCellRangeAddress(value);
                // need to check the range for merged regions
                MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                        spreadsheet.getMergedRegionContainer(),
                        cra.getFirstRow() + 1, cra.getLastRow() + 1,
                        cra.getFirstColumn() + 1, cra.getLastColumn() + 1);
                if (region != null) {
                    cra = new CellRangeAddress(region.row1 - 1, region.row2 - 1,
                            region.col1 - 1, region.col2 - 1);
                }
                handleCellRangeSelection(cra);
                selectedCellReference = new CellReference(cra.getFirstRow(),
                        cra.getFirstColumn());
                paintedCellRange = cra;
                cellRangeAddresses.clear();
                cellRangeAddresses.add(cra);
            } else if (namedRangeUtils.isCellReference(value)) {
                final CellReference cellReference = new CellReference(value);
                MergedRegion region = MergedRegionUtil.findIncreasingSelection(
                        spreadsheet.getMergedRegionContainer(),
                        cellReference.getRow() + 1, cellReference.getRow() + 1,
                        cellReference.getCol() + 1, cellReference.getCol() + 1);
                if (region != null && (region.col1 != region.col2
                        || region.row1 != region.row2)) {
                    CellRangeAddress cra = spreadsheet
                            .createCorrectCellRangeAddress(region.row1,
                                    region.col1, region.row2, region.col2);
                    handleCellRangeSelection(cra);
                    selectedCellReference = new CellReference(cra.getFirstRow(),
                            cra.getFirstColumn());
                    paintedCellRange = cra;
                    cellRangeAddresses.clear();
                    cellRangeAddresses.add(cra);
                } else {
                    handleCellAddressChange(cellReference.getRow() + 1,
                            cellReference.getCol() + 1, initialSelection);
                    paintedCellRange = spreadsheet
                            .createCorrectCellRangeAddress(
                                    cellReference.getRow() + 1,
                                    cellReference.getCol() + 1,
                                    cellReference.getRow() + 1,
                                    cellReference.getCol() + 1);
                    selectedCellReference = cellReference;
                    cellRangeAddresses.clear();
                }
            }
            individualSelectedCells.clear();
            spreadsheet.loadCustomEditorOnSelectedCell();
            ensureClientHasSelectionData();
            fireNewSelectionChangeEvent();
        } catch (Exception e) {
            spreadsheet.getRpcProxy().invalidCellAddress();
        }
    }

    private void handleCellAddressChange(int rowIndex, int colIndex,
            boolean initialSelection) {
        handleCellAddressChange(rowIndex, colIndex, initialSelection, null);
    }

    /**
     * Reports the correct cell selection value (formula/data) and selection.
     * This method is called when the cell selection has changed via the address
     * field.
     *
     * @param rowIndex
     *            Index of row, 1-based
     * @param columnIndex
     *            Index of column, 1-based
     */
    void handleCellAddressChange(int rowIndex, int colIndex,
            boolean initialSelection, String name) {
        if (rowIndex >= spreadsheet.getRows()) {
            rowIndex = spreadsheet.getRows();
        }
        if (colIndex >= spreadsheet.getCols()) {
            colIndex = spreadsheet.getCols();
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
                    boolean formula = cell.getCellType() == CellType.FORMULA;
                    if (!spreadsheet.isCellHidden(cell)) {
                        if (formula) {
                            value = cell.getCellFormula();
                        } else if (SpreadsheetUtil.needsLeadingQuote(cell)) {
                            value = "'" + spreadsheet.getCellValue(cell);
                        } else {
                            value = spreadsheet.getCellValue(cell);
                        }
                    }
                    spreadsheet.getRpcProxy().showSelectedCell(name, colIndex,
                            rowIndex, value, formula,
                            spreadsheet.isCellLocked(cell), initialSelection);
                } else {
                    spreadsheet.getRpcProxy().showSelectedCell(name, colIndex,
                            rowIndex, "", false, spreadsheet.isCellLocked(cell),
                            initialSelection);
                }
            } else {
                spreadsheet.getRpcProxy().showSelectedCell(name, colIndex,
                        rowIndex, "", false,
                        spreadsheet.isActiveSheetProtected(), initialSelection);
            }
        }
    }

    /**
     * Reselects the currently selected single cell
     */
    protected void reSelectSelectedCell() {
        if (selectedCellReference != null) {
            handleCellSelection(selectedCellReference);
        }
    }

    /**
     * Selects a single cell from the active sheet
     *
     * @param cellReference
     *            Reference to the cell to be selected
     */
    protected void handleCellSelection(CellReference cellReference) {
        handleCellSelection(cellReference.getRow() + 1,
                cellReference.getCol() + 1);
    }

    /**
     * Reports the selected cell formula value, if any. This method is called
     * when the cell value has changed via sheet cell selection change.
     *
     * This method can also be used when the selected cell has NOT changed but
     * the value it displays on the formula field might have changed and needs
     * to be updated.
     *
     * @param rowIndex
     *            1-based
     * @param columnIndex
     *            1-based
     */
    private void handleCellSelection(int rowIndex, int columnIndex) {
        spreadsheet.getRpcProxy().updateFormulaBar(null, columnIndex, rowIndex);
    }

    private void handleCellSelection(int rowIndex, int columnIndex,
            CellRangeAddress cra) {

        final String possibleName = namedRangeUtils
                .getNameForFormulaIfExists(cra);

        spreadsheet.getRpcProxy().updateFormulaBar(possibleName, columnIndex,
                rowIndex);
    }

    protected void handleCellRangeSelection(CellRangeAddress cra) {
        final String possibleName = namedRangeUtils
                .getNameForFormulaIfExists(cra);

        handleCellRangeSelection(possibleName, cra);
    }

    protected void handleCellRangeSelection(String name, CellRangeAddress cra) {

        final CellReference firstCell = new CellReference(cra.getFirstRow(),
                cra.getFirstColumn());

        handleCellRangeSelection(name, firstCell, cra, true);
    }

    protected void handleCellRangeSelection(CellReference startingPoint,
            CellRangeAddress cellsToSelect, boolean scroll) {

        handleCellRangeSelection(null, startingPoint, cellsToSelect, scroll);
    }

    private void handleCellRangeSelection(String name,
            CellReference startingPoint, CellRangeAddress cellsToSelect,
            boolean scroll) {
        int row1 = cellsToSelect.getFirstRow() + 1;
        int row2 = cellsToSelect.getLastRow() + 1;
        int col1 = cellsToSelect.getFirstColumn() + 1;
        int col2 = cellsToSelect.getLastColumn() + 1;

        spreadsheet.getRpcProxy().setSelectedCellAndRange(name,
                startingPoint.getCol() + 1, startingPoint.getRow() + 1, col1,
                col2, row1, row2, scroll);

        selectedCellReference = startingPoint;
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        paintedCellRange = cellsToSelect;
        if (col1 != col2 || row1 != row2) {
            cellRangeAddresses.add(cellsToSelect);
        }
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * Sets the given range as the current selection.
     *
     * @param row1
     *            Starting row index, 1-based
     * @param col1
     *            Starting column index, 1-based
     * @param row2
     *            Ending row index, 1-based
     * @param col2
     *            Ending column index, 1-based
     */
    protected void onCellRangeSelected(int row1, int col1, int row2, int col2) {
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(row1,
                col1, row2, col2);
        paintedCellRange = cra;
        if (col1 != col2 || row1 != row2) {
            cellRangeAddresses.add(cra);
        }
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * Sets the given range and starting point as the current selection.
     *
     * @param selectedCellRow
     *            Index of the row where the paint was started, 1-based
     * @param selectedCellColumn
     *            Index of the column where the paint was started, 1-based
     * @param row1
     *            Starting row index, 1-based
     * @param col1
     *            Starting column index, 1-based
     * @param row2
     *            Ending row index, 1-based
     * @param col2
     *            Ending column index, 1-based
     */
    protected void onCellRangePainted(int selectedCellRow,
            int selectedCellColumn, int row1, int col1, int row2, int col2) {
        cellRangeAddresses.clear();
        individualSelectedCells.clear();

        selectedCellReference = new CellReference(selectedCellRow - 1,
                selectedCellColumn - 1);

        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(row1,
                col1, row2, col2);

        handleCellSelection(selectedCellRow, selectedCellColumn, cra);

        paintedCellRange = cra;
        cellRangeAddresses.add(cra);

        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * Adds the cell at the given coordinates to the current selection.
     *
     * @param row
     *            Row index, 1-based
     * @param column
     *            Column index, 1-based
     */
    protected void onCellAddToSelectionAndSelected(int row, int column) {
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
        handleCellSelection(row, column);
        selectedCellReference = new CellReference(row - 1, column - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        if (individualSelectedCells.contains(selectedCellReference)) {
            individualSelectedCells.remove(
                    individualSelectedCells.indexOf(selectedCellReference));
        }
        paintedCellRange = null;
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * This is called when a cell range has been added to the current selection.
     *
     * @param row1
     *            Starting row index, 1-based
     * @param col1
     *            Starting column index, 1-based
     * @param row2
     *            Ending row index, 1-based
     * @param col2
     *            Ending column index, 1-based
     */
    protected void onCellsAddedToRangeSelection(int row1, int col1, int row2,
            int col2) {
        CellRangeAddress newRange = spreadsheet
                .createCorrectCellRangeAddress(row1, col1, row2, col2);
        for (Iterator<CellReference> i = individualSelectedCells.iterator(); i
                .hasNext();) {
            CellReference cell = i.next();
            if (newRange.isInRange(cell.getRow(), cell.getCol())) {
                i.remove();
            }
        }

        cellRangeAddresses.add(newRange);
        paintedCellRange = null;
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * This is called when a row has been made the current selection
     *
     * @param row
     *            Index of target row, 1-based
     * @param firstColumnIndex
     *            Index of first column, 1-based
     */
    protected void onRowSelected(int row, int firstColumnIndex) {
        handleCellSelection(row, firstColumnIndex);
        selectedCellReference = new CellReference(row - 1,
                firstColumnIndex - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(row, 1,
                row, spreadsheet.getColumns());
        paintedCellRange = cra;
        cellRangeAddresses.add(cra);
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * This is called when a row has been added to the current selection
     *
     * @param row
     *            Index of target row, 1-based
     * @param firstColumnIndex
     *            Index of first column, 1-based
     */
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
        handleCellSelection(row, firstColumnIndex);
        selectedCellReference = new CellReference(row - 1,
                firstColumnIndex - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.add(spreadsheet.createCorrectCellRangeAddress(row, 1,
                row, spreadsheet.getColumns()));
        paintedCellRange = null;
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * This is called when a column has made the current selection
     *
     * @param firstRowIndex
     *            Index of first row, 1-based
     * @param column
     *            Index of target column, 1-based
     */
    protected void onColumnSelected(int firstRowIndex, int column) {
        handleCellSelection(firstRowIndex, column);
        selectedCellReference = new CellReference(firstRowIndex - 1,
                column - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.clear();
        individualSelectedCells.clear();
        CellRangeAddress cra = spreadsheet.createCorrectCellRangeAddress(1,
                column, spreadsheet.getRows(), column);
        paintedCellRange = cra;
        cellRangeAddresses.add(cra);
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * This is called when a column has been added to the current selection
     *
     * @param firstRowIndex
     *            Index of first row, 1-based
     * @param column
     *            Index of target column, 1-based
     */
    protected void onColumnAddedToSelection(int firstRowIndex, int column) {
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
        handleCellSelection(firstRowIndex, column);
        selectedCellReference = new CellReference(firstRowIndex - 1,
                column - 1);
        spreadsheet.loadCustomEditorOnSelectedCell();
        cellRangeAddresses.add(spreadsheet.createCorrectCellRangeAddress(1,
                column, spreadsheet.getRows(), column));
        paintedCellRange = null;
        ensureClientHasSelectionData();
        fireNewSelectionChangeEvent();
    }

    /**
     * This is called when a merged region has been added, since the selection
     * may need to be updated.
     *
     * @param region
     *            Merged region that was added
     */
    protected void mergedRegionAdded(CellRangeAddress region) {
        if (selectedCellReference == null) {
            return;
        }

        boolean fire = false;
        if (region.isInRange(selectedCellReference.getRow(),
                selectedCellReference.getCol())) {
            if (selectedCellReference.getCol() != region.getFirstColumn()
                    || selectedCellReference.getRow() != region.getFirstRow()) {
                handleCellAddressChange(region.getFirstRow() + 1,
                        region.getFirstColumn() + 1, false);
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

    /**
     * This is called when a merged region is removed, since the selection may
     * need to be updated.
     *
     * @param region
     *            Merged region that was removed
     */
    protected void mergedRegionRemoved(CellRangeAddress region) {
        if (selectedCellReference == null) {
            return;
        }
        if (region.isInRange(selectedCellReference.getRow(),
                selectedCellReference.getCol())) {
            cellRangeAddresses.add(region);
            ensureClientHasSelectionData();
            fireNewSelectionChangeEvent();
        }
    }

    /**
     * Make sure that the selected ranges are available on the client side.
     */
    private void ensureClientHasSelectionData() {
        // Make sure data for the selection has been loaded so it can be copied
        for (CellRangeAddress cellRangeAddress : cellRangeAddresses) {
            spreadsheet.loadCells(cellRangeAddress.getFirstRow() + 1,
                    cellRangeAddress.getFirstColumn() + 1,
                    cellRangeAddress.getLastRow() + 1,
                    cellRangeAddress.getLastColumn() + 1);
        }
    }

    /**
     * Fires a new SelectionChangeEvent based on the internal selection state.
     */
    private void fireNewSelectionChangeEvent() {
        CellRangeAddress selectedCellMergedRegion = null;
        MergedRegion region = spreadsheet.getMergedRegionContainer()
                .getMergedRegionStartingFrom(selectedCellReference.getCol() + 1,
                        selectedCellReference.getRow() + 1);
        if (region != null) {
            selectedCellMergedRegion = new CellRangeAddress(region.row1 - 1,
                    region.row2 - 1, region.col1 - 1, region.col2 - 1);
            // if the only range is the merged region, clear ranges
            if (cellRangeAddresses.size() == 1
                    && cellRangeAddresses.get(0).formatAsString().equals(
                            selectedCellMergedRegion.formatAsString())) {
                cellRangeAddresses.clear();
            }
        }
        if (latestSelectionEvent != null) {
            boolean changed = false;
            if (!latestSelectionEvent.getSelectedCellReference()
                    .equals(selectedCellReference)) {
                changed = true;
            }
            if (!changed) {
                if (latestSelectionEvent.getIndividualSelectedCells()
                        .size() != individualSelectedCells.size()) {
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
                if (latestSelectionEvent.getCellRangeAddresses()
                        .size() != cellRangeAddresses.size()) {
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
                if ((previouSelectedCellMergedRegion == null
                        && selectedCellMergedRegion != null)
                        || (previouSelectedCellMergedRegion != null
                                && !previouSelectedCellMergedRegion
                                        .equals(selectedCellMergedRegion))) {
                    changed = true;
                }
            }
            if (!changed) {
                return;
            }
        }
        ArrayList<CellReference> cellRefCopy = new ArrayList<CellReference>(
                individualSelectedCells);
        ArrayList<CellRangeAddress> rangeCopy = new ArrayList<CellRangeAddress>(
                cellRangeAddresses);
        latestSelectionEvent = new SelectionChangeEvent(spreadsheet,
                selectedCellReference, cellRefCopy, selectedCellMergedRegion,
                rangeCopy);

        spreadsheet.fireEvent(latestSelectionEvent);
    }
}
