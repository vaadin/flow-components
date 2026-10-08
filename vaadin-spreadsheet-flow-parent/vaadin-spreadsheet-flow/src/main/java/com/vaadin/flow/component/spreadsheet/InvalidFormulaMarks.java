/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

/**
 * The flag a cell carries when it holds a formula the server could not parse,
 * for the undo and redo commands, which live in another package: they put the
 * flag back with the cell they restore.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public final class InvalidFormulaMarks {

    private InvalidFormulaMarks() {
    }

    /**
     * Whether the cell on the active sheet carries the invalid-formula flag.
     *
     * @param spreadsheet
     *            the spreadsheet, not {@code null}
     * @param col
     *            column index, 1-based
     * @param row
     *            row index, 1-based
     * @return {@code true} when the cell carries the flag
     */
    public static boolean isMarked(Spreadsheet spreadsheet, int col, int row) {
        return spreadsheet.isMarkedAsInvalidFormula(col, row);
    }

    /**
     * Puts the invalid-formula flag on the cell on the active sheet.
     *
     * @param spreadsheet
     *            the spreadsheet, not {@code null}
     * @param col
     *            column index, 1-based
     * @param row
     *            row index, 1-based
     */
    public static void mark(Spreadsheet spreadsheet, int col, int row) {
        spreadsheet.markInvalidFormula(col, row);
    }
}
