/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;

import com.vaadin.flow.component.spreadsheet.testbench.SheetCellElement;
import com.vaadin.flow.testutil.TestPath;

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
    public void longText_nextCellEmpty_textRunsIntoIt() {
        // A spreadsheet does not clip text at the column edge; it carries on
        // across the empty cells beside it.
        getSpreadsheet().getCellAt("A1")
                .setValue("a value far too wide for one column");

        SheetCellElement a1 = getSpreadsheet().getCellAt("A1");
        Assert.assertEquals("visible", a1.getCssValue("overflow"));
        // And the text really is drawn past the cell, not merely allowed to
        // be: the element holding it is wider than the cell it sits in.
        Assert.assertTrue("The text should be drawn wider than its own column",
                renderedTextWidth(a1) > a1.getSize().getWidth());
    }

    /**
     * Width of the element the cell's text is drawn in, which is the cell
     * itself until the text runs past it.
     */
    private long renderedTextWidth(SheetCellElement cell) {
        Object width = ((JavascriptExecutor) getDriver()).executeScript(
                "const held = arguments[0].querySelector('.cell-spill');"
                        + "const box = held ? held : arguments[0];"
                        + "return Math.round(box.getBoundingClientRect().width);",
                cell);
        return ((Number) width).longValue();
    }

    @Test
    public void longText_nextCellTaken_isHeldAtItsColumnsWidth() {
        getSpreadsheet().getCellAt("B1").setValue("in the way");
        getSpreadsheet().getCellAt("A1")
                .setValue("a value far too wide for one column");

        // As the GWT client held it: as wide as the column, from the padding
        // on, so it reaches past the cell's edge and under the neighbour.
        SheetCellElement a1 = getSpreadsheet().getCellAt("A1");
        Assert.assertEquals(a1.getSize().getWidth(), renderedTextWidth(a1));
        Assert.assertEquals("visible", a1.getCssValue("overflow"));
    }

    @Test
    public void longText_nextCellTaken_isCutShortWithAnEllipsis() {
        getSpreadsheet().getCellAt("B1").setValue("in the way");
        getSpreadsheet().getCellAt("A1")
                .setValue("a value far too wide for one column");

        // The GWT client read "a value f..." here. Clipped plainly, the
        // text ends mid-letter and nothing says there is more of it.
        SheetCellElement a1 = getSpreadsheet().getCellAt("A1");
        Object textOverflow = ((JavascriptExecutor) getDriver()).executeScript(
                "const held = arguments[0].querySelector('.cell-spill');"
                        + "return held ? getComputedStyle(held).textOverflow"
                        + " : null;",
                a1);
        Assert.assertEquals("ellipsis", textOverflow);
    }

    @Test
    public void longText_nextCellTaken_staysUnderTheNeighbour() {
        getSpreadsheet().getCellAt("B1").setValue("in the way");
        getSpreadsheet().getCellAt("A1")
                .setValue("a value far too wide for one column");

        // The neighbour comes later and covers what reaches past the edge;
        // raised above it, the text would run on over "in the way".
        SheetCellElement a1 = getSpreadsheet().getCellAt("A1");
        SheetCellElement b1 = getSpreadsheet().getCellAt("B1");
        Assert.assertTrue(zIndex(a1) <= zIndex(b1));
    }

    private static int zIndex(SheetCellElement cell) {
        String z = cell.getCssValue("z-index");
        return "auto".equals(z) ? 0 : Integer.parseInt(z);
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
