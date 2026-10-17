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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

/**
 * Orchestrates a POI row-shift on the active sheet and propagates the
 * server-side bookkeeping that POI doesn't know about: cached row heights,
 * hidden-row indexes, cell styles for shifted borders, the formula and
 * conditional-formatting evaluator caches, the value-manager's client-cache row
 * mapping, the selection address, the client's list of merged regions, popup
 * buttons rooted in shifted or overwritten rows, and the invalid-formula
 * tracker's per-cell key set.
 *
 * <p>
 * Called from the two public {@code shiftRows} overloads on
 * {@code Spreadsheet}.
 *
 * <p>
 * The orchestrator carries no state of its own; it holds a {@link Spreadsheet}
 * reference for read/write into the host's many collaborator handles
 * ({@code valueManager}, {@code styler}, {@code sheetOverlayManager},
 * {@code selectionManager}, {@code viewportBounds},
 * {@code popupButtonRegistry}, {@code invalidFormulaTracker}).
 */
final class RowShiftOrchestrator implements Serializable {

    private final Spreadsheet spreadsheet;

    RowShiftOrchestrator(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void shift(int startRow, int endRow, int n, boolean copyRowHeight,
            boolean resetOriginalRowHeight) {
        Sheet sheet = spreadsheet.getActiveSheet();
        sheet.shiftRows(startRow, endRow, n, copyRowHeight,
                resetOriginalRowHeight);
        spreadsheet.getFormulaEvaluator().clearAllCachedResultValues();
        spreadsheet.getConditionalFormattingEvaluator().clearAllCachedValues();
        // The rows moved, where they were and where they went.
        int firstAffectedRow = n < 0 ? startRow + n : startRow;
        int lastAffectedRow = n < 0 ? endRow : endRow + n;
        // The rows moved with their heights, whether hidden or not, and the
        // rows they left are empty.
        spreadsheet.sizes().readRows(firstAffectedRow, lastAffectedRow);
        // POI moves and removes the merged regions itself, and
        // updateMergedRegions() below passes its result to the client.
        spreadsheet.popupButtons().shiftRows(startRow, endRow, n);
        spreadsheet.invalidFormulas().shiftActiveSheetRows(startRow, endRow, n);
        spreadsheet.getCellValueManager().rowsChanged(firstAffectedRow,
                lastAffectedRow);

        // Sends the cells and reloads the ones shown. Shifting moves cells,
        // and an editor is cached against a cell key — one left in place
        // would end up on the wrong cell.
        spreadsheet.updateMarkedCellsRecreatingEditors();
        spreadsheet.updateMergedRegions();
        // Once the merged regions are where POI moved them: the cells draw
        // their borders on the cells before them, and the rows hidden moved.
        spreadsheet.getSpreadsheetStyleFactory().reloadBorders();

        CellReference selectedCellReference = spreadsheet
                .getCellSelectionManager().getSelectedCellReference();
        if (selectedCellReference != null) {
            if (selectedCellReference.getRow() >= firstAffectedRow
                    && selectedCellReference.getRow() <= lastAffectedRow) {
                spreadsheet.getCellSelectionManager().onSheetAddressChanged(
                        selectedCellReference.formatAsString(), false);
            }
        }
    }

    /**
     * The value of {@link #rowAfterShift} for a row whose content is gone.
     */
    static final int OVERWRITTEN = -1;

    /**
     * Where the content of a row is after
     * {@code Sheet.shiftRows(startRow, endRow, n)}. The shifted rows move by
     * {@code n}. The rows they land on that are not shifted themselves are
     * overwritten: POI removes their content. Every other row stays where it
     * is. Rows are 0-based.
     *
     * @return the row the content is on after the shift, or
     *         {@link #OVERWRITTEN}
     */
    static int rowAfterShift(int row, int startRow, int endRow, int n) {
        if (startRow <= row && row <= endRow) {
            return row + n;
        }
        if (startRow + n <= row && row <= endRow + n) {
            return OVERWRITTEN;
        }
        return row;
    }
}
