package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class FormulaTest extends AbstractSpreadsheetTestCase {

    @Test
    public void testSimpleFormulaSheet() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "formulasheet.xlsx", this);
        spreadsheetPage.clickOnCell("A2");
        assertEquals("1", spreadsheetPage.getCellValue("A1"));
        spreadsheetPage.clickOnCell("A1");
        assertEquals("1", spreadsheetPage.getFormulaFieldValue());

        assertEquals("2", spreadsheetPage.getCellValue("B1"));
        spreadsheetPage.clickOnCell("B1");
        assertEquals("=A1+1", spreadsheetPage.getFormulaFieldValue());

        assertEquals("10", spreadsheetPage.getCellValue("C8"));
        spreadsheetPage.clickOnCell("C8");
        assertEquals("=C7+1", spreadsheetPage.getFormulaFieldValue());
    }
}
