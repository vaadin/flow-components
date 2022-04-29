package com.vaadin.addon.spreadsheet.client;

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

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.NativeEvent;
import com.vaadin.addon.spreadsheet.client.GroupingWidget.GroupingHandler;

public interface SheetHandler extends GroupingHandler {

    void onCellClick(int column, int row, String value, boolean shiftPressed,
            boolean metaOrCtrlPressed, boolean updateToActionHandler);

    void onLinkCellClick(int column, int row);

    void onCellDoubleClick(int column, int row, String value);

    void onRowHeaderClick(int row, boolean shiftPressed,
            boolean metaOrCrtlPressed);

    void onColumnHeaderClick(int row, boolean shiftPressed,
            boolean metaOrCrtlPressed);

    void onScrollViewChanged(int firstRow, int lastRow, int firstColumn,
            int lastColumn);

    void onSelectionIncreasePainted(int c1, int c2, int r1, int r2);

    void onSelectionDecreasePainted(int colEdgeIndex, int rowEdgeIndex);

    void onFinishedSelectingCellsWithDrag(int col1, int col2, int row1,
            int row2);

    void onSelectingCellsWithDrag(int parsedCol, int parsedRow);

    void onCellInputBlur(String value);

    void onCellInputFocus();

    void onCellInputCancel();

    void onCellInputEnter(String value, boolean shift);

    void onCellInputTab(String value, boolean shift);

    void onCellInputValueChange(String value);

    void onSheetKeyPress(NativeEvent nativeEvent, String enteredCharacter);

    int getRowBufferSize();

    int getColumnBufferSize();

    /**
     * default row height in points (?)
     *
     * @return
     */
    float getDefaultRowHeight();

    /** Number of defined rows in the spreadsheet */
    int getDefinedRows();

    int[] getColWidths();

    /**
     * Height of a row in points (pt) including bottom border. Rows are indexed
     * from 1 to getRows(). Returns 0 for hidden rows.
     */
    float getRowHeight(int row);

    /**
     * Width of a column in pixels including right border. Columns are indexed
     * from 1 to getColumns(). Returns 0 for hidden columns.
     */
    int getColWidth(int col);

    /**
     * Returns 0 for hidden columns, otherwise same as {@link #getColWidth(int)}
     * .
     *
     * @param col
     *            1-based
     * @return width (px)
     */
    int getColWidthActual(int col);

    /**
     * Get header of a column as HTML. Columns are indexed from 1 to
     * getColumns().
     */
    String getColHeader(int col);

    /** Get header of a row as HTML. Rows are indexed from 1 to getRows(). */
    String getRowHeader(int row);

    /**
     * The maximum amount of columns that are visible
     *
     * @return
     */
    int getMaxColumns();

    /**
     * The maximum amount of rows that are visible
     *
     * @return
     */
    int getMaxRows();

    int[] getRowHeightsPX();

    Map<Integer, String> getCellStyleToCSSStyle();

    Map<Integer, Integer> getRowIndexToStyleIndex();

    Map<Integer, Integer> getColumnIndexToStyleIndex();

    Map<Integer, String> getConditionalFormattingStyles();

    /**
     *
     * @param i
     *            1-based
     * @return true if the column is hidden
     */
    boolean isColumnHidden(int i);

    /**
     *
     * @param i
     *            1-based
     * @return true if the row is hidden
     */
    boolean isRowHidden(int i);

    /**
     * Called on right mouse button click on top of some cell.
     *
     * @param nativeEvent
     * @param column
     *            1-based
     * @param row
     *            1-based
     */
    void onCellRightClick(NativeEvent nativeEvent, int column, int row);

    /**
     * Called on right mouse button click on top of a row header
     *
     * @param nativeEvent
     * @param rowIndex
     *            1-based
     */
    void onRowHeaderRightClick(NativeEvent nativeEvent, int rowIndex);

    /**
     * Called on right mouse button click on top of a column header
     *
     * @param nativeEvent
     * @param columnIndex
     *            1-based
     */
    void onColumnHeaderRightClick(NativeEvent nativeEvent, int columnIndex);

    boolean hasCustomContextMenu();

    boolean canResizeColumn();

    boolean canResizeRow();

    /** Map containing 1-based row indexes and new sizes as pt */
    void onRowsResized(Map<Integer, Float> newSizes);

    /** Map containing 1-based column indexes and new sizes as pt */
    void onColumnsResized(Map<Integer, Integer> newSizes);

    /**
     * @param rowIndex
     *            1-based
     */
    void onRowHeaderDoubleClick(int rowIndex);

    /**
     *
     * @param columnIndex
     *            1-based
     */
    void onColumnHeaderResizeDoubleClick(int columnIndex);

    /**
     * Returns the merged region that this cell belongs to.
     *
     * @param col
     * @param row
     * @return
     */
    MergedRegion getMergedRegion(int col, int row);

    /**
     * Params 1-based
     *
     * @param col
     *            starting column of merged cell
     * @param row
     *            starting row of merged cell
     * @return
     */
    MergedRegion getMergedRegionStartingFrom(int col, int row);

    void onRedoPress();

    void onUndoPress();

    void setCellStyleWidthRatios(
            HashMap<Integer, Float> cellStyleWidthRatioMap);

    /**
     * Called when user pastes something inside the sheet.
     *
     * @param text
     *            the pasted content
     */
    void onSheetPaste(String text);

    /**
     * Called after successful cut operation; currently selected cells should be
     * cleared
     */
    void clearSelectedCellsOnCut();

    FormulaBarWidget getFormulaBarWidget();

    void updateCellComment(String text, int col, int row);

    void selectAll();

    boolean isSheetProtected();

    boolean isColProtected(int col);

    boolean isRowProtected(int row);

}
