package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class ConditionalFormattingTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetElement spreadSheet;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        headerPage.loadFile("conditional_formatting_with_Invalid_formula.xlsx", this);
        spreadSheet = $(SpreadsheetElement.class).first();
    }

    @Test
    public void unsupportedFormula_parse_noAffectCondtionalFormat() {
        String value = "rgba(255, 235, 156, 1)";
        assertEquals(value, spreadSheet.getCellAt("B1").getCssValue("background-color"));
    }

    @Test
    public void unsupportedFormula_parse_noErrorIndicator() {
        assertNoErrorIndicatorDetected();
    }

}
