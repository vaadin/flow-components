package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.testutil.TestPath;

@TestPath("vaadin-spreadsheet")
public class BigExcelFileIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        open();
    }

    @Test
    public void openSpreadsheet_fromExcelFileWith_100_000_Rows_theContentIsRendered()
            throws Exception {
        loadFile("100_000_rows.xlsx");

        Assert.assertEquals("File opened", getCellContent("A1"));
    }
}
