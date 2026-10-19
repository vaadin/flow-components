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

import java.util.List;
import java.util.Locale;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Cells created, with a value or a formula, or rows shifted, past the last row
 * or column shown: the sheet grows to show them, with the styles and locks of
 * the rows and columns it grows by, as loading the workbook shows it.
 */
class CellsCreatedPastTheEndTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            spreadsheet -> spreadsheet.setLocale(Locale.US),
            new FreshLoadHarness.Viewport(195, 1, 210, 10));

    /** A sheet of the default 200 rows and 52 columns, A1 holding a value. */
    private static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).createCell(0).setCellValue("a1");
        return workbook;
    }

    @Test
    void aCellCreatedPastTheLastRow_givesTheClientTheRowsAdded_inTheResponse() {
        // Not refreshed, so the rows added are all the response sends.
        Spreadsheet spreadsheet = new Spreadsheet(20, 10);
        ui.add(spreadsheet);
        ui.fakeClientCommunication();
        Object heights = spreadsheet.getElement().getPropertyRaw("rowH");

        spreadsheet.createCell(25, 0, "row 26");
        ui.fakeClientCommunication();

        assertNotEquals(heights,
                spreadsheet.getElement().getPropertyRaw("rowH"));
    }

    @Test
    void aCellJustPastTheLastRow() {
        harness.show(workbook());

        assertEquals(List.of(), harness.check(spreadsheet -> spreadsheet
                .refreshCells(spreadsheet.createCell(200, 0, "row 201"))));
    }

    @Test
    void aCellJustPastTheLastColumn() {
        harness.show(workbook());

        assertEquals(List.of(), harness.check(spreadsheet -> spreadsheet
                .refreshCells(spreadsheet.createCell(196, 52, "column 53"))));
    }

    @Test
    void aFormulaCellJustPastTheLastRow() {
        harness.show(workbook());

        assertEquals(List.of(), harness.check(spreadsheet -> spreadsheet
                .refreshCells(spreadsheet.createFormulaCell(200, 0, "1+1"))));
    }

    @Test
    void aFormulaCellJustPastTheLastColumn() {
        harness.show(workbook());

        assertEquals(List.of(), harness.check(spreadsheet -> spreadsheet
                .refreshCells(spreadsheet.createFormulaCell(196, 52, "1+1"))));
    }

    @Test
    void aRowShiftedPastTheLastRow() {
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).createRow(199).createCell(0)
                .setCellValue("last");
        harness.show(workbook);

        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.shiftRows(199, 199, 1)));
    }

    @Test
    void emptyRowsShiftedPastTheLastRow() {
        harness.show(workbook());

        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.shiftRows(150, 199, 5)));
    }

    @Test
    void aCellPastTheLastRow_ofAProtectedSheet() {
        XSSFWorkbook workbook = workbook();
        workbook.getSheetAt(0).protectSheet("");
        harness.show(workbook);

        assertEquals(List.of(), harness.check(spreadsheet -> spreadsheet
                .refreshCells(spreadsheet.createCell(204, 1, "row 205"))));
    }
}
