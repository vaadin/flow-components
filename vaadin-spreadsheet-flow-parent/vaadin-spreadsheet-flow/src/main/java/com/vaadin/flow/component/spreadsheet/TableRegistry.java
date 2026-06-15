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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Holds the set of {@link SpreadsheetTable}s registered with a
 * {@link Spreadsheet} and the per-sheet-change latch used to defer table-reload
 * work to a single pass.
 */
@SuppressWarnings("serial")
final class TableRegistry implements Serializable {

    private final HashSet<SpreadsheetTable> tables = new HashSet<>();
    private boolean tablesLoaded;

    void add(SpreadsheetTable table) {
        tables.add(table);
    }

    void remove(SpreadsheetTable table) {
        tables.remove(table);
    }

    HashSet<SpreadsheetTable> getAll() {
        return tables;
    }

    List<SpreadsheetTable> getForSheet(Sheet sheet) {
        List<SpreadsheetTable> out = new ArrayList<>();
        for (SpreadsheetTable table : tables) {
            if (table.getSheet().equals(sheet)) {
                out.add(table);
            }
        }
        return out;
    }

    boolean isLoadedForActiveSheet() {
        return tablesLoaded;
    }

    void markLoadedForActiveSheet() {
        this.tablesLoaded = true;
    }

    void markNeedsReload() {
        this.tablesLoaded = false;
    }
}
