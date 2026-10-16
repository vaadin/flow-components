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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.ref.WeakReference;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.tests.TestHelper;
import com.vaadin.tests.MockUIExtension;

/**
 * Columns autofitted: as wide as their text, with room for the button of a
 * filter in them, and as wide when the workbook is shown again. A column
 * autofitted is autofitted again when a filter is put over it, unless it was
 * resized since.
 */
class SpreadsheetColumnAutofitTest {

    /** The width of a filter button, with the space before it. */
    private static final int FILTER_BUTTON = 16;

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private XSSFWorkbook workbook;
    private Spreadsheet spreadsheet;

    @BeforeEach
    void show() {
        // Columns A and B alike from row 3 on, and C of shorter text.
        workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        String[] texts = { "Header", "a longer value", "value" };
        for (int i = 0; i < texts.length; i++) {
            Row row = sheet.createRow(2 + i);
            row.createCell(0).setCellValue(texts[i]);
            row.createCell(1).setCellValue(texts[i]);
            row.createCell(2).setCellValue("x");
        }
        spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
    }

    private void putFilterOver(String range) {
        spreadsheet.registerTable(new SpreadsheetFilterTable(spreadsheet,
                CellRangeAddress.valueOf(range)));
    }

    private int shownAgain(int column) {
        return new Spreadsheet(workbook).getColW()[column];
    }

    @Test
    void aColumnAutofitted_isAsWideAsItsText() {
        spreadsheet.autofitColumn(0);
        spreadsheet.autofitColumn(2);

        int width = spreadsheet.getColW()[0];
        assertTrue(width > spreadsheet.getColW()[2],
                "wider than a column of shorter text");
        assertEquals(width, shownAgain(0), "shown again");
    }

    @Test
    void aColumnWithAFilterButton_isAutofittedWithRoomForIt() {
        putFilterOver("B3:B5");

        spreadsheet.autofitColumn(0);
        spreadsheet.autofitColumn(1);

        int width = spreadsheet.getColW()[1];
        assertEquals(spreadsheet.getColW()[0] + FILTER_BUTTON, width);
        assertEquals(width, shownAgain(1), "shown again");
    }

    @Test
    void aColumnBesideAFilter_isAutofittedWithoutRoomForItsButton() {
        putFilterOver("C3:C5");

        spreadsheet.autofitColumn(0);
        spreadsheet.autofitColumn(1);

        assertEquals(spreadsheet.getColW()[0], spreadsheet.getColW()[1]);
    }

    @Test
    void aColumnWithTheButtonOfATableNotFiltered_isAutofittedWithoutRoomForIt() {
        spreadsheet.registerTable(new SpreadsheetTable(spreadsheet,
                CellRangeAddress.valueOf("B3:B5")));

        spreadsheet.autofitColumn(0);
        spreadsheet.autofitColumn(1);

        assertEquals(spreadsheet.getColW()[0], spreadsheet.getColW()[1]);
    }

    @Test
    void anAutofittedColumn_isAutofittedAgain_whenAFilterIsPutOverIt() {
        spreadsheet.autofitColumn(0);
        spreadsheet.autofitColumn(1);

        putFilterOver("B3:B5");

        int width = spreadsheet.getColW()[1];
        assertEquals(spreadsheet.getColW()[0] + FILTER_BUTTON, width);
        assertEquals(width, shownAgain(1), "shown again");
    }

    @Test
    void aColumnAutofittedWithAFilter_isAutofittedAgain_whenAFilterIsPutOverItAgain() {
        putFilterOver("B3:B5");
        spreadsheet.autofitColumn(1);
        // Longer text in A and B alike, for B to be autofitted to again.
        for (int column = 0; column < 2; column++) {
            workbook.getSheetAt(0).getRow(3).getCell(column)
                    .setCellValue("a value longer than the others");
        }
        spreadsheet.autofitColumn(0);

        putFilterOver("B3:B5");

        assertEquals(spreadsheet.getColW()[0] + FILTER_BUTTON,
                spreadsheet.getColW()[1]);
    }

    @Test
    void anAutofittedColumnResizedSince_isNotAutofittedAgain() {
        spreadsheet.autofitColumn(1);
        spreadsheet.setColumnWidth(1, 200);

        putFilterOver("B3:B5");

        assertEquals(200, spreadsheet.getColW()[1]);
    }

    @Test
    void aColumnNeverAutofitted_keepsItsWidth_whenAFilterIsPutOverIt() {
        int width = spreadsheet.getColW()[1];

        putFilterOver("B3:B5");

        assertEquals(width, spreadsheet.getColW()[1]);
    }

    @Test
    void anAutofittedColumn_isAutofittedAgain_afterTheGarbageIsCollected() {
        spreadsheet.autofitColumn(0);
        spreadsheet.autofitColumn(1);
        collectGarbage();

        putFilterOver("B3:B5");

        assertEquals(spreadsheet.getColW()[0] + FILTER_BUTTON,
                spreadsheet.getColW()[1]);
    }

    @Test
    void aColumnAutofittedInAnotherWorkbook_isNotAutofitted_whenAFilterIsPutOverIt() {
        spreadsheet.autofitColumn(1);
        int width = spreadsheet.getColW()[1];
        // A workbook of a sheet of the same name, with a column B as wide.
        XSSFWorkbook other = new XSSFWorkbook();
        Sheet sheet = other.createSheet(workbook.getSheetName(0));
        for (int row = 2; row < 5; row++) {
            sheet.createRow(row).createCell(1).setCellValue("x");
        }
        sheet.setColumnWidth(1, SpreadsheetUtil.columnWidthUnits(sheet, width));
        spreadsheet.setWorkbook(other);

        putFilterOver("B3:B5");

        assertEquals(width, spreadsheet.getColW()[1]);
    }

    /** Collects the garbage: what is held only weakly is let go. */
    private static void collectGarbage() {
        WeakReference<Object> weak = new WeakReference<>(new Object());
        for (int i = 0; weak.get() != null && i < 100; i++) {
            System.gc();
        }
        assertNull(weak.get(), "the garbage is collected");
    }

    @Test
    void anAutofitOfTheUser_isUndone() {
        int width = spreadsheet.getColW()[0];
        // The client numbers the columns from 1.
        TestHelper.fireClientEvent(spreadsheet, "onColumnAutofit",
                "{\"columnIndex\": 1}");
        assertNotEquals(width, spreadsheet.getColW()[0]);

        TestHelper.fireClientEvent(spreadsheet, "onUndo", "{}");

        assertEquals(width, spreadsheet.getColW()[0]);
        assertEquals(width, shownAgain(0), "shown again");
    }
}
