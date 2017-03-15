package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import java.io.IOException;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.annotations.RunLocally;
import com.vaadin.testbench.parallel.Browser;

public class ConditionalFormattingBasedOnFormulaTest
    extends AbstractSpreadsheetTestCase {

    private static final String VALUE = "'Fooooooooooooooooooooooooo";
    private static final String FALSE_CONDITION_COLOR = "rgba(255, 255, 255, 1)";
    private static final String TRUE_CONDITION_COLOR = "rgba(255, 0, 0, 1)";

    private SpreadsheetElement spreadSheet;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        headerPage.loadFile(
            "conditional_formatting_with_formula_on_second_sheet.xlsx", this);
        spreadSheet = $(SpreadsheetElement.class).first();
        spreadSheet.selectSheetAt(1);
    }

    @Test
    public void loadSpreadsheetWithConditionalFormattingInA2_MakeConditionFalse_CellA2FilledWhite()
        throws IOException {

        final SheetCellElement a1 = spreadSheet.getCellAt("A1");
        final SheetCellElement a2 = spreadSheet.getCellAt("A2");

        a1.setValue(VALUE);
        a2.setValue("'Not" + VALUE);

        String cellColor = spreadSheet.getCellAt("A2")
            .getCssValue("background-color");

        assertEquals(FALSE_CONDITION_COLOR, cellColor);
    }

    @Test
    public void loadSpreadsheetWithConditionalFormattingInA2_MakeConditionTrue_CellA2FilledRed()
        throws IOException {

        final SheetCellElement a1 = spreadSheet.getCellAt("A1");
        final SheetCellElement a2 = spreadSheet.getCellAt("A2");

        a1.setValue(VALUE);
        a2.setValue(VALUE);

        String cellColor = spreadSheet.getCellAt("A2")
            .getCssValue("background-color");

        assertEquals(TRUE_CONDITION_COLOR, cellColor);
    }
}
