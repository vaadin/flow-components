package com.vaadin.addon.spreadsheet.test.junit;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.ConditionalFormatter;
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
}
