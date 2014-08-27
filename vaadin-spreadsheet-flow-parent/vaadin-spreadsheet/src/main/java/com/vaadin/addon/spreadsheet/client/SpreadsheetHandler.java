package com.vaadin.addon.spreadsheet.client;

import java.util.HashMap;
import java.util.Map;

public interface SpreadsheetHandler {

    /**
     * These cells have become visible and possibly need the content, if has not
     * been given previously or has not changed.
     */
    public void onSheetScroll(int firstRow, int lastRow, int firstColumn,
            int lastColumn);

    /** Address field value changed. */
    public void sheetAddressChanged(String value);

    /** Single cell selected inside sheet. */
    public void cellSelected(int column, int row,
            boolean oldSelectionRangeDiscarded);

    /** Cell range selected from scratch. Actual selected cell not changed. */
    public void cellRangeSelected(int col1, int col2, int row1, int row2);

    /** Single cell added to selection. Selection changed to this. */
    public void cellAddedToSelectionAndSelected(int column, int row);

    /**
     * Multiple cells added to previous range selection. Actual selected cell
     * not changed.
     */
    public void cellsAddedToRangeSelection(int col1, int col2, int row1,
            int row2);

    /**
     * Complete row selected. New selected cell is at firstColumnIndex:row.
     * 
     * @param row
     *            the row that was selected
     * @param firstColumnIndex
     *            column index for the selected cell (left most visible)
     */
    public void rowSelected(int row, int firstColumnIndex);

    /**
     * Complete row added to previous range selection. New selected cell is at
     * firstColumnIndex:row.
     * 
     * @param row
     *            the row that was selected
     * @param firstColumnIndex
     *            column index for the selected cell (left most visible)
     */
    public void rowAddedToRangeSelection(int row, int firstColumnIndex);

    /**
     * Complete column selected. New selected cell is at column:firstRowIndex.
     * 
     * @param column
     *            the column that was selected
     * @param firstRowIndex
     *            row index for the selected cell (top most visible)
     */
    public void columnSelected(int column, int firstRowIndex);

    /**
     * Complete column added to previous range selection. New selected cell is
     * at column:firstRowIndex.
     * 
     * @param column
     *            the column that was selected
     * @param firstRowIndex
     *            row index for the selected cell (top most)
     */
    public void columnAddedToSelection(int column, int firstRowIndex);

    /**
     * The new selection that was painted from the old. Values and formulas
     * should be painted to the new selection.
     * 
     * @param c1
     *            new selection left, 1-based
     * @param c2
     *            new selection right, 1-based
     * @param r1
     *            new selection top, 1-based
     * @param r2
     *            new selection bottom, 1-based
     */
    public void selectionIncreasePainted(int c1, int c2, int r1, int r2);

    /**
     * The existing selection has been painted inwards meaning that the painted
     * selection cells should be cleared.
     * 
     * @param col
     *            leftmost cell index where the clearing starts, 1-based
     * @param row
     *            topmost cell index where the clearing starts, 1-based
     */
    public void selectionDecreasePainted(int col, int row);

    public void cellValueEdited(int col, int row, String value);

    /**
     * 
     * @param sheetIndex
     *            0-based
     * @param scrollTop
     * @param scrollLeft
     */
    public void sheetSelected(int sheetIndex, int scrollLeft, int scrollTop);

    /**
     * 
     * @param sheetIndex
     *            0-based
     * @param newName
     */
    public void sheetRenamed(int sheetIndex, String newName);

    /**
     * Sheet is created as the last sheet
     * 
     * @param scrollTop
     * @param scrollLeft
     */
    public void sheetCreated(int scrollLeft, int scrollTop);

    /**
     * Cell range selected by painting
     * 
     * @param selectedCellColumn
     * @param selectedCellRow
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void cellRangePainted(int selectedCellColumn, int selectedCellRow,
            int col1, int col2, int row1, int row2);

    /**
     * Delete the contents of the selected cells, do not remove
     * style/formatting.
     */
    public void deleteSelectedCells();

    /**
     * A cell containing a hyperlink has been clicked.
     * 
     * @param column
     *            1-based
     * @param row
     *            1-based
     */
    public void linkCellClicked(int column, int row);

    /**
     * Rows resized with header drag and drop. Indexes 1-based.
     * 
     * @param newRowSizes
     *            row index and new size (converted pt)
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void rowsResized(Map<Integer, Float> newRowSizes, int col1,
            int col2, int row1, int row2);

    /**
     * Columns resized with drag and drop. Indexes 1-based.
     * 
     * @param newRowSizes
     *            column index and new size (px)
     * @param col1
     * @param col2
     * @param row1
     * @param row2
     */
    public void columnResized(Map<Integer, Integer> newColumnSizes, int col1,
            int col2, int row1, int row2);

    /**
     * Column autofit with double click on the column header resizing area.
     * 
     * @param columnIndex
     *            1-based
     */
    public void onColumnAutofit(int columnIndex);

    /**
     * Client pressed undo ctrl/meta+z
     */
    public void onUndo();

    /**
     * Client pressed redo ctrl/meta+y
     */
    public void onRedo();

    public void setCellStyleWidthRatios(
            HashMap<Integer, Float> cellStyleWidthRatioMap);

}
