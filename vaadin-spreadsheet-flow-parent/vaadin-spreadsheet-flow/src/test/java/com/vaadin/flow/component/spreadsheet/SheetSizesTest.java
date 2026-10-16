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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * The heights the rows of a sheet are shown at, as the sheet gives them.
 */
class SheetSizesTest {

    @Test
    void aSheetOfMoreRowsAndColumnsThanTheDefaults_isShownWithAllOfThem() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(299).createCell(69);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(300, spreadsheet.getRows());
        assertEquals(300, spreadsheet.getRowH().length);
        assertEquals(70, spreadsheet.getColumns());
        assertEquals(70, spreadsheet.getColW().length);
    }

    @Test
    void theOnlyRow_isShownAtItsOwnHeight() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).setHeightInPoints(20);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(20f, spreadsheet.getRowH()[0]);
    }

    @Test
    void theOnlyRow_hidden_staysHidden() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().createRow(0).setZeroHeight(true);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(0f, spreadsheet.getRowH()[0]);
        assertTrue(spreadsheet.getHiddenRowIndexes().contains(1));
    }

    @Test
    void aSheetWithoutRows_showsTheFirstAtTheDefaultHeight() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet().setDefaultRowHeightInPoints(18);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(18f, spreadsheet.getRowH()[0]);
    }

    @Test
    void aSheetWithoutRows_ofAnOldWorkbook_showsTheFirstAtTheDefaultHeight() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        workbook.createSheet().setDefaultRowHeightInPoints(18);

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(18f, spreadsheet.getRowH()[0]);
    }
}
