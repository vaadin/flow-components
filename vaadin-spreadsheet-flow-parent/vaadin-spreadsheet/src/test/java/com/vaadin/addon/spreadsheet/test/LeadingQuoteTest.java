package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.addon.spreadsheet.test.testutil.SheetController;

public class LeadingQuoteTest extends AbstractSpreadsheetTestCase {

    private SheetController sheetController;
    private SpreadsheetPage spreadsheetPage;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        sheetController = new SheetController(driver, testBench(driver),
            getDesiredCapabilities());
        spreadsheetPage = headerPage.loadFile("leading_quotes.xlsx", this);
        waitForElementPresent(By.className("v-spreadsheet"));
    }

    @Test
    public void existingCell_numberStringWithQuotePrefixStyle_formulaBarAndInlineEditorShowALeadingQuote()
        throws Exception {

        assertCellValues("B3", "01", "'01", "'01");
    }

    @Test
    public void existingCell_stringWithQuotePrefixStyle_formulaBarAndInlineEditorShowALeadingQuote()
        throws Exception {

        assertCellValues("B4", "abc", "'abc", "'abc");
    }

    @Test
    public void existingCell_stringWithoutQuotePrefixStyle_noLeadingQuote()
        throws Exception {

        assertCellValues("B5", "def", "def", "def");
    }

    @Test
    public void typingOnCell_withLeadingQuote_noQuoteShownInCellAndInPOIModel()
        throws Exception {

        final String cell = "D15";

        sheetController.putCellContent(cell, "'567");

        assertCellValues(cell, "567", "'567", "'567");
    }

    @Test
    public void typingOnCell_withTwoLeadingQuotes_justOneQuoteShownInCellAndInPOIModel()
        throws Exception {

        final String cell = "D10";

        sheetController.putCellContent(cell, "''567");

        assertCellValues(cell, "'567", "''567", "''567");
    }

    @Test
    public void existingCell_selectedCellWithQuotePrefixStyle_formulaBarAndInlineEditorShowALeadingQuoteAfterSheetSelection()
        throws Exception {

        final String cell = "B3";

        spreadsheetPage.selectSheetAt(0);
        sheetController.selectCell(cell);

        // Switch to another sheet and then switching again to the initial one
        spreadsheetPage.selectSheetAt(1);
        spreadsheetPage.selectSheetAt(0);

        assertCellValues(cell, "01", "'01", "'01");
    }

    private void assertCellValues(String cell, String cellValue,
        String formulaBarValue, String inlineEditorValue) {

        assertEquals(cellValue, spreadsheetPage.getCellValue(cell));

        assertEquals(formulaBarValue, getFormulaBarValue(cell));

        assertEquals(inlineEditorValue, getInlineEditorValue(cell));
    }

    private String getInlineEditorValue(String cell) {
        WebElement cellEditor = sheetController.getInlineEditor(cell);
        return cellEditor.getAttribute("value");
    }

    private String getFormulaBarValue(String cell) {
        sheetController.selectCell(cell);
        return spreadsheetPage.getFormulaFieldValue();
    }
}
