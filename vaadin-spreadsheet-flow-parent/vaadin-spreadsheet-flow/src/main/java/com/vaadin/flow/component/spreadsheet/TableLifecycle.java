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
 * Deletes a {@link SpreadsheetTable}: unregisters it, removes every pop-up
 * button it owns (when the table's sheet is currently active), clears filters
 * on filter tables, and clears the table's content.
 */
final class TableLifecycle implements Serializable {

    private final Spreadsheet spreadsheet;

    TableLifecycle(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    void delete(SpreadsheetTable table) {
        // Through the component, so an override of it applies.
        spreadsheet.unregisterTable(table);
        if (!table.isTableSheetCurrentlyActive()) {
            return;
        }
        for (PopupButton popupButton : table.getPopupButtons()) {
            spreadsheet.setPopup(popupButton.getCellReference(), null);
        }
        if (table instanceof SpreadsheetFilterTable filterTable) {
            filterTable.clearAllFilters();
        }
        table.clear();
    }
}
