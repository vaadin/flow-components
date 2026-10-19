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
import com.vaadin.flow.dom.Element;

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

    private final ClientProperty<List<GroupingData>> colGroupingData;
    private final ClientProperty<List<GroupingData>> rowGroupingData;
    private final ClientProperty<Integer> colGroupingMax;
    private final ClientProperty<Integer> rowGroupingMax;
    private final ClientProperty<Boolean> colGroupingInversed;
    private final ClientProperty<Boolean> rowGroupingInversed;

    SheetGrouping(Element element) {
        colGroupingData = ClientProperty.json(element, "colGroupingData");
        rowGroupingData = ClientProperty.json(element, "rowGroupingData");
        colGroupingMax = ClientProperty.integer(element, "colGroupingMax", 0);
        rowGroupingMax = ClientProperty.integer(element, "rowGroupingMax", 0);
        colGroupingInversed = ClientProperty.bool(element,
                "colGroupingInversed", false);
        rowGroupingInversed = ClientProperty.bool(element,
                "rowGroupingInversed", false);
    }

    List<GroupingData> getColGroupingData() {
        return colGroupingData.get();
    }

    void setColGroupingData(List<GroupingData> colGroupingData) {
        this.colGroupingData.set(colGroupingData);
    }

    void setRowGroupingData(List<GroupingData> rowGroupingData) {
        this.rowGroupingData.set(rowGroupingData);
    }

    int getColGroupingMax() {
        return colGroupingMax.get();
    }

    void setColGroupingMax(int colGroupingMax) {
        this.colGroupingMax.set(colGroupingMax);
    }

    int getRowGroupingMax() {
        return rowGroupingMax.get();
    }

    void setRowGroupingMax(int rowGroupingMax) {
        this.rowGroupingMax.set(rowGroupingMax);
    }

    boolean isColGroupingInversed() {
        return colGroupingInversed.get();
    }

    void setColGroupingInversed(boolean colGroupingInversed) {
        this.colGroupingInversed.set(colGroupingInversed);
    }

    boolean isRowGroupingInversed() {
        return rowGroupingInversed.get();
    }

    void setRowGroupingInversed(boolean rowGroupingInversed) {
        this.rowGroupingInversed.set(rowGroupingInversed);
    }
}
