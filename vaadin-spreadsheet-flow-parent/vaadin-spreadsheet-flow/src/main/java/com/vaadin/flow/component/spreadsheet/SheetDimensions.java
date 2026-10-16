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
import java.util.ArrayList;

/**
 * Per-sheet row-height / column-width arrays plus visibility (hidden rows /
 * columns) and the default row-height / column-width values.
 *
 * <p>
 * Every setter mirrors the value into the corresponding element property so the
 * client gets the change on the next render. The arrays are serialized through
 * {@link Serializer#serialize}; the primitives go through directly.
 *
 * <p>
 * The data here is consumed by both {@code RowSizeOperations} /
 * {@code ColumnSizeOperations} (the operations on row / column geometry) and
 * the client-rendering path; this class holds it as single-source state so the
 * operations and the renderer can't drift on what the current row heights are.
 */
final class SheetDimensions implements Serializable {

    private final Spreadsheet spreadsheet;

    private float defRowH;
    private int defColW;
    private float[] rowH;
    private int[] colW;
    private ArrayList<Integer> hiddenColumnIndexes;
    private ArrayList<Integer> hiddenRowIndexes;

    SheetDimensions(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    float getDefRowH() {
        return defRowH;
    }

    void setDefRowH(float defRowH) {
        this.defRowH = defRowH;
        spreadsheet.getElement().setProperty("defRowH", defRowH);
    }

    int getDefColW() {
        return defColW;
    }

    void setDefColW(int defColW) {
        this.defColW = defColW;
        spreadsheet.getElement().setProperty("defColW", defColW);
    }

    float[] getRowH() {
        return rowH;
    }

    void setRowH(float[] rowH) {
        this.rowH = rowH;
        spreadsheet.getElement().setProperty("rowH",
                Serializer.serialize(rowH == null ? null : SizeRuns.of(rowH)));
    }

    int[] getColW() {
        return colW;
    }

    void setColW(int[] colW) {
        this.colW = colW;
        spreadsheet.getElement().setProperty("colW",
                Serializer.serialize(colW == null ? null : SizeRuns.of(colW)));
    }

    ArrayList<Integer> getHiddenColumnIndexes() {
        return hiddenColumnIndexes;
    }

    void setHiddenColumnIndexes(ArrayList<Integer> hiddenColumnIndexes) {
        this.hiddenColumnIndexes = hiddenColumnIndexes;
        spreadsheet.getElement().setProperty("hiddenColumnIndexes",
                Serializer.serialize(hiddenColumnIndexes));
    }

    ArrayList<Integer> getHiddenRowIndexes() {
        return hiddenRowIndexes;
    }

    void setHiddenRowIndexes(ArrayList<Integer> hiddenRowIndexes) {
        this.hiddenRowIndexes = hiddenRowIndexes;
        spreadsheet.getElement().setProperty("hiddenRowIndexes",
                Serializer.serialize(hiddenRowIndexes));
    }
}
