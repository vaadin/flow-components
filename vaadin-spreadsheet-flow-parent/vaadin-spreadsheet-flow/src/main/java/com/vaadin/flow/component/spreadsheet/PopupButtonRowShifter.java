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
 * Moves the pop-up buttons of the active sheet along with their rows when rows
 * are shifted, and drops those whose rows were overwritten. A button that moves
 * or goes is taken off the client first, where it is still known by its old
 * cell; the refresh after the shift puts the moved ones back on.
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
     * {@code Sheet.shiftRows(startRow, endRow, n)} on the active sheet, and
     * drops the buttons of the rows the shift overwrote. Rows are 0-based.
     */
    void shift(int startRow, int endRow, int n) {
        String sheetName = spreadsheet.getActiveSheet().getSheetName();
        Map<CellReference, PopupButton> updated = new HashMap<>();
        for (PopupButton button : registry.values()) {
            CellReference cell = button.getCellReference();
            int newRow = RowShiftOrchestrator.rowAfterShift(cell.getRow(),
                    startRow, endRow, n);
            if (!sheetName.equals(cell.getSheetName())
                    || newRow == cell.getRow()) {
                updated.put(cell, button);
                continue;
            }
            binding.detach(button);
            if (newRow != RowShiftOrchestrator.OVERWRITTEN) {
                CellReference newCell = new CellReference(sheetName, newRow,
                        cell.getCol(), true, true);
                button.setCellReference(newCell);
                updated.put(newCell, button);
            }
        }
        registry.replaceAll(updated);
    }
}
