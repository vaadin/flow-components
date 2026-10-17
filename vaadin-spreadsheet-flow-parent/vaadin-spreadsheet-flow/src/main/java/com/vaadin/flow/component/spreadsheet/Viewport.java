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
import java.util.function.IntPredicate;

/**
 * The rows and columns the client shows, as it reports them, and so the cells
 * it shows: those of four panes, the frozen corner, the frozen rows over the
 * columns shown, the frozen columns beside the rows shown, and the rows and
 * columns shown. None until the client reports where it is, which it does again
 * for every sheet shown. 1-based, as the client numbers them.
 */
final class Viewport implements Serializable {

    /**
     * The most rows that are not hidden one report shows. Well above what the
     * client asks for in the tallest window, with the rows it loads ahead.
     */
    private static final int MAX_SHOWN_ROWS = 2000;

    /**
     * The most columns that are not hidden one report shows. See
     * {@link #MAX_SHOWN_ROWS}.
     */
    private static final int MAX_SHOWN_COLUMNS = 1000;

    private final SheetView sheet;
    private int firstRow = -1;
    private int lastRow = -1;
    private int firstColumn = -1;
    private int lastColumn = -1;

    Viewport(SheetView sheet) {
        this.sheet = sheet;
    }

    /** Whether the client has reported where it is. */
    boolean isReported() {
        return firstRow != -1;
    }

    int getFirstRow() {
        return firstRow;
    }

    int getLastRow() {
        return lastRow;
    }

    int getFirstColumn() {
        return firstColumn;
    }

    int getLastColumn() {
        return lastColumn;
    }

    /**
     * Takes where the client reports it is, limited to the rows and columns of
     * the sheet, and to at most 2,000 rows and 1,000 columns that are not
     * hidden: every cell shown is loaded. Hidden ones do not count, so a
     * filtered sheet shows the rows it has however far apart they are.
     *
     * @return whether the rows or columns shown changed
     */
    boolean report(int firstRow, int firstColumn, int lastRow, int lastColumn) {
        int maxRow = Math.max(1, sheet.rows());
        int maxColumn = Math.max(1, sheet.columns());
        firstRow = Math.clamp(firstRow, 1, maxRow);
        lastRow = lastWithShownCount(firstRow,
                Math.clamp(lastRow, firstRow, maxRow), MAX_SHOWN_ROWS,
                row -> sheet.isRowHidden(row - 1));
        firstColumn = Math.clamp(firstColumn, 1, maxColumn);
        lastColumn = lastWithShownCount(firstColumn,
                Math.clamp(lastColumn, firstColumn, maxColumn),
                MAX_SHOWN_COLUMNS, column -> sheet.isColumnHidden(column - 1));
        boolean changed = this.firstRow != firstRow || this.lastRow != lastRow
                || this.firstColumn != firstColumn
                || this.lastColumn != lastColumn;
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
        return changed;
    }

    /**
     * The last index from {@code first} to {@code last} up to which no more
     * than {@code max} indexes are shown.
     */
    private static int lastWithShownCount(int first, int last, int max,
            IntPredicate hidden) {
        int shown = 0;
        for (int index = first; index <= last; index++) {
            if (!hidden.test(index) && ++shown == max) {
                return index;
            }
        }
        return last;
    }

    /** Whether the client shows the cell, in one of the four panes. */
    boolean shows(int row, int column) {
        boolean inRows = row >= firstRow && row <= lastRow
                || row >= 1 && row <= sheet.frozenRows();
        boolean inColumns = column >= firstColumn && column <= lastColumn
                || column >= 1 && column <= sheet.frozenColumns();
        return inRows && inColumns;
    }

    /** A range of cells to scan, 1-based and inclusive. */
    @FunctionalInterface
    interface PaneScan extends Serializable {
        void scan(int firstRow, int firstColumn, int lastRow, int lastColumn);
    }

    /**
     * Scans the cells shown, pane by pane. A pane without frozen rows or
     * columns holds no cells, and those of the rows or columns shown are left
     * out while where the client is has not been reported.
     */
    void forEachPane(PaneScan scan) {
        int frozenRows = sheet.frozenRows();
        int frozenColumns = sheet.frozenColumns();
        scanOnTheSheet(1, 1, frozenRows, frozenColumns, scan);
        scanOnTheSheet(1, firstColumn, frozenRows, lastColumn, scan);
        scanOnTheSheet(firstRow, 1, lastRow, frozenColumns, scan);
        scanOnTheSheet(firstRow, firstColumn, lastRow, lastColumn, scan);
    }

    /**
     * Scans the cells of a pane, unless it starts before the first row or
     * column: where the client is has not been reported.
     */
    static void scanOnTheSheet(int firstRow, int firstColumn, int lastRow,
            int lastColumn, PaneScan scan) {
        if (firstRow >= 1 && firstColumn >= 1) {
            scan.scan(firstRow, firstColumn, lastRow, lastColumn);
        }
    }
}
