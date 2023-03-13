package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ConditionalFormattingOnCellDeletionIT
        extends AbstractSpreadsheetIT {

    private static final String FALSE_CONDITION_COLOR = "rgba(255, 255, 255, 1)";
    private static final String TRUE_CONDITION_COLOR = "rgba(255, 0, 0, 1)";

    @Before
    public void init() {
        open();

        loadFile("conditional_formatting_with_formula_on_second_sheet.xlsx");
    }

    @Test
    public void conditionalFormatting_deleteCellUsedInFormula_formattingAppliedWithoutException() {
        Assert.assertEquals(FALSE_CONDITION_COLOR, getCellColor("A2"));

        deleteCellValue("A2");

        Assert.assertEquals(FALSE_CONDITION_COLOR, getCellColor("A2"));

        deleteCellValue("A1");

        Assert.assertEquals(TRUE_CONDITION_COLOR, getCellColor("A2"));
    }
}
