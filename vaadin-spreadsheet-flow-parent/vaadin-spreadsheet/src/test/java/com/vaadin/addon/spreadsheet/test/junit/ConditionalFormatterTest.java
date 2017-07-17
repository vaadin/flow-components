package com.vaadin.addon.spreadsheet.test.junit;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.ConditionalFormatter;
import com.vaadin.addon.spreadsheet.SheetImageWrapper;
import com.vaadin.addon.spreadsheet.Spreadsheet;

/**
 * Tests for conditional formatting
 *
 */
public class ConditionalFormatterTest {

    /**
     * Ticket #17595
     */
    @Test
    public void createConditionalFormatterRules_sheetWithStringFormatRuleForNumericCell_rulesCreatedWithoutExceptions()
            throws URISyntaxException, IOException {
        createConditionalFormatterRulesForSheet("conditional_formatting.xlsx");
    }

    /**
     * Test no NPE is thrown
     * 
     * This test might fail if assertions are enabled due to
     * {@link SheetImageWrapper#hashCode()} using
     * {@link ClientAnchor#hashCode()} which wasn't designed and does {@code
     *         assert false : "hashCode not designed";
     * }. Assertions can be disabled with -DenableAssertions=false in maven.
     * HashCode issue reported in SHEET-120
     */
    @Test
    public void matchesFormula_rulesWithoutFormula_formulasEvaluatedWithoutExceptions()
            throws URISyntaxException, IOException {
        // ensure sheet with rules without formulas is active
        createConditionalFormatterRulesForSheet(
                "ConditionalFormatterSamples.xlsx", 3);
    }

    @Test
    public void createConditionalFormatterRules_ruleWithNullBackgroundColor_rulesCreatedWithoutExceptions()
            throws URISyntaxException, IOException {
        createConditionalFormatterRulesForSheet(
                "conditionalformater_nobackground.xlsx");
    }

    private void createConditionalFormatterRulesForSheet(String fileName)
            throws URISyntaxException, IOException {
        createConditionalFormatterRulesForSheet(fileName, null);
    }

    private void createConditionalFormatterRulesForSheet(String fileName,
            Integer sheetIndex) throws URISyntaxException, IOException {
        ClassLoader classLoader = ConditionalFormatterTest.class
                .getClassLoader();
        URL resource = classLoader
                .getResource("test_sheets" + File.separator + fileName);
        File file = new File(resource.toURI());

        Spreadsheet sheet = new Spreadsheet(file);

        if (sheetIndex != null && sheet.getActiveSheetIndex() != sheetIndex) {
            sheet.setActiveSheetIndex(sheetIndex);
        }
        new ConditionalFormatter(sheet).createConditionalFormatterRules();
    }
}
