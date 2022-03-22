package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class FormulaTest extends AbstractSpreadsheetTestCase {

    @Test
    public void testSimpleFormulaSheet() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage
                .loadFile("formulasheet.xlsx", this);
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

    @Test
    public void validValueReference_invalidIsSet_formulaIsUpdated()
            throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.createNewSpreadsheet();
        SheetCellElement cellA1 = spreadsheetPage.getCellAt(1, 1);
        SheetCellElement cellB1 = spreadsheetPage.getCellAt(2, 1);

        // Initial setup: A1=3, A2=A1
        cellA1.setValue("3");
        cellB1.setValue("=A1");

        // Change A1 to an invalid formula
        cellA1.setValue("=A+2");
        // Check reference to A1 was updated
        assertEquals("=A+2", cellB1.getValue());
    }
}
