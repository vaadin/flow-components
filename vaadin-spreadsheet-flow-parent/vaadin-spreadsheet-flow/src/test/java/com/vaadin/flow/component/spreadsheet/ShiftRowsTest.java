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

import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.client.MergedRegion;

/**
 * What {@code shiftRows} does to the pop-up buttons, invalid-formula marks and
 * merged regions of the rows it touches.
 * <p>
 * Rows 5 to 9 are shifted, and each of rows 2 to 11 and row 14 has a pop-up
 * button, an invalid-formula mark and a merged region. Each follows its row as
 * POI moves it: the shifted rows move by the offset, the rows they land on that
 * are not shifted themselves are overwritten and lose theirs, and every other
 * row keeps its own. Rows are 0-based here.
 * <p>
 * The column of a mark or a merged region tells the row it started on, so the
 * test can tell a mark that moved from one that stayed. Rows 12 and 13 are left
 * empty, so a button moved there by mistake does not take the place of one that
 * was there.
 */
class ShiftRowsTest {

    private static final int[] ROWS = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 14 };
    private static final int REMOVED = -1;
    private static final int FIRST_REGION_COLUMN = 20;

    private Spreadsheet spreadsheet;
    private final Map<Integer, PopupButton> buttons = new TreeMap<>();

    private void init(Workbook workbook) {
        workbook.createSheet("Sheet1");
        spreadsheet = new Spreadsheet(workbook);
        // Show every cell, so a button that survives is shown.
        spreadsheet.onSheetScroll(1, 1, 30, 50);
        for (int row : ROWS) {
            PopupButton button = new PopupButton();
            spreadsheet.setPopup(row, 0, button);
            buttons.put(row, button);
            // 1-based column and row
            spreadsheet.markInvalidFormula(markColumn(row) + 1, row + 1);
            spreadsheet.addMergedRegion(row, regionColumn(row), row,
                    regionColumn(row) + 1);
        }
    }

    @Test
    void shiftUpByOne_popupButtonsFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, -1);

        assertEquals(shiftedUpByOne(), popupButtonRows());
    }

    @Test
    void shiftUpByOne_invalidFormulaMarksFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, -1);

        assertEquals(shiftedUpByOne(), invalidFormulaRows());
    }

    @Test
    void shiftUpByOne_mergedRegionsFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, -1);

        assertEquals(shiftedUpByOne(), mergedRegionRows());
        assertEquals(shiftedUpByOne(), clientMergedRegionRows());
    }

    @Test
    void shiftUpByOneInAnXlsFile_mergedRegionsFollowTheirRows() {
        init(new HSSFWorkbook());

        spreadsheet.shiftRows(5, 9, -1);

        assertEquals(shiftedUpByOne(), mergedRegionRows());
        assertEquals(shiftedUpByOne(), clientMergedRegionRows());
    }

    @Test
    void shiftUpByTwo_popupButtonsFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, -2);

        assertEquals(shiftedUpByTwo(), popupButtonRows());
    }

    @Test
    void shiftUpByTwo_invalidFormulaMarksFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, -2);

        assertEquals(shiftedUpByTwo(), invalidFormulaRows());
    }

    @Test
    void shiftUpByTwo_mergedRegionsFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, -2);

        assertEquals(shiftedUpByTwo(), mergedRegionRows());
        assertEquals(shiftedUpByTwo(), clientMergedRegionRows());
    }

    @Test
    void shiftDownByTwo_popupButtonsFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, 2);

        assertEquals(shiftedDownByTwo(), popupButtonRows());
    }

    @Test
    void shiftDownByTwo_invalidFormulaMarksFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, 2);

        assertEquals(shiftedDownByTwo(), invalidFormulaRows());
    }

    @Test
    void shiftDownByTwo_mergedRegionsFollowTheirRows() {
        init(new XSSFWorkbook());

        spreadsheet.shiftRows(5, 9, 2);

        assertEquals(shiftedDownByTwo(), mergedRegionRows());
        assertEquals(shiftedDownByTwo(), clientMergedRegionRows());
    }

    /** Row 4 is overwritten by row 5. */
    private static Map<Integer, Integer> shiftedUpByOne() {
        return expected(2, 2, 3, 3, 4, REMOVED, 5, 4, 6, 5, 7, 6, 8, 7, 9, 8,
                10, 10, 11, 11, 14, 14);
    }

    /** Rows 3 and 4 are overwritten by rows 5 and 6. */
    private static Map<Integer, Integer> shiftedUpByTwo() {
        return expected(2, 2, 3, REMOVED, 4, REMOVED, 5, 3, 6, 4, 7, 5, 8, 6, 9,
                7, 10, 10, 11, 11, 14, 14);
    }

    /** Rows 10 and 11 are overwritten by rows 8 and 9. */
    private static Map<Integer, Integer> shiftedDownByTwo() {
        return expected(2, 2, 3, 3, 4, 4, 5, 7, 6, 8, 7, 9, 8, 10, 9, 11, 10,
                REMOVED, 11, REMOVED, 14, 14);
    }

    private static Map<Integer, Integer> expected(int... fromTo) {
        Map<Integer, Integer> expected = new TreeMap<>();
        for (int i = 0; i < fromTo.length; i += 2) {
            expected.put(fromTo[i], fromTo[i + 1]);
        }
        return expected;
    }

    private static int markColumn(int row) {
        return row;
    }

    private static int regionColumn(int row) {
        return FIRST_REGION_COLUMN + 2 * row;
    }

    private static int rowOfRegionColumn(int column) {
        return (column - FIRST_REGION_COLUMN) / 2;
    }

    /** Every row of the fixture, as yet without a place. */
    private static Map<Integer, Integer> allRemoved() {
        Map<Integer, Integer> rows = new TreeMap<>();
        for (int row : ROWS) {
            rows.put(row, REMOVED);
        }
        return rows;
    }

    /**
     * The row each button is shown on now, by the row it was on, or
     * {@link #REMOVED} for one the spreadsheet no longer shows.
     */
    private Map<Integer, Integer> popupButtonRows() {
        Map<Integer, Integer> rows = new TreeMap<>();
        buttons.forEach((row, button) -> {
            boolean shown = spreadsheet.getElement()
                    .equals(button.getElement().getParent());
            rows.put(row, shown ? button.getCellReference().getRow() : REMOVED);
        });
        return rows;
    }

    private Map<Integer, Integer> invalidFormulaRows() {
        Map<Integer, Integer> rows = allRemoved();
        for (int startedOn : ROWS) {
            for (int row = 0; row < spreadsheet.getRows(); row++) {
                if (spreadsheet.isMarkedAsInvalidFormula(
                        markColumn(startedOn) + 1, row + 1)) {
                    assertEquals(REMOVED, rows.put(startedOn, row),
                            "One mark per row");
                }
            }
        }
        return rows;
    }

    private Map<Integer, Integer> mergedRegionRows() {
        Map<Integer, Integer> rows = allRemoved();
        for (CellRangeAddress region : spreadsheet.getActiveSheet()
                .getMergedRegions()) {
            assertEquals(region.getFirstRow(), region.getLastRow(),
                    region + " should still cover one row");
            rows.put(rowOfRegionColumn(region.getFirstColumn()),
                    region.getFirstRow());
        }
        return rows;
    }

    /** The merged regions the client is told about. */
    private Map<Integer, Integer> clientMergedRegionRows() {
        Map<Integer, Integer> rows = allRemoved();
        for (MergedRegion region : spreadsheet.sheetModel().mergedRegions
                .get()) {
            // 1-based
            rows.put(rowOfRegionColumn(region.col1 - 1), region.row1 - 1);
        }
        return rows;
    }
}
