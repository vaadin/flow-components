package com.vaadin.flow.component.spreadsheet.test;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

/**
 * Test for formula field formatting.
 *
 */
@TestPath("vaadin-spreadsheet")
public class FormulaFieldFormatIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
        setDefaultLocale();
    }

    @Test
    public void numberFormat_sheetWithNumberFormatRuleForNumericCells_formulaFieldContentsUnformattedExceptForLocale() {
        loadFile("number_format.xlsx");
        assertFormat("F3", "3,333.333", "3333.333");
        assertFormat("H3", "3,333.33 €", "3333.333");
    }

    @Test
    public void rounding_sheetWithNumberFormatRuleForNumericCells_formulaFieldContentsUnformatted() {
        loadFile("rounding.xlsx");
        assertFormat("B2", "5", "4.99999");
        assertFormat("B3", "5", "5.00005");
    }

    @Test
    public void rounding_sheetWithGeneralFormatRuleForNumericCells_formulaFieldContentsUnformattedExceptForLocale() {
        loadFile("general_round.xlsx");
        assertFormat("E3", "1E+12", "999999999999");
        assertFormat("E14", "10", "9.99999999999");
    }

    @Test
    public void dateFormat_sheetWithDateFormatRuleForDateCells_formulaFieldContentsSimpleDateFormat() {
        loadFile("date_format.xlsx");

        // Cell values and formula field values equals with dates
        assertFormat("A5", "14-Mar-14", "14-Mar-14");
        assertFormat("A10", "3/14/14 12:00 AM", "3/14/14 12:00 AM");
        assertFormat("A1", "3/14", "3/14");
    }

    @Test
    public void formulaTrimming_invalidPOIFormula_formulaIsNotTrimmed() {
        loadFile("table-subtotals-ranges.xlsx");
        assertFormat("C4", "300", "=SUBTOTAL(109,Sheet1!$C$2:$C$3)");
    }

    private void assertFormat(String cell, String cellValue,
            String formulaFieldValue) {
        Assert.assertEquals("Unexpected cell content,", cellValue,
                getCellValue(cell));
        clickCell(cell);
        Assert.assertEquals("Unexpected formula bar value,", formulaFieldValue,
                getFormulaFieldValue());
    }

    private void setDefaultLocale() {
        setLocale(Locale.US);
    }

}