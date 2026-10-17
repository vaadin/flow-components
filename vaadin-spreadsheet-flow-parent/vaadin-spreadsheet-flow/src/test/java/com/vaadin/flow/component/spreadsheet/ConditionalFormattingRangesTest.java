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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Rules over more rows or columns than the sheet shows, as rules over whole
 * columns or rows are: only the cells shown are formatted, the sheet does not
 * grow to the end of the rule, and rows added later are formatted too.
 */
class ConditionalFormattingRangesTest {

    /**
     * A sheet of 20 rows, the default 200 shown, whose cells in the range are
     * filled while C1 holds over 0, which it does.
     */
    private static XSSFWorkbook filledWhileC1IsSet(String range) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int row = 0; row < 20; row++) {
            sheet.createRow(row).createCell(0).setCellValue(row);
        }
        sheet.getRow(0).createCell(2).setCellValue(1);
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        ConditionalFormattingRule set = formatting
                .createConditionalFormattingRule("$C$1>0");
        PatternFormatting fill = set.createPatternFormatting();
        fill.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
        fill.setFillPattern(FillPatternType.SOLID_FOREGROUND.getCode());
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf(range) },
                set);
        return workbook;
    }

    @Test
    void aRuleOverMoreRowsThanShown_growsNeitherTheSheetNorTheWorkbook() {
        XSSFWorkbook workbook = filledWhileC1IsSet("A1:A100000");

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(200, spreadsheet.getRows());
        assertTrue(workbook.getSheetAt(0).getLastRowNum() < 200);
    }

    @Test
    void aRuleOverMoreColumnsThanShown_growsNeitherTheSheetNorTheWorkbook() {
        XSSFWorkbook workbook = filledWhileC1IsSet("A2:ZZ2");

        Spreadsheet spreadsheet = new Spreadsheet(workbook);

        assertEquals(52, spreadsheet.getColumns());
        assertTrue(workbook.getSheetAt(0).getRow(1).getLastCellNum() <= 52);
    }

    @Test
    void rowsAddedToTheSheet_areFormattedToo() {
        Spreadsheet spreadsheet = new Spreadsheet(
                filledWhileC1IsSet("A1:A100000"));

        spreadsheet.setMaxRows(300);

        assertNotNull(spreadsheet.getConditionalFormatter()
                .getCellFormattingIndex(spreadsheet.getCell(250, 0)));
    }
}
