/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetTable;
import com.vaadin.tests.MockUIExtension;

/**
 * A workbook whose filter or table covers its header row alone, as one with
 * headers and no data yet has, opens without it, with a warning, and keeps it:
 * a filter table filters the rows under its header. The other tables of its
 * sheet are still shown, and so is one with rows under its header.
 */
class HeaderOnlyFilterTest {

    private static final String HEADER_ONLY_WARNING = "The filter of B2:D2 on"
            + " sheet Sheet0 has no rows under its header row, and is not shown";

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private Spreadsheet spreadsheet;

    /** A sheet whose B2:D2 hold "Head 1" to "Head 3", and B3:D3 values. */
    private static XSSFSheet sheetWithHeaders(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet();
        fillHeadersAndValues(sheet, 1);
        return sheet;
    }

    /** Headers in row 2 and values in row 3, in three columns from this one. */
    private static void fillHeadersAndValues(XSSFSheet sheet, int firstColumn) {
        for (int row = 1; row <= 2; row++) {
            XSSFRow poiRow = sheet.getRow(row) != null ? sheet.getRow(row)
                    : sheet.createRow(row);
            for (int column = firstColumn; column < firstColumn + 3; column++) {
                poiRow.createCell(column).setCellValue(
                        row == 1 ? "Head " + column : "v" + column);
            }
        }
    }

    /** Opens the workbook; returns what the component logged meanwhile. */
    private List<String> open(XSSFWorkbook workbook) {
        try (CapturedLogs.Capture logs = CapturedLogs.capture()) {
            spreadsheet = new Spreadsheet(workbook);
            return logs.entries().stream()
                    .filter(entry -> entry.logger()
                            .startsWith(NoUnexpectedWarnings.COMPONENT))
                    .map(CapturedLogs.Entry::message).toList();
        }
    }

    private List<String> shownTables() {
        return spreadsheet.getTables().stream()
                .map(SpreadsheetTable::getFullTableRegion)
                .map(CellRangeAddress::formatAsString).toList();
    }

    @Test
    @ExpectWarnings
    void aFilterOfTheHeaderRowAlone_opensWithoutIt_andIsKept() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = sheetWithHeaders(workbook);
        sheet.setAutoFilter(CellRangeAddress.valueOf("B2:D2"));

        Assertions.assertEquals(List.of(HEADER_ONLY_WARNING), open(workbook));
        Assertions.assertEquals(List.of(), shownTables());
        Assertions.assertEquals("B2:D2",
                sheet.getCTWorksheet().getAutoFilter().getRef());
    }

    @Test
    @ExpectWarnings
    void aTableOfOneRow_opensWithoutIt_andIsKept() {
        // As a file holds it: POI makes no table of one row itself.
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFTable table = sheetWithHeaders(workbook).createTable(
                new AreaReference("B2:D3", SpreadsheetVersion.EXCEL2007));
        table.getCTTable().setHeaderRowCount(0);
        table.getCTTable().setRef("B2:D2");

        Assertions.assertEquals(List.of(HEADER_ONLY_WARNING), open(workbook));
        Assertions.assertEquals(List.of(), shownTables());
        Assertions.assertEquals("B2:D2", table.getCTTable().getRef());
    }

    @Test
    @ExpectWarnings
    void aFilterOfTheHeaderRowAlone_leavesTheOtherTablesOfItsSheetShown() {
        // Before, the filter's failure ended the loading of the sheet's
        // tables, and the table after it was not shown either.
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = sheetWithHeaders(workbook);
        fillHeadersAndValues(sheet, 5);
        sheet.setAutoFilter(CellRangeAddress.valueOf("B2:D2"));
        sheet.createTable(
                new AreaReference("F2:H3", SpreadsheetVersion.EXCEL2007));

        Assertions.assertEquals(List.of(HEADER_ONLY_WARNING), open(workbook));
        Assertions.assertEquals(List.of("F2:H3"), shownTables());
    }

    @Test
    void aFilterWithARowUnderItsHeader_isShown() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        sheetWithHeaders(workbook)
                .setAutoFilter(CellRangeAddress.valueOf("B2:D3"));

        Assertions.assertEquals(List.of(), open(workbook));
        Assertions.assertEquals(List.of("B2:D3"), shownTables());
    }
}
