package com.vaadin.addon.spreadsheet.test;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;

public class BigExcelFileTest extends AbstractSpreadsheetTestCase {

    //TODO unignore
    @Test
    @Ignore
    public void openSpreadsheet_fromExcelFileWith_100_000_Rows_theContentIsRendered() throws Exception {
        SpreadsheetPage spreadsheetPage = headerPage.loadFile(
                "100_000_rows.xlsx", this);

        assertEquals("File opened", spreadsheetPage.getCellValue("A1"));
    }
}
