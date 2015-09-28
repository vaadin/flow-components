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

        ClassLoader classLoader = ConditionalFormatterTest.class
                .getClassLoader();
        URL resource = classLoader.getResource("test_sheets" + File.separator
                + "conditional_formatting.xlsx");
        File file = new File(resource.toURI());

        Spreadsheet sheet = new Spreadsheet(file);

        new ConditionalFormatter(sheet).createConditionalFormatterRules();
    }

    /**
     * Test no NPE is thrown
     * 
     * This test might fail if assertions are enabled due to
     * {@link SheetImageWrapper#hashCode()} using
     * {@link ClientAnchor#hashCode()} which wasn't designed and does
     * {@code
     *         assert false : "hashCode not designed";
     * }. Assertions can be disabled with -DenableAssertions=false in maven.
     * HashCode issue reported in SHEET-120
     */
    @Test
    public void matchesFormula_rulesWithoutFormula_formulasEvaluatedWithoutExceptions()
            throws URISyntaxException, IOException {

        ClassLoader classLoader = ConditionalFormatterTest.class
                .getClassLoader();
        URL resource = classLoader.getResource("test_sheets" + File.separator
                + "ConditionalFormatterSamples.xlsx");
        File file = new File(resource.toURI());

        Spreadsheet sheet = new Spreadsheet(file);

        // active sheet is saved in file
        // it might change after modifying test file
        // ensure sheet with rules without formulas is active
        if (sheet.getActiveSheetIndex() != 3) {
            sheet.setActiveSheetIndex(3);
        }
        new ConditionalFormatter(sheet).createConditionalFormatterRules();
    }
}
