/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Each sheet is shown again at the scroll offsets it was left at. The offsets
 * stay with their sheets when other sheets are deleted, hidden or shown again,
 * and the client gets them in the order of the visible sheets.
 */
class SheetScrollMemoryTest {

    /** Sheets A, B and C, with B hidden when asked. */
    private static Spreadsheet spreadsheetWith(boolean bHidden) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("A");
        workbook.createSheet("B");
        workbook.createSheet("C");
        if (bHidden) {
            workbook.setSheetVisibility(1, SheetVisibility.HIDDEN);
        }
        return new Spreadsheet(workbook);
    }

    /**
     * Leaves the active sheet scrolled to the given offsets for the visible
     * sheet at the given index, as a click on its tab does.
     */
    private static void leave(Spreadsheet spreadsheet, int top, int left,
            int toVisibleIndex) {
        spreadsheet.onSheetSelected(toVisibleIndex, left, top);
    }

    /** A at 100/10, B at 200/20 and C at 300/30, with A showing. */
    private static Spreadsheet allThreeScrolled() {
        Spreadsheet spreadsheet = spreadsheetWith(false);
        leave(spreadsheet, 100, 10, 1);
        leave(spreadsheet, 200, 20, 2);
        leave(spreadsheet, 300, 30, 0);
        return spreadsheet;
    }

    private static void assertOffsets(Spreadsheet spreadsheet, int[] tops,
            int[] lefts) {
        assertArrayEquals(tops,
                spreadsheet.workbookModel().verticalScrollPositions.get());
        assertArrayEquals(lefts,
                spreadsheet.workbookModel().horizontalScrollPositions.get());
    }

    @Test
    void sheetsLeft_rememberTheirOffsets() {
        assertOffsets(allThreeScrolled(), new int[] { 100, 200, 300 },
                new int[] { 10, 20, 30 });
    }

    @Test
    void theClientIsSentTheOffsets() {
        Spreadsheet spreadsheet = allThreeScrolled();

        assertEquals("[100,200,300]", spreadsheet.getElement()
                .getProperty("verticalScrollPositions"));
        assertEquals("[10,20,30]", spreadsheet.getElement()
                .getProperty("horizontalScrollPositions"));
    }

    @Test
    void deletingASheet_leavesTheOthersTheirOffsets() {
        // C was given B's offsets.
        Spreadsheet spreadsheet = allThreeScrolled();

        spreadsheet.deleteSheet(1);

        assertOffsets(spreadsheet, new int[] { 100, 300 },
                new int[] { 10, 30 });
    }

    @Test
    void hidingASheet_leavesTheOthersTheirOffsets() {
        Spreadsheet spreadsheet = allThreeScrolled();

        spreadsheet.setSheetHidden(1, SheetVisibility.HIDDEN);

        assertOffsets(spreadsheet, new int[] { 100, 300 },
                new int[] { 10, 30 });
    }

    @Test
    void showingAHiddenSheet_keepsTheOffsetsItHad() {
        Spreadsheet spreadsheet = allThreeScrolled();
        spreadsheet.setSheetHidden(1, SheetVisibility.HIDDEN);
        leave(spreadsheet, 150, 15, 1);

        spreadsheet.setSheetHidden(1, SheetVisibility.VISIBLE);

        assertOffsets(spreadsheet, new int[] { 150, 200, 300 },
                new int[] { 15, 20, 30 });
    }

    @Test
    void leavingASheetShownAfterLoading_remembersItsOffsets() {
        // Threw ArrayIndexOutOfBoundsException: the offsets had a place only
        // for the sheets that were visible when the workbook was loaded.
        Spreadsheet spreadsheet = spreadsheetWith(true);
        spreadsheet.setSheetHidden(1, SheetVisibility.VISIBLE);

        leave(spreadsheet, 0, 0, 2);
        leave(spreadsheet, 300, 30, 0);

        assertOffsets(spreadsheet, new int[] { 0, 0, 300 },
                new int[] { 0, 0, 30 });
    }

    @Test
    void aNewSheet_startsAtTheTop() {
        Spreadsheet spreadsheet = spreadsheetWith(true);
        leave(spreadsheet, 100, 10, 1);

        spreadsheet.createNewSheet("D", 10, 10);

        assertOffsets(spreadsheet, new int[] { 100, 0, 0 },
                new int[] { 10, 0, 0 });
    }

    @Test
    void anotherWorkbook_startsEverySheetAtTheTop() {
        Spreadsheet spreadsheet = allThreeScrolled();
        XSSFWorkbook other = new XSSFWorkbook();
        other.createSheet("X");
        other.createSheet("Y");

        spreadsheet.setWorkbook(other);

        assertOffsets(spreadsheet, new int[] { 0, 0 }, new int[] { 0, 0 });
    }
}
