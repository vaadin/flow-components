package com.vaadin.flow.component.spreadsheet.tests;

import com.vaadin.flow.component.spreadsheet.ConditionalFormatter;
import com.vaadin.flow.component.spreadsheet.SheetImageWrapper;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.junit.Test;

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
    public void createConditionalFormatterRules_ruleWithNullBackgroundColor_rulesCreatedWithoutExceptions() {
        createConditionalFormatterRulesForSheet(
                "conditionalformater_nobackground.xlsx");
    }

    private void createConditionalFormatterRulesForSheet(String fileName) {
        createConditionalFormatterRulesForSheet(fileName, null);
    }

    private void createConditionalFormatterRulesForSheet(String fileName,
            Integer sheetIndex) {
        Spreadsheet sheet = TestHelper.createSpreadsheet(fileName);

        if (sheetIndex != null && sheet.getActiveSheetIndex() != sheetIndex) {
            sheet.setActiveSheetIndex(sheetIndex);
        }
        new ConditionalFormatter(sheet).createConditionalFormatterRules();
    }
}
