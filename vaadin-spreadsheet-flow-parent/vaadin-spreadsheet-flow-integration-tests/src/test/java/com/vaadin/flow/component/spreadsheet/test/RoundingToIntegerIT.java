package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class RoundingToIntegerIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        createNewSpreadsheet();
        loadFile("rounding.xlsx");
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
        assertFormulaFieldContents("=B1-B2", "B4");
        assertFormulaFieldContents("=B1-B3", "B5");

        assertCellContents("0", "C5");
        assertFormulaFieldContents("=C1-C3", "C5");
    }

    private void assertCellContents(String value, String cell) {
        Assert.assertEquals("Unexpected cell contents,", value,
                getCellValue(cell));
    }

    private void assertFormulaFieldContents(String value, String cell) {
        clickCell(cell);
        Assert.assertEquals("Unexpected formula,", value,
                getFormulaFieldValue());
    }
}
