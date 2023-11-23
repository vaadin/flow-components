package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class ConditionalFormatterIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        loadFile("conditional_formatting.xlsx");
    }

    @Test
    public void conditionalFormattingForFormulaCell_updateFormulaReference_formattingApplied()
            throws Exception {
        SheetCellElement formulaCell = getSpreadsheet().getCellAt("A3");
        SheetCellElement referedCell = getSpreadsheet().getCellAt("B3");

        // formula cell is white to start with
        Assert.assertEquals("rgba(255, 255, 255, 1)",
                formulaCell.getCssValue("background-color"));

        selectCell("B3");
        referedCell.setValue("10");

        Assert.assertEquals("10", referedCell.getValue());

        // after formula is updated the background is red
        Assert.assertEquals("rgba(255, 199, 206, 1)",
                formulaCell.getCssValue("background-color"));
    }

    @Test
    public void conditionalFormattingForStringCell_equalsWithQuotationMarks_formattingMatches() {
        SheetCellElement targetCell = getSpreadsheet().getCellAt("A5");

        // formula cell is white to start with
        Assert.assertEquals("rgba(255, 255, 255, 1)",
                targetCell.getCssValue("background-color"));

        // TODO: fix to avoid selection
        selectCell("A5");
        // wrong capitalization, but comparison should ignore
        targetCell.setValue("o");
        Assert.assertEquals("o", targetCell.getValue());

        // after formula is updated the background is red
        Assert.assertEquals("rgba(255, 199, 206, 1)",
                targetCell.getCssValue("background-color"));
    }

    @Test
    public void conditionalFormattingForNumberCell_formattingMatches() {
        SheetCellElement targetCell = getSpreadsheet().getCellAt("B8");
        // target cell has conditional formatting: when value > 10 then display
        // four decimal places
        Assert.assertEquals("12.0000", targetCell.getValue());
    }
}
