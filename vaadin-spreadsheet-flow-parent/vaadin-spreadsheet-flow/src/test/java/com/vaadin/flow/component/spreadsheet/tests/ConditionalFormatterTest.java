package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.spreadsheet.SheetImageWrapper;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import elemental.json.impl.JreJsonObject;
import elemental.json.impl.JsonUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;

/**
 * Tests for conditional formatting
 *
 */
public class ConditionalFormatterTest {

    @Test
    public void createConditionalFormatterRules_sheetWithStringFormatRuleForNumericCell_rulesCreatedWithoutExceptions() {
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
    public void matchesFormula_rulesWithoutFormula_formulasEvaluatedWithoutExceptions() {
        // ensure sheet with rules without formulas is active
        createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 3);
    }

    @Test
    public void cellValueMatchesFormula_cellHasFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D7 cell value is $192,10, so it meets the criteria
        var cell = sheet.getCell("D7");
        Assert.assertNotNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    public void cellValueMatchedFormula_valueIsChangeToNotMatch_cellHasNoFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D7 cell value is $192,10, so it meets the criteria
        var cell = sheet.getCell("D7");
        cell.setCellValue(550);
        sheet.refreshCells(cell);
        Assert.assertNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    public void cellDoesntMatchFormula_cellHasNoFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D9 cell value is $560,40, so it doesn't meet the criteria
        var cell = sheet.getCell("D9");
        Assert.assertNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    public void cellValuesMatchedFormula_styleIsPresent() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        // D3:D21 range has conditional formatting applied to cell with value <
        // 500
        // D7 cell value is $192,10, so it meets the criteria
        var cell = sheet.getCell("D7");

        assertCellHasStyle(sheet, cell);
    }

    @Test
    public void sheetHasConditionalsFormatting_cellsMatching_allCellsHaveStyles() {
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
    public void createConditionalFormatterRules_ruleWithNullBackgroundColor_rulesCreatedWithoutExceptions() {
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

        var styles = (JreJsonObject) JsonUtil.parse(
                sheet.getElement().getProperty("conditionalFormattingStyles"));
        var cellFormattingIndex = sheet.getConditionalFormatter()
                .getCellFormattingIndex(cell);

        Assert.assertEquals(1, cellFormattingIndex.size());
        var formattingIndex = cellFormattingIndex.stream().findFirst()
                .orElse(-1).toString();
        Assert.assertNotNull(styles.get(formattingIndex));
    }
}
