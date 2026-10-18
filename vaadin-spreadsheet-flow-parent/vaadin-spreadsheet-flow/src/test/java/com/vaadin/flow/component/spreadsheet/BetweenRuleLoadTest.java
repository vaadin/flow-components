/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * A workbook whose "between" and "not between" rules end at a cell reference
 * opens, and the rules colour the cells they hold for. The second bound used to
 * be read as a number, which threw while the sheet loaded, and "not between"
 * never held.
 */
class BetweenRuleLoadTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    /**
     * A1 holds 15 and A2 25, B1 15 and B2 25; C1 holds 20. A1:A2 are filled
     * between 10 and C1, B1:B2 outside them.
     */
    private static XSSFWorkbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(0).createCell(0).setCellValue(15);
        sheet.getRow(0).createCell(1).setCellValue(15);
        sheet.getRow(0).createCell(2).setCellValue(20);
        sheet.createRow(1).createCell(0).setCellValue(25);
        sheet.getRow(1).createCell(1).setCellValue(25);
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        addFilled(formatting, ComparisonOperator.BETWEEN, "A1:A2");
        addFilled(formatting, ComparisonOperator.NOT_BETWEEN, "B1:B2");
        return workbook;
    }

    private static void addFilled(SheetConditionalFormatting formatting,
            byte operator, String range) {
        ConditionalFormattingRule rule = formatting
                .createConditionalFormattingRule(operator, "10", "$C$1");
        PatternFormatting fill = rule.createPatternFormatting();
        fill.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
        fill.setFillPattern(FillPatternType.SOLID_FOREGROUND.getCode());
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf(range) },
                rule);
    }

    private static boolean formatted(Spreadsheet spreadsheet, String address) {
        Set<Integer> rules = spreadsheet.getConditionalFormatter()
                .getCellFormattingIndex(spreadsheet.getCell(address));
        return rules != null && !rules.isEmpty();
    }

    @Test
    void betweenRulesEndingAtACellReference_formatTheCellsTheyHoldFor() {
        Spreadsheet spreadsheet = new Spreadsheet(workbook());
        ui.add(spreadsheet);
        ui.fakeClientCommunication();

        assertTrue(formatted(spreadsheet, "A1"), "15 is between 10 and 20");
        assertFalse(formatted(spreadsheet, "A2"), "25 is not");
        assertFalse(formatted(spreadsheet, "B1"), "15 is not outside them");
        assertTrue(formatted(spreadsheet, "B2"), "25 is");
    }
}
