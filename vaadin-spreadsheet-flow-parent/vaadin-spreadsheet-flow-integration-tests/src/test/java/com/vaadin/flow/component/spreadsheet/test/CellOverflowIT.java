package com.vaadin.flow.component.spreadsheet.test;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

@TestPath("vaadin-spreadsheet")
public class CellOverflowIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();

        createNewSpreadsheet();
    }

    @Test
    public void cellOverflow_stringFormula_overflowsAsText()
            throws IOException {

        String valueToTest = "aaaaabbbbccccddddeeee";

        getSpreadsheet().getCellAt("B1").setValue(valueToTest);

        SheetCellElement a1 = getSpreadsheet().getCellAt("A1");

        a1.setValue("=B1");
        Assert.assertEquals(valueToTest, a1.getValue());
    }

    @Test
    public void verticalOverflowCells_noOverflow() {
        loadWrapTextTest();

        assertNoOverflowForCell("C4");
        assertNoOverflowForCell("C13");
    }

    @Test
    public void longWordInCellWithWrapText_noOverflow() {
        loadWrapTextTest();
        assertNoOverflowForCell("E8");
    }

    @Test
    public void sameContentInTwoCellsWithDifferentWidths_noOverflow() {
        loadWrapTextTest();

        assertNoOverflowForCell("E4");
        assertNoOverflowForCell("E13");
    }

    private void assertNoOverflowForCell(String cell) {
        SheetCellElement cellElement = getSpreadsheet().getCellAt(cell);

        Assert.assertEquals("hidden", cellElement.getCssValue("overflow"));
    }

    private void loadWrapTextTest() {
        loadFile("wrap_text_test.xlsx");
    }
}
