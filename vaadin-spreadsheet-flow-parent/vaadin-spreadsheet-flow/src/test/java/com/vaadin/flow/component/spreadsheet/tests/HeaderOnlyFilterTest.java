/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.tests.MockUIExtension;

/**
 * A workbook whose filter or table covers its header row alone, as one with
 * headers and no data yet has, opens without it, and keeps it: a filter table
 * filters the rows under its header. One with rows under its header is shown.
 */
class HeaderOnlyFilterTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    /** A sheet whose B2:D2 hold "Head 1" to "Head 3", and B3:D3 values. */
    private static XSSFSheet sheetWithHeaders(XSSFWorkbook workbook) {
        XSSFSheet sheet = workbook.createSheet();
        for (int row = 1; row <= 2; row++) {
            var poiRow = sheet.createRow(row);
            for (int column = 1; column <= 3; column++) {
                poiRow.createCell(column).setCellValue(
                        row == 1 ? "Head " + column : "v" + column);
            }
        }
        return sheet;
    }

    @Test
    void aFilterOfTheHeaderRowAlone_opensWithoutIt_andIsKept() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = sheetWithHeaders(workbook);
        sheet.setAutoFilter(CellRangeAddress.valueOf("B2:D2"));

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        Assertions.assertTrue(spreadsheet.getTables().isEmpty());
        Assertions.assertEquals("B2:D2",
                sheet.getCTWorksheet().getAutoFilter().getRef());
    }

    @Test
    void aTableOfOneRow_opensWithoutIt_andIsKept() {
        // As a file holds it: POI makes no table of one row itself.
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFTable table = sheetWithHeaders(workbook).createTable(
                new AreaReference("B2:D3", SpreadsheetVersion.EXCEL2007));
        table.getCTTable().setHeaderRowCount(0);
        table.getCTTable().setRef("B2:D2");

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        Assertions.assertTrue(spreadsheet.getTables().isEmpty());
        Assertions.assertEquals("B2:D2", table.getCTTable().getRef());
    }

    @Test
    void aFilterWithARowUnderItsHeader_isShown() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        sheetWithHeaders(workbook)
                .setAutoFilter(CellRangeAddress.valueOf("B2:D3"));

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        Assertions.assertEquals("B2:D3", spreadsheet.getTables().iterator()
                .next().getFullTableRegion().formatAsString());
    }
}
