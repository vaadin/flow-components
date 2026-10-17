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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Rows shifted: the workbook has the rows moved, and nothing else added to it.
 * What the client has afterwards, {@link FreshLoadConsistencyTest} shows.
 */
class SpreadsheetRowShiftTest {

    @Test
    void shiftingRows_addsNoCellsToTheWorkbook() {
        // A1 shifted down to A3, with a value below in A6.
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(0).setCellValue("moved");
        sheet.createRow(5).createCell(0).setCellValue("below");
        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        spreadsheet.shiftRows(0, 0, 2);

        assertNull(sheet.getRow(0));
        assertEquals(1, sheet.getRow(2).getPhysicalNumberOfCells());
        assertEquals(1, sheet.getRow(5).getPhysicalNumberOfCells());
    }

    @Test
    void shiftingRowsUp_addsNoCellsToTheWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(3).setCellValue("above");
        sheet.createRow(4).createCell(0).setCellValue("moved");
        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        spreadsheet.shiftRows(4, 4, -2);

        assertEquals(1, sheet.getRow(0).getPhysicalNumberOfCells());
        assertEquals(1, sheet.getRow(2).getPhysicalNumberOfCells());
    }
}
