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
import java.util.Map;

import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.IndexedColors;
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
 * Formulas that leave hidden rows out, as {@code SUBTOTAL(109, ...)} does, are
 * worked out again when rows are hidden or shown, whichever way: the client has
 * what a fresh load gives it.
 */
class SpreadsheetHiddenRowFormulasTest {

    @RegisterExtension
    MockUIExtension ui = new MockUIExtension();

    private final FreshLoadHarness harness = new FreshLoadHarness(ui,
            spreadsheet -> spreadsheet.setLocale(Locale.US),
            new FreshLoadHarness.Viewport(1, 1, 10, 5));

    /**
     * A1 to A5 hold 1 to 5, row 2 is hidden, and A6 sums the rows shown, in red
     * and with two decimals when the sum is over 12.
     */
    private static Workbook workbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        for (int row = 0; row < 5; row++) {
            sheet.createRow(row).createCell(0).setCellValue(row + 1);
        }
        sheet.createRow(5).createCell(0).setCellFormula("SUBTOTAL(109,A1:A5)");
        sheet.getRow(1).setZeroHeight(true);
        SheetConditionalFormatting formatting = sheet
                .getSheetConditionalFormatting();
        ConditionalFormattingRule overTwelve = formatting
                .createConditionalFormattingRule(ComparisonOperator.GT, "12");
        overTwelve.createFontFormatting()
                .setFontColorIndex(IndexedColors.RED.getIndex());
        // The rule shows the sum with two decimals too, a format only the
        // rule gives it; its style is the last of the workbook's.
        StylesTable styles = workbook.getStylesSource();
        CTNumFmt twoDecimals = styles.getDxfAt(styles._getDXfsSize() - 1)
                .addNewNumFmt();
        twoDecimals.setNumFmtId(200);
        twoDecimals.setFormatCode("0.00");
        formatting.addConditionalFormatting(
                new CellRangeAddress[] { CellRangeAddress.valueOf("A6") },
                overTwelve);
        return workbook;
    }

    @BeforeEach
    void show() {
        harness.show(workbook());
    }

    @Test
    void aRowHidden() {
        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.setRowHidden(2, true)));
    }

    @Test
    void aHiddenRowShown() {
        assertEquals(List.of(), harness
                .check(spreadsheet -> spreadsheet.setRowHidden(1, false)));
    }

    @Test
    void aRowGivenNoHeight() {
        assertEquals(List.of(),
                harness.check(spreadsheet -> spreadsheet.setRowHeight(2, 0f)));
    }

    @Test
    void aHiddenRowGivenAHeight() {
        assertEquals(List.of(),
                harness.check(spreadsheet -> spreadsheet.setRowHeight(1, 20f)));
    }

    @Test
    void aRowDraggedToNothing() {
        // The client numbers the rows from 1.
        assertEquals(List.of(),
                harness.check(spreadsheet -> harness.clientEvent("rowsResized",
                        "{\"rowIndexes\": [3], \"newHeights\": [0]}")));
    }

    @Test
    void aHiddenRowDraggedOpen() {
        // The client numbers the rows from 1.
        assertEquals(List.of(),
                harness.check(spreadsheet -> harness.clientEvent("rowsResized",
                        "{\"rowIndexes\": [2], \"newHeights\": [20]}")));
    }

    @Test
    void rowsFilteredOut() {
        // As a filter hides the rows it leaves out.
        assertEquals(List.of(), harness.check(spreadsheet -> spreadsheet
                .setRowsHidden(Map.of(2, true, 3, true))));
    }
}
