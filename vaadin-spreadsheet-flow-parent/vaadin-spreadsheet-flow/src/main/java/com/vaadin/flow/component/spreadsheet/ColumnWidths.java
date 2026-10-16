/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * The width a column is shown at, for the undo and redo commands, which live in
 * another package: they put a column back at the width it was shown at.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
public final class ColumnWidths {

    private ColumnWidths() {
    }

    /**
     * The width the column is shown at: none when it is hidden, its own width
     * in the sheet, or the default width of the spreadsheet when it has none of
     * its own.
     *
     * @param spreadsheet
     *            the spreadsheet, not {@code null}
     * @param sheet
     *            the sheet of the column, not {@code null}
     * @param column
     *            the column index, 0-based
     * @return the width in pixels
     */
    public static int shown(Spreadsheet spreadsheet, Sheet sheet, int column) {
        return SpreadsheetUtil.columnWidthShown(sheet, column,
                spreadsheet.getDefColW());
    }
}
