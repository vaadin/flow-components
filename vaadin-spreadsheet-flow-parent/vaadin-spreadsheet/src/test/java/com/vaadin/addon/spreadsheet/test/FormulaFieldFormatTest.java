package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.testbench.By;
import com.vaadin.testbench.elements.ComboBoxElement;

/**
 * Test for formula field formatting.
 *
 */
public class FormulaFieldFormatTest extends AbstractSpreadsheetTestCase {

    SpreadsheetPage spreadsheetPage;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setDefaultLocale();
        assertLocale(Locale.US);
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
        // Note: these might change with #18175 and #17012
        assertFormat("A3", "###", "1.23456789199999E11");
        assertFormat("A10", "###", "12345.6789199999");
        assertFormat("A15", "0.1234567892", "0.123456789199999");
        assertFormat("E3", "999999999999", "9.99999999999E11");
        assertFormat("E10", "99999.9999999", "99999.9999999");
        assertFormat("E14", "10", "9.99999999999");
    }

    @Test
    public void dateFormat_sheetWithDateFormatRuleForDateCells_formulaFieldContentsSimpleDateFormat() {
        spreadsheetPage = headerPage.loadFile("date_format.xlsx", this);
        assertFormat("A5", "14-Mar-14", "14/03/14 00:00");
        assertFormat("A10", "3/14/14 12:00 AM", "14/03/14 00:00");
        assertFormat("A1", "3/14", "14/03/14 00:00");
    }

    private void assertFormat(String cell, String cellValue,
            String formulaFieldValue) {
        assertEquals("Unexpected cell content,", cellValue,
                spreadsheetPage.getCellValue(cell));
        spreadsheetPage.clickOnCell(cell);
        assertEquals("Unexpected formula bar value,", formulaFieldValue,
                spreadsheetPage.getFormulaFieldValue());
    }

    private void assertLocale(Locale locale) {
        assertEquals(
                locale.getDisplayName(),
                $(ComboBoxElement.class).id("localeSelect")
                        .findElement(By.tagName("input")).getAttribute("value"));
    }

    private void setDefaultLocale() {
        WebElement localeSelect = $(ComboBoxElement.class).id("localeSelect")
                .findElement(By.tagName("input"));
        localeSelect.click();
        localeSelect.sendKeys(Locale.US.getDisplayName());
        localeSelect.sendKeys(Keys.ARROW_DOWN);
        localeSelect.sendKeys(Keys.RETURN);
    }

}
