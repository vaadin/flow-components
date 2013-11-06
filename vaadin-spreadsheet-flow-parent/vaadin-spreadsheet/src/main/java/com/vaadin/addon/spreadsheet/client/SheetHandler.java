package com.vaadin.addon.spreadsheet.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Event;
import com.vaadin.addon.spreadsheet.client.MergedRegionUtil.MergedRegionContainer;

public interface SheetHandler {

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

    void onSelectionDecreasePainted(int col1, int col2, int colEdgeIndex,
            int row1, int row2, int rowEdgeIndex);

    void onFinishedSelectingCellsWithDrag(int col1, int col2, int row1, int row2);

    void onSelectingCellsWithDrag(int parsedCol, int parsedRow);

    void onCellInputBlur(String value);

    void onCellInputFocus();

    void onCellInputCancel();

    void onCellInputEnter(String value, boolean shift);

    void onCellInputTab(String value, boolean shift);

    void onCellInputValueChange(String value);

    void onSheetKeyPress(Event event, String enteredCharacter);

    int getRowBufferSize();

    int getColumnBufferSize();

    /**
     * default row height in points (?)
     * 
     * @return
     */
    float getDefaultRowHeight();

    int getDefaultColumnWidth();

    /** Number of defined rows in the spreadsheet */
    int getDefinedRows();

    /** Number of defined columns in the spreadsheet */
    int getDefinedCols();

    float[] getRowHeights();

    int[] getColWidths();

    /**
     * Height of a row in points (pt) including bottom border. Rows are indexed
     * from 1 to getRows(). Returns size for hidden rows too!
     */
    float getRowHeight(int row);

    /**
     * Width of a row in pixels including right border. Columns are indexed from
     * 1 to getColumns(). Returns size for hidden columns too!
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

    int getActiveSheetIndex();

    int getNumberOfSheets();

    /**
     * The maximum amount of columns that are visible
     * 
     * @return
     */
    int getMaximumCols();

    /**
     * The maximum amount of rows that are visible
     * 
     * @return
     */
    int getMaximumRows();

    int[] getRowHeightsPX();

    Map<Integer, String> getCellStyles();

    List<String> getCustomCellBorderStyles();

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
     * @param event
     * @param column
     *            1-based
     * @param row
     *            1-based
     */
    void onCellRightClick(Event event, int column, int row);

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

    boolean canResize();

    /** Map containing 1-based row indexes and new sizes as pt */
    void onRowsResized(Map<Integer, Float> newSizes);

    /** Map containing 1-based column indexes and new sizes as pt */
    void onColumnsResized(Map<Integer, Integer> newSizes);

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

    MergedRegionContainer getMergedRegionContainer();

}