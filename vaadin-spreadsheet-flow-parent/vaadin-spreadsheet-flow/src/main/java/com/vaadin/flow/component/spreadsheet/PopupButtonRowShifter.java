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
import java.util.Map;

import org.apache.poi.ss.util.CellReference;

/**
 * Remaps pop-up-button cell references when rows are inserted, removed, or
 * shifted on the active sheet. Detaches every existing button from the DOM
 * first (its row index is stale), recomputes the new cell reference, and either
 * drops the button (if its row was removed) or rebuilds the registry with the
 * new keys.
 */
final class PopupButtonRowShifter implements Serializable {

    private final Spreadsheet spreadsheet;
    private final PopupButtonRegistry registry;
    private final PopupButtonBinding binding;

    PopupButtonRowShifter(Spreadsheet spreadsheet, PopupButtonRegistry registry,
            PopupButtonBinding binding) {
        this.spreadsheet = spreadsheet;
        this.registry = registry;
        this.binding = binding;
    }

    /**
     * Shift cell references after a row-insert / row-delete. {@code first} and
     * {@code last} bound the affected range; {@code n} is the row-offset to
     * apply to surviving buttons.
     */
    void shift(int first, int last, int n) {
        if (registry.isEmpty()) {
            return;
        }
        Map<CellReference, PopupButton> updated = new HashMap<>();
        for (PopupButton button : registry.values()) {
            CellReference cell = button.getCellReference();
            binding.detach(button);
            int row = cell.getRow();
            if (rowWasRemoved(row, first, n)) {
                // do nothing -> will be removed
            } else if (numberOfRowsAboveWasChanged(row, last, first)) {
                int newRow = row + n;
                int col = cell.getCol();
                CellReference newCell = new CellReference(
                        spreadsheet.getActiveSheet().getSheetName(), newRow,
                        col, true, true);
                button.setCellReference(newCell);
                updated.put(newCell, button);
            } else {
                updated.put(cell, button);
            }
        }
        registry.replaceAll(updated);
    }

    private static boolean rowWasRemoved(int row, int first, int n) {
        return n < 0 && row >= first && row < first - n;
    }

    private static boolean numberOfRowsAboveWasChanged(int row, int last,
            int first) {
        return row >= first && row > last;
    }
}
