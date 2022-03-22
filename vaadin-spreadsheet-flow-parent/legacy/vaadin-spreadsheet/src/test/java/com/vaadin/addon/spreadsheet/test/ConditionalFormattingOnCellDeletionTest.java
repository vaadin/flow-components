package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class ConditionalFormattingOnCellDeletionTest extends AbstractSpreadsheetTestCase {

    private static final String FALSE_CONDITION_COLOR = "rgba(255, 255, 255, 1)";
    private static final String TRUE_CONDITION_COLOR = "rgba(255, 0, 0, 1)";

    private SpreadsheetPage spreadsheetPage;

    @Test
    public void conditionalFormatting_deleteCellUsedInFormula_formattingAppliedWithoutException() {
        spreadsheetPage = headerPage.loadFile(
                "conditional_formatting_with_formula_on_second_sheet.xlsx",
                this);

        assertEquals(FALSE_CONDITION_COLOR, spreadsheetPage.getCellColor("A2"));

        spreadsheetPage.deleteCellValue("A2");

        assertEquals(FALSE_CONDITION_COLOR, spreadsheetPage.getCellColor("A2"));

        spreadsheetPage.deleteCellValue("A1");

        assertEquals(TRUE_CONDITION_COLOR, spreadsheetPage.getCellColor("A2"));
    }
}
