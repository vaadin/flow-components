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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Per-sheet set of cell-keys whose user-entered formulas the server has flagged
 * as invalid. The keys use the {@link SpreadsheetUtil#toKey} format
 * ({@code "col,row"} 1-based) and the map is keyed by the POI active-sheet
 * index so the marks survive an active-sheet switch.
 *
 * <p>
 * Three update concerns:
 *
 * <ul>
 * <li>{@link #mark} / {@link #isMarked} / {@link #removeMark} — single-cell
 * lookups against the active sheet.</li>
 * <li>{@link #shiftActiveSheetRows} — re-keys the active sheet's marks when a
 * row-shift moves their row positions. Marks whose row is removed are dropped;
 * marks within the shifted range have their row index translated. Marks outside
 * the range stay as-is.</li>
 * <li>{@link #clearAll} — flush every sheet's marks. Called from the full-sheet
 * reload path.</li>
 * </ul>
 *
 * <p>
 * Holds a {@link Spreadsheet} reference to read the active sheet index via
 * {@code spreadsheet.getWorkbook().getActiveSheetIndex()}.
 */
final class InvalidFormulaTracker implements Serializable {

    private final Spreadsheet spreadsheet;
    private final Map<Integer, HashSet<String>> invalidFormulas = new HashMap<>();

    InvalidFormulaTracker(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void mark(int col, int row) {
        int activeSheetIndex = activeSheetIndex();
        if (!invalidFormulas.containsKey(activeSheetIndex)) {
            invalidFormulas.put(activeSheetIndex, new HashSet<>());
        }
        invalidFormulas.get(activeSheetIndex)
                .add(SpreadsheetUtil.toKey(col, row));
    }

    boolean isMarked(int col, int row) {
        int activeSheetIndex = activeSheetIndex();
        if (invalidFormulas.containsKey(activeSheetIndex)) {
            return invalidFormulas.get(activeSheetIndex)
                    .contains(SpreadsheetUtil.toKey(col, row));
        }
        return false;
    }

    void removeMark(int col, int row) {
        int activeSheetIndex = activeSheetIndex();
        if (invalidFormulas.containsKey(activeSheetIndex)) {
            invalidFormulas.get(activeSheetIndex)
                    .remove(SpreadsheetUtil.toKey(col, row));
        }
    }

    /**
     * Re-key marks on the active sheet after a {@code shiftRows(first, last,
     * n)} operation. Marks whose row is in {@code [first, first + n - 1]} (when
     * {@code n > 0}) get dropped — those rows were removed. Marks whose row is
     * in {@code [first, last]} get translated by {@code n}. Marks outside the
     * range stay as-is.
     */
    void shiftActiveSheetRows(int first, int last, int n) {
        int activeSheetIndex = activeSheetIndex();
        HashSet<String> original = invalidFormulas.get(activeSheetIndex);
        if (original == null) {
            return;
        }
        HashSet<String> updated = new HashSet<>();
        for (String key : original) {
            int row = SpreadsheetUtil.getRowFromKey(key) - 1;
            int col = SpreadsheetUtil.getColumnIndexFromKey(key) - 1;
            if (rowWasRemoved(row, first, n)) {
                // do nothing -> will be removed
            } else if (numberOfRowsAboveWasChanged(row, last, first)) {
                // the number of rows above has changed -> update the row
                // index
                updated.add(SpreadsheetUtil.toKey(col + 1, row + n + 1));
            } else {
                updated.add(key);
            }
        }
        original.clear();
        invalidFormulas.put(activeSheetIndex, updated);
    }

    void clearAll() {
        invalidFormulas.clear();
    }

    private int activeSheetIndex() {
        return spreadsheet.getWorkbook().getActiveSheetIndex();
    }

    private static boolean numberOfRowsAboveWasChanged(int row, int last,
            int first) {
        return first <= row && row <= last;
    }

    private static boolean rowWasRemoved(int row, int first, int n) {
        return n < 0 && first + n < row && row <= first;
    }
}
