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
 * The sheet shown, as the classes that work on it read it, and nothing else of
 * the component: the rows and columns it has, which of them are hidden, and how
 * many are frozen.
 */
final class SheetView implements Serializable {

    private final Spreadsheet spreadsheet;

    SheetView(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /** The rows the sheet has. */
    int rows() {
        return spreadsheet.getRows();
    }

    /** The columns the sheet has. */
    int columns() {
        return spreadsheet.getColumns();
    }

    /** Whether the row is hidden, 0-based. */
    boolean isRowHidden(int row) {
        return spreadsheet.isRowHidden(row);
    }

    /** Whether the column is hidden, 0-based. */
    boolean isColumnHidden(int column) {
        return spreadsheet.isColumnHidden(column);
    }

    /** The rows frozen at the top. */
    int frozenRows() {
        return spreadsheet.getLastFrozenRow();
    }

    /** The columns frozen at the left. */
    int frozenColumns() {
        return spreadsheet.getLastFrozenColumn();
    }
}
