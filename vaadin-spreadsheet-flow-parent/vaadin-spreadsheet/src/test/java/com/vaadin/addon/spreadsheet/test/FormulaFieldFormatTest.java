package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

/**
 * Test for formula field formatting.
 *
 */
public class FormulaFieldFormatTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetPage spreadsheetPage;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setDefaultLocale();
    }

    @Test
    public void numberFormat_sheetWithNumberFormatRuleForNumericCells_formulaFieldContentsUnformattedExceptForLocale() {
        spreadsheetPage = headerPage.loadFile("number_format.xlsx", this);
        assertFormat("F3", "3,333.333", "3333.333");
        assertFormat("H3", "3,333.33 €", "3333.333");
    }

    @Test
    public void rounding_sheetWithNumberFormatRuleForNumericCells_formulaFieldContentsUnformatted() {
        spreadsheetPage = headerPage.loadFile("rounding.xlsx", this);
        assertFormat("B2", "5", "4.99999");
        assertFormat("B3", "5", "5.00005");
    }

    @Test
    public void rounding_sheetWithGeneralFormatRuleForNumericCells_formulaFieldContentsUnformattedExceptForLocale() {
        spreadsheetPage = headerPage.loadFile("general_round.xlsx", this);
        assertFormat("E3", "1E+12", "999999999999");
        assertFormat("E14", "10", "9.99999999999");
    }

    @Test
    public void dateFormat_sheetWithDateFormatRuleForDateCells_formulaFieldContentsSimpleDateFormat() {
        spreadsheetPage = headerPage.loadFile("date_format.xlsx", this);
//        assertFormat("A5", "14-Mar-14", "14/03/14 00:00");
//        assertFormat("A10", "3/14/14 12:00 AM", "14/03/14 00:00");
//        assertFormat("A1", "3/14", "14/03/14 00:00");

        // Cell values and formula field values equals with dates
        assertFormat("A5", "14-Mar-14", "14-Mar-14");
        assertFormat("A10", "3/14/14 12:00 AM", "3/14/14 12:00 AM");
        assertFormat("A1", "3/14", "3/14");
    }

    @Test
    public void formulaTrimming_invalidPOIFormula_formulaIsNotTrimmed() {
        spreadsheetPage = headerPage.loadFile("table-subtotals-ranges.xlsx",
                this);
        assertFormat("C4", "#VALUE!", "=SUBTOTAL(109,Sheet1!$C$2:$C$3)");
    }

    private void assertFormat(String cell, String cellValue,
            String formulaFieldValue) {
        assertEquals("Unexpected cell content,", cellValue,
                spreadsheetPage.getCellValue(cell));
        spreadsheetPage.clickOnCell(cell);
        assertEquals("Unexpected formula bar value,", formulaFieldValue,
                spreadsheetPage.getFormulaFieldValue());
    }

    private void setDefaultLocale() {
        setLocale(Locale.US);
    }

}
