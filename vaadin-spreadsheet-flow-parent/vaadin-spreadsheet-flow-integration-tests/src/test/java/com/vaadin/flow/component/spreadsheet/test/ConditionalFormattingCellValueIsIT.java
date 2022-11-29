package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;

@TestPath("vaadin-spreadsheet")
public class ConditionalFormattingCellValueIsIT extends AbstractSpreadsheetIT {

    private static final String STRING_VALUE = "'Foo";
    private static final String NUMBER_VALUE = "1";
    private static final String DIFFERENT_NUMBER_VALUE = "2";
    private static final String TRUE_VALUE = "TRUE";
    private static final String FALSE_VALUE = "FALSE";
    private static final String FALSE_CONDITION_COLOR = "rgba(255, 255, 255, 1)";
    private static final String TRUE_CONDITION_COLOR = "rgba(255, 0, 0, 1)";

    @Before
    public void init() {
        open();

        loadFile("conditional_formatting_cell_is.xlsx");
        selectSheetAt(1);
    }

    @Test
    public void loadSpreadsheetWithEqualConditionFormattingInB3_MakeConditionFalse_CellB3FilledWhite() {
        setCellValue("B2", STRING_VALUE);
        setCellValue("B3", "Not" + STRING_VALUE);

        setCellValue("D2", NUMBER_VALUE);
        setCellValue("D3", DIFFERENT_NUMBER_VALUE);

        setCellValue("F2", TRUE_VALUE);
        setCellValue("F3", FALSE_VALUE);

        String cellColorStringCase = getCellColor("B3");
        String cellColorNumberCase = getCellColor("D3");
        String cellColorBooleanCase = getCellColor("F3");

        Assert.assertEquals(FALSE_CONDITION_COLOR, cellColorStringCase);
        Assert.assertEquals(FALSE_CONDITION_COLOR, cellColorNumberCase);
        Assert.assertEquals(FALSE_CONDITION_COLOR, cellColorBooleanCase);
    }

    @Test
    public void loadSpreadsheetWithEqualConditionFormattingInB3_MakeConditionTrue_CellB3FilledRed() {
        setCellValue("B2", STRING_VALUE);
        setCellValue("B3", STRING_VALUE);

        setCellValue("D2", NUMBER_VALUE);
        setCellValue("D3", NUMBER_VALUE);

        setCellValue("F2", TRUE_VALUE);
        setCellValue("F3", TRUE_VALUE);

        String cellColorStringCase = getCellColor("B3");
        String cellColorNumberCase = getCellColor("D3");
        String cellColorBooleanCase = getCellColor("F3");

        Assert.assertEquals(TRUE_CONDITION_COLOR, cellColorStringCase);
        Assert.assertEquals(TRUE_CONDITION_COLOR, cellColorNumberCase);
        Assert.assertEquals(TRUE_CONDITION_COLOR, cellColorBooleanCase);
    }

    @Test
    public void loadSpreadsheetWithNotEqualConditionFormattingInB4_insertIncoherentValue_CellB4FilledRed() {
        setCellValue("B2", STRING_VALUE);
        setCellValue("B4", NUMBER_VALUE);
        Assert.assertEquals(TRUE_CONDITION_COLOR, getCellColor("B4"));
    }
}
