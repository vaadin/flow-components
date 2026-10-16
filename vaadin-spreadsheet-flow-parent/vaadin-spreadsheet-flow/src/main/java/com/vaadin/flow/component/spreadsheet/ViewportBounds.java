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

/**
 * Server-side memo of the cell range the client is currently displaying.
 *
 * <p>
 * Four 1-based indices: first / last row, first / last column. Updated from
 * {@code onSheetScroll} when the client reports a new viewport; read by
 * Spreadsheet's public getters and by code that decides what cells to push to
 * the client.
 *
 * <p>
 * Holds no POI references and no Spreadsheet reference — it's a pure state
 * container plus an equality predicate for cheap change detection.
 */
final class ViewportBounds implements Serializable {

    private int firstRow;
    private int lastRow;
    private int firstColumn;
    private int lastColumn;

    /** Not reported yet: every bound is -1. */
    ViewportBounds() {
        update(-1, -1, -1, -1);
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
     * {@code true} if any of the four indices differ from the currently
     * remembered viewport. Used by {@code onSheetScroll} to decide whether to
     * issue a fresh cell-load to the client.
     */
    boolean hasChanged(int firstRow, int lastRow, int firstColumn,
            int lastColumn) {
        return this.firstRow != firstRow || this.lastRow != lastRow
                || this.firstColumn != firstColumn
                || this.lastColumn != lastColumn;
    }

    void update(int firstRow, int lastRow, int firstColumn, int lastColumn) {
        this.firstRow = firstRow;
        this.lastRow = lastRow;
        this.firstColumn = firstColumn;
        this.lastColumn = lastColumn;
    }

    /** A range of cells to scan, 1-based and inclusive. */
    @FunctionalInterface
    interface PaneScan extends Serializable {
        void scan(int firstRow, int firstColumn, int lastRow, int lastColumn);
    }

    /**
     * Scans the cells shown, pane by pane: the frozen corner, the frozen rows
     * over the columns shown, the frozen columns beside the rows shown, and the
     * rows and columns shown. A pane without frozen rows or columns holds no
     * cells, and one of the rows or columns shown is left out while where the
     * client is has not been reported.
     */
    void forEachPane(int frozenRows, int frozenColumns, PaneScan scan) {
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
