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
 * <li>{@link #sheetDeleted} — drops the marks of a sheet deleted, and moves
 * those of the sheets after it to their new indexes.</li>
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
     * Moves the active sheet's marks along with their rows after
     * {@code Sheet.shiftRows(startRow, endRow, n)}, and drops the marks of the
     * rows the shift overwrote. Rows are 0-based here; the keys are 1-based.
     */
    void shiftActiveSheetRows(int startRow, int endRow, int n) {
        int activeSheetIndex = activeSheetIndex();
        HashSet<String> original = invalidFormulas.get(activeSheetIndex);
        if (original == null) {
            return;
        }
        HashSet<String> updated = new HashSet<>();
        for (String key : original) {
            int row = SpreadsheetUtil.getRowFromKey(key) - 1;
            int newRow = RowShiftOrchestrator.rowAfterShift(row, startRow,
                    endRow, n);
            if (newRow != RowShiftOrchestrator.OVERWRITTEN) {
                updated.add(SpreadsheetUtil.toKey(
                        SpreadsheetUtil.getColumnIndexFromKey(key),
                        newRow + 1));
            }
        }
        original.clear();
        invalidFormulas.put(activeSheetIndex, updated);
    }

    /**
     * The sheet at the POI index was deleted: its marks go, and those of the
     * sheets after it move with them to the index before.
     */
    void sheetDeleted(int poiIndex) {
        Map<Integer, HashSet<String>> moved = new HashMap<>();
        invalidFormulas.forEach((index, marks) -> {
            if (index < poiIndex) {
                moved.put(index, marks);
            } else if (index > poiIndex) {
                moved.put(index - 1, marks);
            }
        });
        invalidFormulas.clear();
        invalidFormulas.putAll(moved);
    }

    private int activeSheetIndex() {
        return spreadsheet.getWorkbook().getActiveSheetIndex();
    }
}
