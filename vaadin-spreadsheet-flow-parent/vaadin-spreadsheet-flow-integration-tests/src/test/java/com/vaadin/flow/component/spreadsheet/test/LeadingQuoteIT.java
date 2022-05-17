package com.vaadin.flow.component.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebElement;

public class LeadingQuoteIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
        loadFile("leading_quotes.xlsx");
    }

    @Test
    public void existingCell_numberStringWithQuotePrefixStyle_formulaBarAndInlineEditorShowALeadingQuote() {

        assertCellValues("B3", "01", "'01", "'01");
    }

    @Test
    public void existingCell_stringWithQuotePrefixStyle_formulaBarAndInlineEditorShowALeadingQuote() {

        assertCellValues("B4", "abc", "'abc", "'abc");
    }

    @Test
    public void existingCell_stringWithoutQuotePrefixStyle_noLeadingQuote() {

        assertCellValues("B5", "def", "def", "def");
    }

    @Test
    public void typingOnCell_withLeadingQuote_noQuoteShownInCellAndInPOIModel() {

        final String cell = "D15";

        setCellValue(cell, "'567");

        assertCellValues(cell, "567", "'567", "'567");
    }

    @Test
    public void typingOnCell_withTwoLeadingQuotes_justOneQuoteShownInCellAndInPOIModel() {

        final String cell = "D10";

        setCellValue(cell, "''567");

        assertCellValues(cell, "'567", "''567", "''567");
    }

    @Test
    public void existingCell_selectedCellWithQuotePrefixStyle_formulaBarAndInlineEditorShowALeadingQuoteAfterSheetSelection() {

        final String cell = "B3";

        selectSheetAt(0);
        selectCell(cell);

        // Switch to another sheet and then switching again to the initial one
        selectSheetAt(1);
        selectSheetAt(0);

        assertCellValues(cell, "01", "'01", "'01");
    }

    private void assertCellValues(String cell, String cellValue,
            String formulaBarValue, String inlineEditorValue) {

        assertEquals(cellValue, getCellValue(cell));

        assertEquals(formulaBarValue, getFormulaBarValue(cell));

        assertEquals(inlineEditorValue, getInlineEditorValue(cell));
    }

    private String getInlineEditorValue(String cell) {
        WebElement cellEditor = getInlineEditor(cell);
        return cellEditor.getAttribute("value");
    }

    private String getFormulaBarValue(String cell) {
        selectCell(cell);
        return getFormulaFieldValue();
    }
}
