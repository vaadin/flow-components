package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.vaadin.addon.spreadsheet.elements.SheetCellElement;
import com.vaadin.addon.spreadsheet.elements.SheetHeaderElement;
import com.vaadin.addon.spreadsheet.elements.SpreadsheetElement;
import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

import org.junit.Test;

public class FreezePaneTest extends AbstractSpreadsheetTestCase {

    @Test
    public void addFreezePane_verticalAndHorizontal_firstHeaderIsPlacedCorrectly() throws Exception {
        headerPage.createNewSpreadsheet();

        headerPage.addFreezePane();

        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();
        SheetHeaderElement firstColumnHeader = spreadsheetElement.getColumnHeader(1);
        SheetHeaderElement firstRowHeader = spreadsheetElement.getRowHeader(1);
        assertEquals("A", firstColumnHeader.getText());
        assertEquals("0px", firstColumnHeader.getWrappedElement().getCssValue("left"));
        assertEquals("1", firstRowHeader.getText());
        assertEquals("0px", firstRowHeader.getWrappedElement().getCssValue(
                "top"));
    }

    @Test
    public void addFreezePane_onlyVertical_firstHeaderIsPlacedCorrectly() throws Exception {
        headerPage.createNewSpreadsheet();

        headerPage.addFreezePane(0,1);

        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();
        SheetHeaderElement firstColumnHeader = spreadsheetElement.getColumnHeader(1);
        SheetHeaderElement firstRowHeader = spreadsheetElement.getRowHeader(1);
        assertEquals("A", firstColumnHeader.getText());
        assertEquals("0px", firstColumnHeader.getWrappedElement().getCssValue("left"));
        assertEquals("1", firstRowHeader.getText());
        assertEquals("0px", firstRowHeader.getWrappedElement().getCssValue("top"));
    }

    @Test
    public void addFreezePane_onlyHorizontal_firstHeaderIsPlacedCorrectly() throws Exception {
        headerPage.createNewSpreadsheet();

        headerPage.addFreezePane(1,0);

        SpreadsheetElement spreadsheetElement = $(SpreadsheetElement.class).first();
        SheetHeaderElement firstColumnHeader = spreadsheetElement.getColumnHeader(1);
        SheetHeaderElement firstRowHeader = spreadsheetElement.getRowHeader(1);
        assertEquals("A", firstColumnHeader.getText());
        assertEquals("0px", firstColumnHeader.getWrappedElement().getCssValue("left"));
        assertEquals("1", firstRowHeader.getText());
        assertEquals("0px", firstRowHeader.getWrappedElement().getCssValue("top"));
    }
}
