/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ss.usermodel.Cell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The width a cell's value has to fit in: its column's, or, in a merged region,
 * all of the region's columns'.
 */
class MergedCellWidthTest {

    private Spreadsheet spreadsheet;

    @BeforeEach
    void setUp() {
        spreadsheet = new Spreadsheet();
        spreadsheet.setColumnWidth(0, 50);
        spreadsheet.setColumnWidth(1, 70);
        spreadsheet.setColumnWidth(2, 90);
        spreadsheet.setColumnWidth(3, 110);
    }

    private int widthOf(Cell cell) {
        return spreadsheet.getCellValueManager().getCellWidth(cell);
    }

    @Test
    void aCellOutsideAnyMerge_hasItsColumnsWidth() {
        Cell b2 = spreadsheet.createCell(1, 1, "value");
        spreadsheet.addMergedRegion(0, 0, 0, 2);

        assertEquals(spreadsheet.getColW()[1], widthOf(b2));
    }

    @Test
    void theCellAMergeStartsAt_hasTheWidthOfAllItsColumns() {
        Cell a1 = spreadsheet.createCell(0, 0, "value");
        spreadsheet.addMergedRegion(0, 0, 1, 2);

        int[] colW = spreadsheet.getColW();
        assertEquals(colW[0] + colW[1] + colW[2], widthOf(a1));
    }

    @Test
    void aCellInsideAMerge_hasTheWidthOfAllItsColumns() {
        spreadsheet.addMergedRegion(1, 1, 2, 3);
        Cell c3 = spreadsheet.createCell(2, 2, "value");

        int[] colW = spreadsheet.getColW();
        assertEquals(colW[1] + colW[2] + colW[3], widthOf(c3));
    }

    @Test
    void aCellBesideAMerge_hasItsColumnsWidth() {
        spreadsheet.addMergedRegion(0, 0, 0, 1);
        Cell c1 = spreadsheet.createCell(0, 2, "value");

        assertEquals(spreadsheet.getColW()[2], widthOf(c1));
    }

    @Test
    void aMergeRemoved_leavesTheCellItsColumnsWidth() {
        Cell a1 = spreadsheet.createCell(0, 0, "value");
        spreadsheet.addMergedRegion(0, 0, 0, 2);
        spreadsheet.removeMergedRegion(0);

        assertEquals(spreadsheet.getColW()[0], widthOf(a1));
    }
}
