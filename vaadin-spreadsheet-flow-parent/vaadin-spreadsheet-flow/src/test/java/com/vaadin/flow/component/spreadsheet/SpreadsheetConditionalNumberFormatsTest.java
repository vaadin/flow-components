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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;

import com.vaadin.tests.MockUIExtension;

/**
 * Values a conditional formatting rule shows in a number format of its own,
 * where the rule holds: the client has what a fresh load gives it once values
 * change, rules are added or removed, or the rows are shifted.
 */
class SpreadsheetConditionalNumberFormatsTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            spreadsheet -> spreadsheet.setLocale(Locale.US),
            new FreshLoadHarness.Viewport(1, 1, 10, 5));

    /**
     * A3 holds 20 and A4 holds 3, in A1:A10, where a value over 12 has two
     * decimals; C3 holds 9, in C3:C4, where a value over 5 has two decimals; E2
     * holds 7, under no rule.
     */
    private static Workbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        Row third = sheet.createRow(2);
        third.createCell(0).setCellValue(20);
        third.createCell(2).setCellValue(9);
        sheet.createRow(3).createCell(0).setCellValue(3);
        sheet.createRow(1).createCell(4).setCellValue(7);
        twoDecimalsOver(workbook, sheet, "12", "A1:A10");
        twoDecimalsOver(workbook, sheet, "5", "C3:C4");
        return workbook;
    }

    private static void twoDecimalsOver(XSSFWorkbook workbook, Sheet sheet,
            String value, String range) {
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        ConditionalFormattingRule over = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT, value);
        over.createFontFormatting()
                .setFontColorIndex(IndexedColors.RED.getIndex());
        // The rule's style is the last of the workbook's.
        StylesTable styles = workbook.getStylesSource();
        CTNumFmt twoDecimals = styles.getDxfAt(styles._getDXfsSize() - 1)
                .addNewNumFmt();
        twoDecimals.setNumFmtId(200);
        twoDecimals.setFormatCode("0.00");
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf(range) },
                over);
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
    void aValueTypedThatTheRuleHoldsFor() {
        // 30 into A4, where 3 was.
        assertEquals(List.of(), harness.check(spreadsheet -> type(4, 1, "30")));
    }

    @Test
    void aValueTypedThatTheRuleNoLongerHoldsFor() {
        // 1 into A3, where 20 was.
        assertEquals(List.of(), harness.check(spreadsheet -> type(3, 1, "1")));
    }

    @Test
    void aValueSetThatTheRuleHoldsFor() {
        assertEquals(List.of(), harness.check(spreadsheet -> {
            Cell a4 = spreadsheet.getCell("A4");
            a4.setCellValue(30);
            spreadsheet.refreshCells(a4);
        }));
    }

    @Test
    void aRuleAdded() {
        // Over 5 in E1:E5, which E2 holds 7 in.
        assertEquals(List.of(), harness.check(spreadsheet -> {
            twoDecimalsOver((XSSFWorkbook) spreadsheet.getWorkbook(),
                    spreadsheet.getActiveSheet(), "5", "E1:E5");
            spreadsheet.refreshAllCellValues();
        }));
    }

    @Test
    void aRuleRemoved() {
        // The rule over A1:A10, which A3 holds 20 in.
        assertEquals(List.of(), harness.check(spreadsheet -> {
            spreadsheet.getActiveSheet().getSheetConditionalFormatting()
                    .removeConditionalFormatting(0);
            spreadsheet.refreshAllCellValues();
        }));
    }

    @Test
    void valuesShiftedWithinTheirRange() {
        // A3:A4 down to A4:A5: 20 comes where 3 was shown.
        assertEquals(List.of(),
                harness.check(spreadsheet -> spreadsheet.shiftRows(2, 3, 1)));
    }

    @Test
    void aRangeShiftedWithItsRows() {
        // C3:C4 down to C5:C6, which the rule holds for then.
        assertEquals(List.of(),
                harness.check(spreadsheet -> spreadsheet.shiftRows(2, 3, 2)));
    }
}
