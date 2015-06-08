package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

@Ignore("Value setting fails on Phantom and Firefox")
public class ConditionalFormatterTBTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetElement spreadSheet;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        headerPage.loadFile("conditional_formatting.xlsx", this);

        spreadSheet = $(SpreadsheetElement.class).first();
    }

    @Test
    public void conditionalFormattingForFormulaCell_updateFormulaReference_formattingApplied()
            throws Exception {
        SheetCellElement formulaCell = spreadSheet.getCellAt("A3");
        SheetCellElement referedCell = spreadSheet.getCellAt("B3");

        // formula cell is white to start with
        assertEquals("rgba(255, 255, 255, 1)",
                formulaCell.getCssValue("background-color"));

        referedCell.setValue("10");
        assertEquals("10", referedCell.getValue());

        // after formula is updated the background is red
        assertEquals("rgba(255, 199, 206, 1)",
                formulaCell.getCssValue("background-color"));
    }

    @Test
    public void conditionalFormattingForStringCell_equalsWithQuotationMarks_formattingMatches() {
        SheetCellElement targetCell = spreadSheet.getCellAt("A5");

        // formula cell is white to start with
        assertEquals("rgba(255, 255, 255, 1)",
                targetCell.getCssValue("background-color"));

        // wrong capitalisation, but comparison should ignore
        targetCell.setValue("o");
        assertEquals("o", targetCell.getValue());

        // after formula is updated the background is red
        assertEquals("rgba(255, 199, 206, 1)",
                targetCell.getCssValue("background-color"));
    }
}
