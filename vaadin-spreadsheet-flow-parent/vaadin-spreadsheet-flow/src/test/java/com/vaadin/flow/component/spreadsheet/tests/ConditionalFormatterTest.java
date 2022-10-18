package com.vaadin.flow.component.spreadsheet.tests;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.nimbusds.jose.shaded.json.parser.JSONParser;
import com.vaadin.flow.component.spreadsheet.ConditionalFormatter;
import com.vaadin.flow.component.spreadsheet.SheetImageWrapper;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import elemental.json.JsonValue;
import elemental.json.impl.JreJsonObject;
import elemental.json.impl.JsonUtil;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

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
        Spreadsheet sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 3);
    }

    @Test
    public void cellValueMatchesFormula_cellHasFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        var cell = sheet.getCell("D7");
        Assert.assertNotNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    public void cellValueMatchedFormula_valueIsChangeToNotMatch_cellHasNoFormatting() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
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
        var cell = sheet.getCell("D9");
        Assert.assertNull(
                sheet.getConditionalFormatter().getCellFormattingIndex(cell));
    }

    @Test
    public void cellValuesMatchedFormula_styleIsPresent() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        var cell = sheet.getCell("D7");

        var styles = (JreJsonObject) JsonUtil.parse(
                sheet.getElement().getProperty("conditionalFormattingStyles"));
        var cellFormattingIndex = sheet.getConditionalFormatter()
                .getCellFormattingIndex(cell);

        Assert.assertEquals(1, cellFormattingIndex.size());
        var formattingIndex = cellFormattingIndex.stream().findFirst()
                .orElse(-1).toString();
        Assert.assertNotNull(styles.get(formattingIndex));
    }

    @Test
    public void sheetHas() {
        var sheet = createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 1);
        var cell = sheet.getCell("D7");

        var styles = (JreJsonObject) JsonUtil.parse(
                sheet.getElement().getProperty("conditionalFormattingStyles"));
        var cellFormattingIndex = sheet.getConditionalFormatter()
                .getCellFormattingIndex(cell);

        Assert.assertEquals(1, cellFormattingIndex.size());
        var formattingIndex = cellFormattingIndex.stream().findFirst()
                .orElse(-1).toString();
        Assert.assertNotNull(styles.get(formattingIndex));
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
        // new ConditionalFormatter(sheet).createConditionalFormatterRules();

        return sheet;
    }
}
