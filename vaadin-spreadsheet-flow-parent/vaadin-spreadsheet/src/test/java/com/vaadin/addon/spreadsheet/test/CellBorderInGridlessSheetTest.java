package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class CellBorderInGridlessSheetTest  extends AbstractSpreadsheetTestCase{
    
    @Test
    public void openSpreadsheet_fromExcelFileWith_bordersAndNoGrid_thereAreBorders() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "test_borders.xlsx", this);

        compareScreen("bordersAndNoGrid");
    }

}
