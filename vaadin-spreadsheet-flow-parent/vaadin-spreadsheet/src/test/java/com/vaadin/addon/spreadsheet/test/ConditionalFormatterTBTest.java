package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.annotations.RunLocally;
import com.vaadin.testbench.parallel.Browser;

@RunLocally(Browser.FIREFOX)
public class ConditionalFormatterTBTest extends AbstractSpreadsheetTestCase {

    @Test
    public void conditionalFormattingForFormulaCell_updateFormulaReference_formattingApplied()
            throws Exception {
        headerPage.loadFile("conditional_formatting.xlsx", this);

        SpreadsheetElement element = $(SpreadsheetElement.class).first();

        SheetCellElement formulaCell = element.getCellAt("A3");
        SheetCellElement referedCell = element.getCellAt("B3");

        // formula cell is white to start with
        assertEquals("rgba(255, 255, 255, 1)",
                formulaCell.getCssValue("background-color"));

        referedCell.setValue("10");

        // after formula is updated the background is red
        assertEquals("rgba(255, 199, 206, 1)",
                formulaCell.getCssValue("background-color"));
    }
}
