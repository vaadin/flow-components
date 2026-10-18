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
import java.util.HashMap;
import java.util.Set;

/**
 * Per-sheet CSS-style index maps mirrored to the client.
 *
 * <p>
 * Six properties, populated by {@code SpreadsheetStyleFactory} and
 * {@code ConditionalFormatter} and mirrored to the client as the source of
 * truth for cell appearance:
 *
 * <ul>
 * <li>{@code rowIndexToStyleIndex} / {@code columnIndexToStyleIndex} — row /
 * column index → POI style index (overrides per row / column).</li>
 * <li>{@code lockedColumnIndexes} / {@code lockedRowIndexes} — the indexes that
 * are locked for editing.</li>
 * <li>{@code shiftedCellBorderStyles} — CSS rules of the borders the cells draw
 * on other cells than their own: a top border at the bottom of the cell above,
 * a left one at the right of the cell before, and those inside a merged region
 * on the merged cell. Only sent: the style factory keeps them.</li>
 * <li>{@code conditionalFormattingStyles} — CSS rules for conditional-format
 * highlights (rule index → CSS body).</li>
 * </ul>
 *
 * <p>
 * Note: {@code cellStyleToCSSStyle} is intentionally <em>not</em> here. That
 * field is {@code protected} on {@link Spreadsheet} and would break downstream
 * subclasses if moved — so it stays on the parent.
 *
 * <p>
 * Every setter mirrors the value to the corresponding element property. Setters
 * that take collections go through {@link Serializer#toJson}.
 */
final class SheetCssStyleMaps implements Serializable {

    private final Spreadsheet spreadsheet;

    private HashMap<Integer, Integer> rowIndexToStyleIndex;
    private HashMap<Integer, Integer> columnIndexToStyleIndex;
    private Set<Integer> lockedColumnIndexes;
    private Set<Integer> lockedRowIndexes;
    private HashMap<Integer, String> conditionalFormattingStyles;

    SheetCssStyleMaps(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    HashMap<Integer, Integer> getRowIndexToStyleIndex() {
        return rowIndexToStyleIndex;
    }

    void setRowIndexToStyleIndex(
            HashMap<Integer, Integer> rowIndexToStyleIndex) {
        this.rowIndexToStyleIndex = rowIndexToStyleIndex;
        spreadsheet.getElement().setPropertyJson("rowIndexToStyleIndex",
                Serializer.toJson(rowIndexToStyleIndex));
    }

    HashMap<Integer, Integer> getColumnIndexToStyleIndex() {
        return columnIndexToStyleIndex;
    }

    void setColumnIndexToStyleIndex(
            HashMap<Integer, Integer> columnIndexToStyleIndex) {
        this.columnIndexToStyleIndex = columnIndexToStyleIndex;
        spreadsheet.getElement().setPropertyJson("columnIndexToStyleIndex",
                Serializer.toJson(columnIndexToStyleIndex));
    }

    Set<Integer> getLockedColumnIndexes() {
        return lockedColumnIndexes;
    }

    void setLockedColumnIndexes(Set<Integer> lockedColumnIndexes) {
        this.lockedColumnIndexes = lockedColumnIndexes;
        spreadsheet.getElement().setPropertyJson("lockedColumnIndexes",
                Serializer.toJson(lockedColumnIndexes));
    }

    Set<Integer> getLockedRowIndexes() {
        return lockedRowIndexes;
    }

    void setLockedRowIndexes(Set<Integer> lockedRowIndexes) {
        this.lockedRowIndexes = lockedRowIndexes;
        spreadsheet.getElement().setPropertyJson("lockedRowIndexes",
                Serializer.toJson(lockedRowIndexes));
    }

    void setShiftedCellBorderStyles(ArrayList<String> shiftedCellBorderStyles) {
        spreadsheet.getElement().setPropertyJson("shiftedCellBorderStyles",
                Serializer.toJson(shiftedCellBorderStyles));
    }

    HashMap<Integer, String> getConditionalFormattingStyles() {
        return conditionalFormattingStyles;
    }

    void setConditionalFormattingStyles(
            HashMap<Integer, String> conditionalFormattingStyles) {
        this.conditionalFormattingStyles = conditionalFormattingStyles;
        spreadsheet.getElement().setPropertyJson("conditionalFormattingStyles",
                Serializer.toJson(conditionalFormattingStyles));
    }
}
