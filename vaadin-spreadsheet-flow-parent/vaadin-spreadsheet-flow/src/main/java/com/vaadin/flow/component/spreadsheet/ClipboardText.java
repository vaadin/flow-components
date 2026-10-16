/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import java.util.ArrayList;
import java.util.List;

/**
 * Cells as spreadsheets put them on the clipboard: cells separated by tabs,
 * rows by line breaks, and a cell that holds a tab, a line break or a double
 * quote written in double quotes, with its own quotes doubled.
 * <p>
 * Split on line breaks and tabs alone, a cell with a line break in it was
 * pasted as two rows.
 */
final class ClipboardText {

    private ClipboardText() {
    }

    /** The value of a cell, by 0-based row and column; null for none. */
    @FunctionalInterface
    interface Values {
        String at(int row, int column);
    }

    /**
     * Writes cells the way {@link #parse(String)} reads them, as the client
     * writes the cells it copies. No line break after the last row.
     */
    static String write(int rows, int columns, Values values) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            if (row > 0) {
                text.append('\n');
            }
            for (int column = 0; column < columns; column++) {
                if (column > 0) {
                    text.append('\t');
                }
                String value = values.at(row, column);
                if (value != null) {
                    appendCell(text, value);
                }
            }
        }
        return text.toString();
    }

    private static void appendCell(StringBuilder text, String value) {
        if (value.indexOf('\t') < 0 && value.indexOf('\n') < 0
                && value.indexOf('\r') < 0 && value.indexOf('"') < 0) {
            text.append(value);
        } else {
            text.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
    }

    /**
     * The rows of the given text, each a list of cell values. A line break at
     * the very end does not start another row.
     */
    static List<List<String>> parse(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        int i = 0;
        int length = text.length();
        while (i < length) {
            StringBuilder cell = new StringBuilder();
            i = readCell(text, i, cell);
            row.add(cell.toString());
            if (i >= length) {
                break;
            }
            char separator = text.charAt(i);
            if (separator == '\t') {
                i++;
                if (i == length) {
                    // A tab at the very end ends an empty last cell.
                    row.add("");
                }
                continue;
            }
            // A line break: \r\n, \n or \r.
            i += separator == '\r' && i + 1 < length
                    && text.charAt(i + 1) == '\n' ? 2 : 1;
            rows.add(row);
            row = new ArrayList<>();
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }
        return rows;
    }

    /**
     * Reads one cell from {@code start} into {@code cell} and returns where it
     * ends: at the tab or line break after it, or at the end of the text.
     */
    private static int readCell(String text, int start, StringBuilder cell) {
        if (start < text.length() && text.charAt(start) == '"') {
            int end = readQuoted(text, start + 1, cell);
            if (end >= 0) {
                // Anything between the closing quote and the next separator
                // belongs to the cell too.
                return readPlain(text, end, cell);
            }
            // Never closed: the quote is just a character.
            cell.setLength(0);
        }
        return readPlain(text, start, cell);
    }

    /**
     * Reads a quoted cell's text up to its closing quote, and returns the index
     * after that quote, or -1 when there is none.
     */
    private static int readQuoted(String text, int start, StringBuilder cell) {
        int i = start;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '"') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                    cell.append('"');
                    i += 2;
                    continue;
                }
                return i + 1;
            }
            cell.append(c);
            i++;
        }
        return -1;
    }

    private static int readPlain(String text, int start, StringBuilder cell) {
        int i = start;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\t' || c == '\n' || c == '\r') {
                break;
            }
            cell.append(c);
            i++;
        }
        return i;
    }
}
