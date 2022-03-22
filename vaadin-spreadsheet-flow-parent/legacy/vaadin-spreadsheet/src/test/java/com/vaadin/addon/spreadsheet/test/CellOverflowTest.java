package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.testbench.parallel.Browser;

public class CellOverflowTest extends AbstractSpreadsheetTestCase {

    @Test
    public void cellTextInput_longHtmlText_inputWrappedAndShownAsText()
            throws IOException {
        headerPage.createNewSpreadsheet();

        final SpreadsheetElement spreadsheetElement = $(
                SpreadsheetElement.class).first();

        final SheetCellElement a1 = spreadsheetElement.getCellAt("A1");
        final SheetCellElement b1 = spreadsheetElement.getCellAt("B1");

        a1.setValue("<span>Fooooooooooooooooooooooooo</span>");
        b1.setValue("bar");

        compareScreen("longHtmlTextWrapped");
    }

    @Test
    public void cellTextInput_htmlText_renderedAsText() throws IOException {
        headerPage.createNewSpreadsheet();

        final SpreadsheetElement spreadsheetElement = $(
                SpreadsheetElement.class).first();

        final SheetCellElement a1 = spreadsheetElement.getCellAt("A1");

        a1.setValue("<span>Foo</span>");

        compareScreen("htmlText");
    }

    @Test
    public void cellOverflow_stringFormula_overflowsAsText()
            throws IOException {
        headerPage.createNewSpreadsheet();

        final String valueToTest = "aaaaabbbbccccddddeeee";
        final SpreadsheetElement spreadsheetElement = $(
                SpreadsheetElement.class).first();

        spreadsheetElement.getCellAt("B1").setValue(valueToTest);

        final SheetCellElement a1 = spreadsheetElement.getCellAt("A1");

        a1.setValue("=B1");
        assertEquals(valueToTest, a1.getValue());
    }

    @Test
    public void frozenRows_LongValueInCell_CellOverflows() throws IOException {
        skipBrowser("IE fails to select correct file", Browser.IE11);
        headerPage.loadFile("frozen-rows-overflow.xlsx", this);
        compareScreen("overflow");
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
        final SpreadsheetElement spr = $(SpreadsheetElement.class).first();

        final SheetCellElement cellElement = spr.getCellAt(cell);

        Assert.assertEquals("hidden", cellElement.getCssValue("overflow"));
    }

    private void loadWrapTextTest() {
        headerPage.loadFile("wrap_text_test.xlsx", this);
    }
}
