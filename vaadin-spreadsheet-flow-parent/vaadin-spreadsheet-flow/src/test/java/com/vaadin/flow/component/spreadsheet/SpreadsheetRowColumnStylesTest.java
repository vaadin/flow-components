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

import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Rows and columns given a style of their own, which their cells without a
 * style wear, and which lock their cells when the sheet is protected: a sheet
 * shown has the styles of all its rows and columns, however wide and long it
 * is.
 */
class SpreadsheetRowColumnStylesTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            spreadsheet -> spreadsheet.setLocale(Locale.US),
            new FreshLoadHarness.Viewport(1, 1, 20, 10));

    /**
     * Sheet "First" of the default size, and sheet "Wide" of 250 rows and 60
     * columns, with a style of its own on row 230 and on column 58.
     */
    private static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("First").createRow(0).createCell(0)
                .setCellValue("first");
        Sheet wide = workbook.createSheet("Wide");
        wide.createRow(249).createCell(59).setCellValue("wide");
        CellStyle filled = workbook.createCellStyle();
        filled.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        filled.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        filled.setLocked(false);
        wide.setDefaultColumnStyle(57, filled);
        wide.createRow(229).setRowStyle(filled);
        return workbook;
    }

    @Test
    void aWiderSheetShown_hasTheStylesOfAllItsRowsAndColumns() {
        harness.show(workbook());

        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.setActiveSheetIndex(1)));
    }

    @Test
    void aSmallerSheetShownAfterAWiderOne_hasTheStylesOfItsRowsAndColumns() {
        XSSFWorkbook workbook = workbook();
        workbook.setActiveSheet(1);
        harness.show(workbook);

        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.setActiveSheetIndex(0)));
    }
}
