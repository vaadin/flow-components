/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests;

import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.spreadsheet.SheetImageWrapper;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.core.JacksonException;

/**
 * Tests for conditional formatting
 *
 */
class ConditionalFormatterTest {

    @Test
    void createConditionalFormatterRules_sheetWithStringFormatRuleForNumericCell_rulesCreatedWithoutExceptions() {
        createConditionalFormatterRulesForSheet("conditional_formatting.xlsx");
    }

    /**
     * Test no NPE is thrown
     * <p>
     * This test might fail if assertions are enabled due to
     * {@link SheetImageWrapper#hashCode()} using
     * {@link ClientAnchor#hashCode()} which wasn't designed and does {@code
     *         assert false : "hashCode not designed";
     * }. Assertions can be disabled with -DenableAssertions=false in maven.
     */
    @Test
    void matchesFormula_rulesWithoutFormula_formulasEvaluatedWithoutExceptions() {
        // ensure sheet with rules without formulas is active
        createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 3);
    }

    @Test
    void cellValueMatchesFormula_cellHasFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D7 cell value is $192,10, so it meets the criteria
        var cell = sheet.getCell("D7");
        Assertions.assertNotNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    void cellValueMatchedFormula_valueIsChangeToNotMatch_cellHasNoFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D7 cell value is $192,10, so it meets the criteria
        var cell = sheet.getCell("D7");
        cell.setCellValue(550);
        sheet.refreshCells(cell);
        Assertions.assertNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    void cellDoesntMatchFormula_cellHasNoFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D9 cell value is $560,40, so it doesn't meet the criteria
        var cell = sheet.getCell("D9");
        Assertions.assertNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    void cellValuesMatchedFormula_styleIsPresent() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D7 cell value is $192,10, so it meets the criteria
        var cell = sheet.getCell("D7");

        assertCellHasStyle(sheet, cell);
    }

    @Test
    void sheetHasConditionalsFormatting_cellsMatching_allCellsHaveStyles() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);

        // The sheet has 3 conditional formatters which results in 3 styles
        // being created
        // (This is not always true, as there can be more styles applied to
        // adjacent cells, like for borders)

        var cell = sheet.getCell("D7");
        assertCellHasStyle(sheet, cell);

        cell = sheet.getCell("B9");
        assertCellHasStyle(sheet, cell);

        cell = sheet.getCell("A3");
        // The condition on the "A" column matches when the date is on the
        // current month
        cell.setCellValue(LocalDate.now());
        sheet.refreshCells(cell);
        assertCellHasStyle(sheet, cell);
    }

    @Test
    void createConditionalFormatterRules_ruleWithNullBackgroundColor_rulesCreatedWithoutExceptions() {
        createConditionalFormatterRulesForSheet(
                "conditionalformater_nobackground.xlsx");
    }

    private void createConditionalFormatterRulesForSheet(String fileName) {
        createConditionalFormatterRulesForSheet(fileName, null);
    }

    private Spreadsheet createConditionalFormatterRulesForSheet(String fileName,
            Integer sheetIndex) {
        Spreadsheet sheet = TestHelper.createSpreadsheet(fileName);

        if (sheetIndex != null && sheet.getActiveSheetIndex() != sheetIndex) {
            sheet.setActiveSheetIndex(sheetIndex);
        }

        return sheet;
    }

    private static void assertCellHasStyle(Spreadsheet sheet, Cell cell) {

        try {
            var styles = JacksonUtils.getMapper().readTree(sheet.getElement()
                    .getProperty("conditionalFormattingStyles"));
            var cellFormattingIndex = sheet.getConditionalFormatter()
                    .getCellFormattingIndex(cell);

            Assertions.assertEquals(1, cellFormattingIndex.size());
            var formattingIndex = cellFormattingIndex.stream().findFirst()
                    .orElse(-1).toString();
            Assertions.assertNotNull(styles.get(formattingIndex));
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
}
