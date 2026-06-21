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
 * Reloads each registered {@link SpreadsheetTable} that belongs to the
 * currently active sheet, and latches the registry so duplicate calls within
 * the same sheet-render cycle are a no-op.
 */
final class TableLoader implements Serializable {

    private final Spreadsheet spreadsheet;
    private final TableRegistry registry;

    TableLoader(Spreadsheet spreadsheet, TableRegistry registry) {
        this.spreadsheet = spreadsheet;
        this.registry = registry;
    }

    void loadForActiveSheet() {
        if (registry.isLoadedForActiveSheet()) {
            return;
        }
        for (SpreadsheetTable table : registry.getAll()) {
            if (table.getSheet().equals(spreadsheet.getActiveSheet())) {
                table.reload();
            }
        }
        registry.markLoadedForActiveSheet();
    }
}
