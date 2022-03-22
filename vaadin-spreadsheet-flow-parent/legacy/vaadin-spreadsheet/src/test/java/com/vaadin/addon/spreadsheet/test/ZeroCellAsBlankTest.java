package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class ZeroCellAsBlankTest extends AbstractSpreadsheetTestCase {

    @Test
    public void zeroCellAsBlank_loadSheetWithSettingOn_zeroExpected() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "show-zero-cells.xlsx", this);

        spreadsheetPage.selectSheetAt(0);

        assertEquals("2", spreadsheetPage.getCellValue("B2"));
        assertEquals("0", spreadsheetPage.getCellValue("B3"));
        assertEquals("0", spreadsheetPage.getCellValue("B4"));
        assertEquals("0", spreadsheetPage.getCellValue("B5"));
    }

    @Test
    public void zeroCellAsBlank_loadSheetWithSettingOn_emptyExpected() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "show-zero-cells.xlsx", this);

        spreadsheetPage.selectSheetAt(1);

        assertEquals("2", spreadsheetPage.getCellValue("B2"));
        assertEquals("", spreadsheetPage.getCellValue("B3"));
        assertEquals("0", spreadsheetPage.getCellValue("B4"));
        assertEquals("", spreadsheetPage.getCellValue("B5"));
    }
}
