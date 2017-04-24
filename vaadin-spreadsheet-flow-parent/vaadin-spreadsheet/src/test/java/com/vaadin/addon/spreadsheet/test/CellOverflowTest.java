package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

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
}
