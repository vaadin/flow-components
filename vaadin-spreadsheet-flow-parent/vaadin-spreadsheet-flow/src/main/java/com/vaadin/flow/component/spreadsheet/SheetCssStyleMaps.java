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

import com.vaadin.flow.dom.Element;

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
 * Every setter gives the element the value at once, collections as JSON.
 */
final class SheetCssStyleMaps implements Serializable {

    private final ClientProperty<HashMap<Integer, Integer>> rowIndexToStyleIndex;
    private final ClientProperty<HashMap<Integer, Integer>> columnIndexToStyleIndex;
    private final ClientProperty<Set<Integer>> lockedColumnIndexes;
    private final ClientProperty<Set<Integer>> lockedRowIndexes;
    private final ClientProperty<ArrayList<String>> shiftedCellBorderStyles;
    private final ClientProperty<HashMap<Integer, String>> conditionalFormattingStyles;

    SheetCssStyleMaps(Element element) {
        rowIndexToStyleIndex = ClientProperty.json(element,
                "rowIndexToStyleIndex");
        columnIndexToStyleIndex = ClientProperty.json(element,
                "columnIndexToStyleIndex");
        lockedColumnIndexes = ClientProperty.json(element,
                "lockedColumnIndexes");
        lockedRowIndexes = ClientProperty.json(element, "lockedRowIndexes");
        shiftedCellBorderStyles = ClientProperty.json(element,
                "shiftedCellBorderStyles");
        conditionalFormattingStyles = ClientProperty.json(element,
                "conditionalFormattingStyles");
    }

    HashMap<Integer, Integer> getRowIndexToStyleIndex() {
        return rowIndexToStyleIndex.get();
    }

    void setRowIndexToStyleIndex(
            HashMap<Integer, Integer> rowIndexToStyleIndex) {
        this.rowIndexToStyleIndex.set(rowIndexToStyleIndex);
    }

    HashMap<Integer, Integer> getColumnIndexToStyleIndex() {
        return columnIndexToStyleIndex.get();
    }

    void setColumnIndexToStyleIndex(
            HashMap<Integer, Integer> columnIndexToStyleIndex) {
        this.columnIndexToStyleIndex.set(columnIndexToStyleIndex);
    }

    Set<Integer> getLockedColumnIndexes() {
        return lockedColumnIndexes.get();
    }

    void setLockedColumnIndexes(Set<Integer> lockedColumnIndexes) {
        this.lockedColumnIndexes.set(lockedColumnIndexes);
    }

    Set<Integer> getLockedRowIndexes() {
        return lockedRowIndexes.get();
    }

    void setLockedRowIndexes(Set<Integer> lockedRowIndexes) {
        this.lockedRowIndexes.set(lockedRowIndexes);
    }

    void setShiftedCellBorderStyles(ArrayList<String> shiftedCellBorderStyles) {
        this.shiftedCellBorderStyles.set(shiftedCellBorderStyles);
    }

    HashMap<Integer, String> getConditionalFormattingStyles() {
        return conditionalFormattingStyles.get();
    }

    void setConditionalFormattingStyles(
            HashMap<Integer, String> conditionalFormattingStyles) {
        this.conditionalFormattingStyles.set(conditionalFormattingStyles);
    }
}
