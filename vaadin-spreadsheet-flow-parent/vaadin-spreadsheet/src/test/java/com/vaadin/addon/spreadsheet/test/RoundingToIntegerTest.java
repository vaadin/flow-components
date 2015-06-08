package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

/**
 * Test for rounding numbers when number format has no decimals.
 *
 */
public class RoundingToIntegerTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetPage spreadsheetPage;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        spreadsheetPage = headerPage.loadFile("rounding.xlsx", this);
    }

    @Test
    public void rounding_sheetWithZeroDecimalNumberFormatRuleForNumericCells_numbersRoundedUpCorrectly() {
        assertCellContents("5", "B2");
        assertCellContents("0", "C2");
    }

    @Test
    public void rounding_sheetWithZeroDecimalNumberFormatRuleForNumericCells_numbersRoundedDownCorrectly() {
        assertCellContents("5", "B3");
        assertCellContents("0", "C3");
    }

    @Test
    public void rounding_sheetWithZeroDecimalNumberFormatRuleForNumericCells_formulaResultsRoundedDownCorrectly() {
        assertCellContents("0", "B4");
        assertFormulaFieldContents("=B1-B2", "B4");

        assertCellContents("0", "C4");
        assertFormulaFieldContents("=C1-C2", "C4");
    }

    @Test
    public void rounding_sheetWithZeroDecimalNumberFormatRuleForNumericCells_formulaResultsRoundedUpCorrectly() {
        assertCellContents("0", "B5");
        assertFormulaFieldContents("=B1-B3", "B5");

        assertCellContents("0", "C5");
        assertFormulaFieldContents("=C1-C3", "C5");
    }

    private void assertCellContents(String value, String cell) {
        assertEquals("Unexpected cell contents,", value,
                spreadsheetPage.getCellValue(cell));
    }

    private void assertFormulaFieldContents(String value, String cell) {
        spreadsheetPage.clickOnCell(cell);
        assertEquals("Unexpected formula,", value,
                spreadsheetPage.getFormulaFieldValue());
    }

}
