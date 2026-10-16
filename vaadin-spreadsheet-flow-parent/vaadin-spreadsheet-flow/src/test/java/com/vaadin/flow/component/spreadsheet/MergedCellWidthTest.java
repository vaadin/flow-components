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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

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

    // POI builds its list of a sheet's merged regions from the sheet's XML on
    // every call, parsing each reference: with 20,000 merged regions, the
    // cells of one screen took seconds.

    /** 100 merged regions, two rows each, from row 11 down. */
    private void mergeManyRegions() {
        for (int i = 0; i < 100; i++) {
            spreadsheet.addMergedRegion(10 + i * 2, 0, 11 + i * 2, 1);
        }
    }

    @Test
    void measuringCells_parsesNoMergedRegions() {
        mergeManyRegions();
        Cell a1 = spreadsheet.createCell(0, 0, "value");
        Cell a11 = spreadsheet.createCell(10, 0, "value");
        int[] colW = spreadsheet.getColW();

        try (MockedStatic<CellRangeAddress> poi = mockStatic(
                CellRangeAddress.class, CALLS_REAL_METHODS)) {
            assertEquals(colW[0], widthOf(a1));
            assertEquals(colW[0] + colW[1], widthOf(a11));

            poi.verify(() -> CellRangeAddress.valueOf(anyString()), never());
        }
    }

    @Test
    void textTooWideForItsColumn_parsesNoMergedRegions() {
        mergeManyRegions();
        HashMap<Integer, Float> ratios = new HashMap<>();
        ratios.put(0, 10f);
        spreadsheet.getCellValueManager().onCellStyleWidthRatioUpdate(ratios);
        Cell a1 = spreadsheet.createCell(0, 0, "a value far too wide");
        Cell a11 = spreadsheet.createCell(10, 0, "a value far too wide");

        try (MockedStatic<CellRangeAddress> poi = mockStatic(
                CellRangeAddress.class, CALLS_REAL_METHODS)) {
            spreadsheet.getCellValueManager().createCellDataForCell(a1);
            assertEquals(0, spreadsheet.getCellValueManager()
                    .createCellDataForCell(a11).overflowWidth);

            poi.verify(() -> CellRangeAddress.valueOf(anyString()), never());
        }
    }

    @Test
    void aCellOnASheetNotShown_hasTheWidthOfItsOwnSheetsMerges() {
        // The spreadsheet keeps the merged regions of the sheet it shows
        // only; a cell of another sheet is measured by that sheet's own.
        org.apache.poi.ss.usermodel.Sheet other = spreadsheet.getWorkbook()
                .createSheet("Other");
        Cell a1 = other.createRow(0).createCell(0);
        a1.setCellValue("value");
        other.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));

        int[] colW = spreadsheet.getColW();
        assertEquals(colW[0] + colW[1] + colW[2], widthOf(a1));
    }
}
