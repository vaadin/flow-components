/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Reads a contiguous run of cell values out of a POI row or column, stopping at
 * the first cell that doesn't match the requested type (numeric or string), is
 * {@code null}, or sits on a missing row.
 *
 * <p>
 * Pure POI reads — no {@link Spreadsheet} dependency, no mutations. Used by
 * {@link RangeSequenceDetector} to feed the arithmetic-sequence detection of
 * {@link SequenceIncrement}.
 *
 * <p>
 * Rows and columns are 0-based, as POI numbers them.
 */
final class SequenceValueExtractor {

    private SequenceValueExtractor() {
    }

    /**
     * Numeric values from {@code row} between columns {@code c1} and
     * {@code c2}. The returned array has length {@code c2 - c1 + 1}; entries
     * after the first non-numeric or null cell are left as {@code null}.
     */
    static Double[] extractRowNumeric(Row row, int c1, int c2) {
        Double[] result = new Double[c2 - c1 + 1];
        for (int i = c1; i <= c2; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                result[i - c1] = cell.getNumericCellValue();
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * String values from {@code row} between columns {@code c1} and {@code c2}.
     * The returned array has length {@code c2 - c1 + 1}; entries after the
     * first non-string or null cell are left as {@code null}.
     */
    static String[] extractRowString(Row row, int c1, int c2) {
        String[] result = new String[c2 - c1 + 1];
        for (int i = c1; i <= c2; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                result[i - c1] = cell.getStringCellValue();
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * Numeric values from column {@code columnIndex} of {@code activeSheet}
     * between rows {@code r1} and {@code r2}. Length {@code r2 - r1 + 1}; stops
     * at the first missing row, missing cell, or non-numeric cell.
     */
    static Double[] extractColumnNumeric(Sheet activeSheet, int columnIndex,
            int r1, int r2) {
        Double[] result = new Double[r2 - r1 + 1];
        for (int i = r1; i <= r2; i++) {
            Row row = activeSheet.getRow(i);
            if (row == null) {
                break;
            }
            Cell cell = row.getCell(columnIndex);
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                result[i - r1] = cell.getNumericCellValue();
            } else {
                break;
            }
        }
        return result;
    }

    /**
     * String values from column {@code columnIndex} of {@code activeSheet}
     * between rows {@code r1} and {@code r2}. Length {@code r2 - r1 + 1}; stops
     * at the first missing row, missing cell, or non-string cell.
     */
    static String[] extractColumnString(Sheet activeSheet, int columnIndex,
            int r1, int r2) {
        String[] result = new String[r2 - r1 + 1];
        for (int i = r1; i <= r2; i++) {
            Row row = activeSheet.getRow(i);
            if (row == null) {
                break;
            }
            Cell cell = row.getCell(columnIndex);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                result[i - r1] = cell.getStringCellValue();
            } else {
                break;
            }
        }
        return result;
    }
}
