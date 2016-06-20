package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;

public class CustomFormatTest extends AbstractSpreadsheetTestCase {

    private SpreadsheetElement spreadSheet;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        headerPage.loadFile("custom_format.xlsx", this);

        spreadSheet = $(SpreadsheetElement.class).first();
    }

    @Test
    public void customFormatCell_enterPositiveNumber_becomesNumberType()
            throws Exception {
        SheetCellElement formatCell = spreadSheet.getCellAt("A1");
        formatCell.setValue("5");

        // numbers are right-aligned
        assertEquals("right", formatCell.getCssValue("text-align"));

        // value not formatted further
        assertEquals("5", formatCell.getValue());
    }

    @Test
    public void customFormatCell_enterNegativeNumber_getsFormatted()
            throws Exception {
        SheetCellElement formatCell = spreadSheet.getCellAt("A1");
        formatCell.setValue("-5");

        // drops minus and gains parentheses
        assertEquals("(5)", formatCell.getValue());
    }

    @Test
    @Ignore("not supported")
    public void customFormatCell_enterZero_getsFormatted() throws Exception {
        SheetCellElement formatCell = spreadSheet.getCellAt("A1");
        formatCell.setValue("0");

        // changes into minus, Excel also adds space on both sides and fills the
        // left side with whitespace
        assertEquals("-", formatCell.getValue());
    }

    @Test
    public void customFormatCell_enterText_noChange() throws Exception {
        SheetCellElement formatCell = spreadSheet.getCellAt("A1");
        formatCell.setValue("text");

        // Excel also adds space on both sides
        assertEquals("text", formatCell.getValue());
    }

}
