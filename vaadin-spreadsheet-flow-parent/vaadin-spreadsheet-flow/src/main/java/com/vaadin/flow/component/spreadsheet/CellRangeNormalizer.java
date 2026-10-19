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

import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

/**
 * Builds a {@link CellRangeAddress} from a user-facing input (an address string
 * like {@code "B3:C5"} or a quartet of 1-based coordinates) and clamps the
 * result to the active sheet's row / column extents.
 *
 * <p>
 * Two normalization concerns are bundled here because both methods reduce down
 * to the same shape: order the two corners, then clamp each to the sheet's
 * max-row / max-column. The 0-based string overload clamps with
 * {@code getRows() - 1} / {@code getColumns() - 1} directly. The 1-based int
 * overload clamps with {@code getRows()} / {@code getColumns()} then subtracts
 * one when constructing the {@code CellRangeAddress}. The two paths arrive at
 * the same observable result, but the inner arithmetic differs.
 *
 * <p>
 * Holds a {@link Spreadsheet} reference for the {@code getRows()} /
 * {@code getColumns()} reads only; no mutation.
 */
final class CellRangeNormalizer implements Serializable {

    private final Spreadsheet spreadsheet;

    CellRangeNormalizer(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    /**
     * Parse {@code "<from>:<to>"} into a {@link CellReference} pair, reorder
     * the corners so the result is min-to-max, and clamp to the active sheet's
     * last row / last column.
     */
    CellRangeAddress fromAddressString(String addressString) {
        final String[] split = addressString.split(":");
        final CellReference cr1 = new CellReference(split[0]);
        final CellReference cr2 = new CellReference(split[1]);
        int r1 = cr1.getRow() > cr2.getRow() ? cr2.getRow() : cr1.getRow();
        int r2 = cr1.getRow() > cr2.getRow() ? cr1.getRow() : cr2.getRow();
        int c1 = cr1.getCol() > cr2.getCol() ? cr2.getCol() : cr1.getCol();
        int c2 = cr1.getCol() > cr2.getCol() ? cr1.getCol() : cr2.getCol();
        if (r1 >= spreadsheet.getRows()) {
            r1 = spreadsheet.getRows() - 1;
        }
        if (r2 >= spreadsheet.getRows()) {
            r2 = spreadsheet.getRows() - 1;
        }
        if (c1 >= spreadsheet.getColumns()) {
            c1 = spreadsheet.getColumns() - 1;
        }
        if (c2 >= spreadsheet.getColumns()) {
            c2 = spreadsheet.getColumns() - 1;
        }
        return new CellRangeAddress(r1, r2, c1, c2);
    }

    /**
     * Build a {@link CellRangeAddress} from 1-based start / end coordinates.
     * Reorders the corners so the result is min-to-max, clamps each coordinate
     * to {@code getRows()} / {@code getColumns()}, then subtracts one on emit
     * to land in POI's 0-based space.
     */
    CellRangeAddress fromCoordinates(int row1, int col1, int row2, int col2) {
        int r1 = row1 > row2 ? row2 : row1;
        int r2 = row1 > row2 ? row1 : row2;
        int c1 = col1 > col2 ? col2 : col1;
        int c2 = col1 > col2 ? col1 : col2;
        if (r1 >= spreadsheet.getRows()) {
            r1 = spreadsheet.getRows();
        }
        if (r2 >= spreadsheet.getRows()) {
            r2 = spreadsheet.getRows();
        }
        if (c1 >= spreadsheet.getColumns()) {
            c1 = spreadsheet.getColumns();
        }
        if (c2 >= spreadsheet.getColumns()) {
            c2 = spreadsheet.getColumns();
        }
        return new CellRangeAddress(r1 - 1, r2 - 1, c1 - 1, c2 - 1);
    }
}
