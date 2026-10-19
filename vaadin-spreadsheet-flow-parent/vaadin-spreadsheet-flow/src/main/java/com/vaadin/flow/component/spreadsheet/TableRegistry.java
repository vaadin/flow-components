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

import org.apache.poi.ss.formula.FormulaShifter;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * The {@link SpreadsheetTable}s registered with a {@link Spreadsheet}, and
 * their reload: the tables of the sheet shown are reloaded once each time it is
 * shown, not each time its cells are loaded.
 */
final class TableRegistry implements Serializable {

    private final HashSet<SpreadsheetTable> tables = new HashSet<>();
    /**
     * Whether the tables of the sheet shown were reloaded since it was shown.
     */
    private boolean reloaded;

    void add(SpreadsheetTable table) {
        tables.add(table);
    }

    void remove(SpreadsheetTable table) {
        tables.remove(table);
    }

    /** The tables, as {@link Spreadsheet#getTables()} hands them out. */
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

    /** Rows of the sheet shifted: its tables move with them. */
    void shiftRows(Sheet sheet, FormulaShifter shifter, int sheetIndex,
            int startRow, int endRow, int n) {
        for (SpreadsheetTable table : tables) {
            if (table.getSheet().equals(sheet)) {
                table.shiftRows(shifter, sheetIndex, startRow, endRow, n);
            }
        }
    }

    /** Another sheet is shown: its tables are to be reloaded. */
    void markNeedsReload() {
        reloaded = false;
    }

    /** Reloads the tables of the sheet shown, unless they were since it was. */
    void reloadForSheet(Sheet shown) {
        if (reloaded) {
            return;
        }
        for (SpreadsheetTable table : tables) {
            if (table.getSheet().equals(shown)) {
                table.reload();
            }
        }
        reloaded = true;
    }
}
