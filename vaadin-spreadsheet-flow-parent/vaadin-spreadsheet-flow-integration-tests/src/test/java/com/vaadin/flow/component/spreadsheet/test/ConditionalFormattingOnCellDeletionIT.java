package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConditionalFormattingOnCellDeletionIT
        extends AbstractSpreadsheetIT {

    private static final String FALSE_CONDITION_COLOR = "rgba(255, 255, 255, 1)";
    private static final String TRUE_CONDITION_COLOR = "rgba(255, 0, 0, 1)";

    @Before
    public void init() {
        getDriver().get(getBaseURL());

        loadFile("conditional_formatting_with_formula_on_second_sheet.xlsx");
    }

    @Test
    public void conditionalFormatting_deleteCellUsedInFormula_formattingAppliedWithoutException() {
        assertEquals(FALSE_CONDITION_COLOR, getCellColor("A2"));

        deleteCellValue("A2");

        assertEquals(FALSE_CONDITION_COLOR, getCellColor("A2"));

        deleteCellValue("A1");

        assertEquals(TRUE_CONDITION_COLOR, getCellColor("A2"));
    }
}
