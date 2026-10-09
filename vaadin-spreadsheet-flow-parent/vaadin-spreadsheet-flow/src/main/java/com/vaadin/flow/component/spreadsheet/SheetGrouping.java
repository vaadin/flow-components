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
import java.util.List;

import com.vaadin.flow.component.spreadsheet.shared.GroupingData;

/**
 * Row and column grouping (outline) metadata mirrored to the client.
 *
 * <p>
 * Five fields representing the two grouping axes:
 *
 * <ul>
 * <li>{@code colGroupingData} — the list of column-grouping entries (start /
 * end column indices, group level).</li>
 * <li>{@code colGroupingMax}, {@code rowGroupingMax} — the deepest nesting
 * level on each axis, used by the client to size the grouping gutter.</li>
 * <li>{@code colGroupingInversed}, {@code rowGroupingInversed} — whether the
 * group's collapse / expand control is at the start ({@code true}) or end
 * ({@code false}) of the range.</li>
 * </ul>
 *
 * <p>
 * Note: row-grouping data is mirrored without server-side caching — the setter
 * for it pushes directly to the element. Only the column-grouping data is
 * stored here.
 */
final class SheetGrouping implements Serializable {

    private final Spreadsheet spreadsheet;

    private List<GroupingData> colGroupingData;
    private int colGroupingMax;
    private int rowGroupingMax;
    private boolean colGroupingInversed;
    private boolean rowGroupingInversed;

    SheetGrouping(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    List<GroupingData> getColGroupingData() {
        return colGroupingData;
    }

    void setColGroupingData(List<GroupingData> colGroupingData) {
        this.colGroupingData = colGroupingData;
        spreadsheet.getElement().setProperty("colGroupingData",
                Serializer.serialize(colGroupingData));
    }

    void setRowGroupingData(List<GroupingData> rowGroupingData) {
        // Unlike the column data, row-grouping data goes straight to the
        // element without server-side caching.
        spreadsheet.getElement().setProperty("rowGroupingData",
                Serializer.serialize(rowGroupingData));
    }

    int getColGroupingMax() {
        return colGroupingMax;
    }

    void setColGroupingMax(int colGroupingMax) {
        this.colGroupingMax = colGroupingMax;
        spreadsheet.getElement().setProperty("colGroupingMax", colGroupingMax);
    }

    int getRowGroupingMax() {
        return rowGroupingMax;
    }

    void setRowGroupingMax(int rowGroupingMax) {
        this.rowGroupingMax = rowGroupingMax;
        spreadsheet.getElement().setProperty("rowGroupingMax", rowGroupingMax);
    }

    boolean isColGroupingInversed() {
        return colGroupingInversed;
    }

    void setColGroupingInversed(boolean colGroupingInversed) {
        this.colGroupingInversed = colGroupingInversed;
        spreadsheet.getElement().setProperty("colGroupingInversed",
                colGroupingInversed);
    }

    boolean isRowGroupingInversed() {
        return rowGroupingInversed;
    }

    void setRowGroupingInversed(boolean rowGroupingInversed) {
        this.rowGroupingInversed = rowGroupingInversed;
        spreadsheet.getElement().setProperty("rowGroupingInversed",
                rowGroupingInversed);
    }
}
