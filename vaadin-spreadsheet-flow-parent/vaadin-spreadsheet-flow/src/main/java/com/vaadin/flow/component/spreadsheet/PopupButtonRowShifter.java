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
 * new keys and puts the button back.
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
     * Moves the buttons along with their rows after
     * {@code Sheet.shiftRows(startRow, endRow, n)}, and drops the buttons of
     * the rows the shift overwrote. Rows are 0-based.
     */
    void shift(int startRow, int endRow, int n) {
        if (registry.isEmpty()) {
            return;
        }
        Map<CellReference, PopupButton> updated = new HashMap<>();
        for (PopupButton button : registry.values()) {
            CellReference cell = button.getCellReference();
            binding.detach(button);
            int newRow = RowShiftOrchestrator.rowAfterShift(cell.getRow(),
                    startRow, endRow, n);
            if (newRow == RowShiftOrchestrator.OVERWRITTEN) {
                // Its row is gone, and the button with it.
                continue;
            }
            if (newRow != cell.getRow()) {
                CellReference newCell = new CellReference(
                        spreadsheet.getActiveSheet().getSheetName(), newRow,
                        cell.getCol(), true, true);
                button.setCellReference(newCell);
                updated.put(newCell, button);
            } else {
                updated.put(cell, button);
            }
        }
        registry.replaceAll(updated);
        // Every button was detached above because its row index was stale.
        // The ones that survived have a current one now, so they go back -
        // without this the client is simply left without them.
        for (PopupButton button : updated.values()) {
            binding.attach(button);
        }
    }
}
