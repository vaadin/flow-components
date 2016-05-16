package com.vaadin.addon.spreadsheet.test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import org.junit.Test;

import java.io.IOException;

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
}
