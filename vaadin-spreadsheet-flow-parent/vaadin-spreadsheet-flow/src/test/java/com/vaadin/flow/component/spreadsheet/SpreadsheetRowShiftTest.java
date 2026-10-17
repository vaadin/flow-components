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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Rows shifted or deleted: the workbook has the rows moved, and nothing else
 * added to it, and the selection is made again over the merged regions there
 * now. What the client has afterwards, {@link FreshLoadConsistencyTest} shows.
 */
class SpreadsheetRowShiftTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

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
    void aMergedRegionShiftedOntoTheSelectedCell_isSelectedWithIt() {
        // B2:C2 shifted down onto B3, which is selected.
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(1).createCell(1).setCellValue("merged");
        sheet.addMergedRegion(CellRangeAddress.valueOf("B2:C2"));
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSelection("B3");

        spreadsheet.shiftRows(1, 1, 1);

        assertEquals("B3:C3", spreadsheet.getCellSelectionManager()
                .getSelectedCellRange().formatAsString());
    }

    @Test
    void aSelectionAboveTheRowsShifted_isKept() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(5).createCell(0).setCellValue("a6");
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSelection("A1:B2");

        spreadsheet.shiftRows(5, 5, 1);

        assertEquals("A1:B2", spreadsheet.getCellSelectionManager()
                .getSelectedCellRange().formatAsString());
    }

    @Test
    void aSelectionBelowTheRowsShifted_isKept() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).createCell(0).setCellValue("a1");
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSelection("A5:B6");

        spreadsheet.shiftRows(0, 0, 1);

        assertEquals("A5:B6", spreadsheet.getCellSelectionManager()
                .getSelectedCellRange().formatAsString());
    }

    @Test
    void shiftingTheRowOfTheSelectedCell_tellsTheClientOfNoInvalidAddress() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(1).createCell(1).setCellValue("b2");
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        ui.add(spreadsheet);
        spreadsheet.setSelection("B2");
        ui.fakeClientCommunication();
        ui.dumpPendingJavaScriptInvocations();

        spreadsheet.shiftRows(1, 1, 1);

        ui.fakeClientCommunication();
        assertTrue(ui.dumpPendingJavaScriptInvocations().stream()
                .noneMatch(call -> call.getInvocation().getParameters()
                        .contains("invalidCellAddress")));
    }

    @Test
    void aCellOfAMergedRegionSelected_isSelectedWithTheRegion_onceItsRowIsDeleted() {
        // Deleting rows takes their cells; POI keeps the merged region, and
        // the selection is made again over it, as a shift and the client
        // make it.
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(2).createCell(1).setCellValue("merged");
        sheet.addMergedRegion(CellRangeAddress.valueOf("B3:C3"));
        Spreadsheet spreadsheet = new Spreadsheet(workbook);
        spreadsheet.setSelection("B3");

        spreadsheet.deleteRows(2, 2);

        assertEquals("B3:C3", spreadsheet.getCellSelectionManager()
                .getSelectedCellRange().formatAsString());
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
