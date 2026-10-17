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
 * Decides whether a contiguous run of cells (row slice or column slice) forms
 * an arithmetic progression and, if so, returns its common difference.
 *
 * <p>
 * Glues two pure helpers together: {@link SequenceValueExtractor} reads the
 * typed values out of POI, and {@link SequenceIncrement} computes the
 * increment. This class adds the type dispatch — STRING vs NUMERIC — based on
 * the leading cell, returning {@code null} for any other case (missing row,
 * missing leading cell, FORMULA, BOOLEAN, etc.).
 *
 * <p>
 * Pure static methods; rows and columns are 0-based, as POI numbers them. Used
 * by {@link DirectionalShiftOperator} to drive Excel autofill on a per-row or
 * per-column basis.
 */
final class RangeSequenceDetector {

    private RangeSequenceDetector() {
    }

    /**
     * Common difference of cells in row {@code rIndex} from column {@code c1}
     * to column {@code c2}; {@code null} if the cells don't form a progression
     * or the leading cell isn't STRING / NUMERIC.
     */
    static Double getRowIncrement(Sheet activeSheet, int rIndex, int c1,
            int c2) {
        Row row = activeSheet.getRow(rIndex);
        if (row == null) {
            return null;
        }
        Cell firstCell = row.getCell(c1);
        if (firstCell == null) {
            return null;
        }
        if (firstCell.getCellType() == CellType.STRING) {
            return SequenceIncrement.compute(
                    SequenceValueExtractor.extractRowString(row, c1, c2));
        }
        if (firstCell.getCellType() == CellType.NUMERIC) {
            return SequenceIncrement.compute(
                    SequenceValueExtractor.extractRowNumeric(row, c1, c2));
        }
        return null;
    }

    /**
     * Common difference of cells in column {@code cIndex} from row {@code r1}
     * to row {@code r2}; {@code null} if the cells don't form a progression or
     * the leading cell isn't STRING / NUMERIC.
     */
    static Double getColumnIncrement(Sheet activeSheet, int cIndex, int r1,
            int r2) {
        Row row = activeSheet.getRow(r1);
        if (row == null) {
            return null;
        }
        Cell firstCell = row.getCell(cIndex);
        if (firstCell == null) {
            return null;
        }
        if (firstCell.getCellType() == CellType.STRING) {
            return SequenceIncrement.compute(SequenceValueExtractor
                    .extractColumnString(activeSheet, cIndex, r1, r2));
        }
        if (firstCell.getCellType() == CellType.NUMERIC) {
            return SequenceIncrement.compute(SequenceValueExtractor
                    .extractColumnNumeric(activeSheet, cIndex, r1, r2));
        }
        return null;
    }
}
