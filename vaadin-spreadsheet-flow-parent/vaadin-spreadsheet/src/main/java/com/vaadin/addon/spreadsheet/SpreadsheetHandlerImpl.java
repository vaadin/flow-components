package com.vaadin.addon.spreadsheet;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.addon.spreadsheet.client.SpreadsheetServerRpc;

public class SpreadsheetHandlerImpl implements SpreadsheetServerRpc {

    private Spreadsheet spreadsheet;

    public SpreadsheetHandlerImpl(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    public void onSheetScroll(int firstRow, int lastRow, int firstColumn,
            int lastColumn) {
        spreadsheet.onSheetScroll(firstRow, lastRow, firstColumn, lastColumn);
    }

    @Override
    public void cellSelected(int column, int row,
            boolean discardOldRangeSelection) {
        spreadsheet.getCellSelectionManager().onCellSelected(column, row,
                discardOldRangeSelection);
    }

    @Override
    public void sheetAddressChanged(String value) {
        spreadsheet.getCellSelectionManager().onSheetAddressChanged(value);
    }

    @Override
    public void cellRangeSelected(int col1, int col2, int row1, int row2) {
        spreadsheet.getCellSelectionManager().onCellRangeSelected(col1, col2,
                row1, row2);
    }

    /* */
    @Override
    public void cellRangePainted(int selectedCellColumn, int selectedCellRow,
            int col1, int col2, int row1, int row2) {
        spreadsheet.getCellSelectionManager().onCellRangePainted(
                selectedCellColumn, selectedCellRow, col1, col2, row1, row2);
    }

    @Override
    public void cellAddedToSelectionAndSelected(int column, int row) {
        spreadsheet.getCellSelectionManager().onCellAddToSelectionAndSelected(
                column, row);
    }

    @Override
    public void cellsAddedToRangeSelection(int col1, int col2, int row1,
            int row2) {
        spreadsheet.getCellSelectionManager().onCellsAddedToRangeSelection(
                col1, col2, row1, row2);
    }

    @Override
    public void rowSelected(int row, int firstColumnIndex) {
        spreadsheet.getCellSelectionManager().onRowSelected(row,
                firstColumnIndex);
    }

    @Override
    public void rowAddedToRangeSelection(int row, int firstColumnIndex) {
        spreadsheet.getCellSelectionManager().onRowAddedToRangeSelection(row,
                firstColumnIndex);
    }

    @Override
    public void columnSelected(int col, int firstRowIndex) {
        spreadsheet.getCellSelectionManager().onColumnSelected(col,
                firstRowIndex);
    }

    @Override
    public void columnAddedToSelection(int column, int firstRowIndex) {
        spreadsheet.getCellSelectionManager().onColumnAddedToSelection(column,
                firstRowIndex);
    }

    /* the actual selected cell hasn't changed */
    @Override
    public void selectionIncreasePainted(int c1, int c2, int r1, int r2) {
        spreadsheet.getCellShifter().onSelectionIncreasePainted(c1, c2, r1, r2);
    }

    /* the actual selected cell hasn't changed */
    @Override
    public void selectionDecreasePainted(int c, int r) {
        spreadsheet.getCellShifter().onSelectionDecreasePainted(c, r);
    }

    @Override
    public void cellValueEdited(int col, int row, String value) {
        spreadsheet.getCellValueManager().onCellValueChange(col, row, value);
    }

    @Override
    public void sheetSelected(int tabIndex, int scrollLeft, int scrollTop) {
        spreadsheet.onSheetSelected(tabIndex, scrollLeft, scrollTop);
    }

    @Override
    public void sheetRenamed(int sheetIndex, String sheetName) {
        spreadsheet.onSheetRename(sheetIndex, sheetName);
    }

    @Override
    public void sheetCreated(int scrollLeft, int scrollTop) {
        spreadsheet.onNewSheetCreated(scrollLeft, scrollTop);
    }

    @Override
    public void deleteSelectedCells() {
        spreadsheet.getCellValueManager().onDeleteSelectedCells();
    }

    @Override
    public void linkCellClicked(int column, int row) {
        spreadsheet.onLinkCellClick(column, row);
    }

    @Override
    public void contextMenuOpenOnSelection(int column, int row) {
        spreadsheet.getContextMenuManager().onContextMenuOpenOnSelection(
                column, row);
    }

    @Override
    public void rowHeaderContextMenuOpen(int rowIndex) {
        spreadsheet.getContextMenuManager()
                .onRowHeaderContextMenuOpen(rowIndex);
    }

    @Override
    public void columnHeaderContextMenuOpen(int columnIndex) {
        spreadsheet.getContextMenuManager().onColumnHeaderContextMenuOpen(
                columnIndex);
    }

    @Override
    public void actionOnCurrentSelection(String actionKey) {
        spreadsheet.getContextMenuManager().onActionOnCurrentSelection(
                actionKey);
    }

    @Override
    public void actionOnRowHeader(String actionKey) {
        spreadsheet.getContextMenuManager().onActionOnRowHeader(actionKey);
    }

    @Override
    public void actionOnColumnHeader(String actionKey) {
        spreadsheet.getContextMenuManager().onActionOnColumnHeader(actionKey);
    }

    @Override
    public void rowsResized(Map<Integer, Float> newRowSizes, int col1,
            int col2, int row1, int row2) {
        spreadsheet.onRowResized(newRowSizes, col1, col2, row1, row2);
    }

    @Override
    public void columnResized(Map<Integer, Integer> newColumnSizes, int col1,
            int col2, int row1, int row2) {
        spreadsheet.onColumnResized(newColumnSizes, col1, col2, row1, row2);
    }

    @Override
    public void onColumnAutofit(int columnIndex) {
        spreadsheet.onColumnAutofit(columnIndex - 1);
    }

    @Override
    public void onUndo() {
        spreadsheet.getSpreadsheetHistoryManager().undo();
    }

    @Override
    public void onRedo() {
        spreadsheet.getSpreadsheetHistoryManager().redo();
    }

    @Override
    public void setCellStyleWidthRatios(
            HashMap<Integer, Float> cellStyleWidthRatioMap) {
        spreadsheet.getCellValueManager().onCellStyleWidthRatioUpdate(
                cellStyleWidthRatioMap);
    }

}
