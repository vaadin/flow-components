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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.vaadin.tests.MockUIExtension;

/**
 * Cells a conditional formatting rule colours, fills or borders where it holds:
 * the client has what a fresh load gives it once values change, the value a
 * rule reads among them, and once rules are added or removed.
 */
class SpreadsheetConditionalFormatsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            spreadsheet -> spreadsheet.setLocale(Locale.US),
            new FreshLoadHarness.Viewport(1, 1, 10, 8));

    /**
     * B2:B4 are filled while D2 holds over 5, which it does not: it holds 1.
     * C2:C4 have a border below a value over 0, which C3 holds: 5. F2:F3 are
     * bold where they hold "x", which F2 does.
     */
    private static Workbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        sheet.createRow(1).createCell(3).setCellValue(1);
        sheet.getRow(1).createCell(5).setCellValue("x");
        sheet.createRow(2).createCell(2).setCellValue(5);
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();

        ConditionalFormattingRule overFive = formatting
                .createConditionalFormattingRule("$D$2>5");
        PatternFormatting fill = overFive.createPatternFormatting();
        fill.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
        fill.setFillPattern(FillPatternType.SOLID_FOREGROUND.getCode());
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("B2:B4") },
                overFive);

        ConditionalFormattingRule overZero = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT, "0");
        overZero.createBorderFormatting().setBorderBottom(BorderStyle.THIN);
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("C2:C4") },
                overZero);

        ConditionalFormattingRule isX = formatting
                .createConditionalFormattingRule(ComparisonOperator.EQUAL,
                        "\"x\"");
        isX.createFontFormatting().setFontStyle(false, true);
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("F2:F3") },
                isX);
        return workbook;
    }

    @BeforeEach
    void show() {
        harness.show(workbook());
    }

    /** The client numbers rows and columns from 1. */
    private void type(int row, int column, String value) {
        harness.clientEvent("cellSelected",
                "{\"row\": " + row + ", \"column\": " + column
                        + ", \"oldSelectionRangeDiscarded\": true}");
        harness.clientEvent("cellValueEdited", "{\"row\": " + row
                + ", \"col\": " + column + ", \"value\": \"" + value + "\"}");
    }

    @Test
    void aValueTypedThatARuleReads() {
        // 9 into D2: B2:B4 are filled.
        assertEquals(List.of(), harness.check(spreadsheet -> type(2, 4, "9")));
    }

    @Test
    void aValueSetThatARuleReads() {
        assertEquals(List.of(), harness.check(spreadsheet -> {
            Cell d2 = spreadsheet.getCell("D2");
            d2.setCellValue(9);
            spreadsheet.refreshCells(d2);
        }));
    }

    @Test
    void aValueTypedThatABorderRuleNoLongerHoldsFor() {
        // 0 into C3.
        assertEquals(List.of(), harness.check(spreadsheet -> type(3, 3, "0")));
    }

    @Test
    void aValueTypedThatAFontRuleHoldsFor() {
        // x into F3.
        assertEquals(List.of(), harness.check(spreadsheet -> type(3, 6, "x")));
    }

    @Test
    void aFormattedCellDeleted() {
        assertEquals(List.of(),
                harness.check(spreadsheet -> spreadsheet.deleteCell(2, 2)));
    }

    @Test
    void aRuleAdded() {
        // F2:F3 italic where they hold "x" too.
        assertEquals(List.of(), harness.check(spreadsheet -> {
            SheetConditionalFormatting formatting = spreadsheet.getActiveSheet()
                    .getSheetConditionalFormatting();
            ConditionalFormattingRule isX = formatting
                    .createConditionalFormattingRule(ComparisonOperator.EQUAL,
                            "\"x\"");
            isX.createFontFormatting().setFontStyle(true, false);
            formatting.addConditionalFormatting(new CellRangeAddress[] {
                    CellRangeAddress.valueOf("F2:F3") }, isX);
            spreadsheet.refreshAllCellValues();
        }));
    }

    @Test
    void aRuleRemoved() {
        // The border below C3 goes.
        assertEquals(List.of(), harness.check(spreadsheet -> {
            spreadsheet.getActiveSheet().getSheetConditionalFormatting()
                    .removeConditionalFormatting(1);
            spreadsheet.refreshAllCellValues();
        }));
    }
}
