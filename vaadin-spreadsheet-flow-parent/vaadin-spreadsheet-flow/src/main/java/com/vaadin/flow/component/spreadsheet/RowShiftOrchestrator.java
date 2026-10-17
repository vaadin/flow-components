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

/**
 * Rows shifted on the active sheet, as the two public {@code shiftRows}
 * overloads of {@code Spreadsheet} shift them. POI moves the rows' cells,
 * heights and merged regions; what is kept for the rows besides moves with
 * them, and the rows changed are shown again, as
 * {@link Spreadsheet#rowsChanged} shows them.
 */
final class RowShiftOrchestrator implements Serializable {

    private final Spreadsheet spreadsheet;

    RowShiftOrchestrator(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void shift(int startRow, int endRow, int n, boolean copyRowHeight,
            boolean resetOriginalRowHeight) {
        spreadsheet.getActiveSheet().shiftRows(startRow, endRow, n,
                copyRowHeight, resetOriginalRowHeight);
        // POI moves the cells, heights and merged regions of the rows; their
        // pop-up buttons and invalid formula marks go with them.
        spreadsheet.popupButtons().shiftRows(startRow, endRow, n);
        spreadsheet.invalidFormulas().shiftActiveSheetRows(startRow, endRow, n);
        // The rows moved, where they were and where they went.
        spreadsheet.rowsChanged(Math.min(startRow, startRow + n),
                Math.max(endRow, endRow + n));
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
