package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class FormulaTest extends AbstractSpreadsheetTestCase {

    @Test
    public void testSimpleFormulaSheet() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "formulasheet.xlsx", this);
        spreadsheetPage.clickOnCell("A1");
        assertEquals("1", spreadsheetPage.getCellValue("A1"));
        assertEquals("1", spreadsheetPage.getFormulaFieldValue());

        spreadsheetPage.clickOnCell("B1");
        assertEquals("2", spreadsheetPage.getCellValue("B1"));
        assertEquals("=A1+1", spreadsheetPage.getFormulaFieldValue());

        spreadsheetPage.clickOnCell("C8");
        assertEquals("10", spreadsheetPage.getCellValue("C8"));
        assertEquals("=C7+1", spreadsheetPage.getFormulaFieldValue());
    }
}
