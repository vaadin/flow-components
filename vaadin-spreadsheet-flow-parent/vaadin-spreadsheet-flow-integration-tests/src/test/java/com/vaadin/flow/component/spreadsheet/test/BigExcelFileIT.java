package com.vaadin.flow.component.spreadsheet.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BigExcelFileIT extends AbstractSpreadsheetIT {

    @Before
    public void init() {
        getDriver().get(getBaseURL());
    }

    @Test
    public void openSpreadsheet_fromExcelFileWith_100_000_Rows_theContentIsRendered()
            throws Exception {
        loadFile("100_000_rows.xlsx");

        Assert.assertEquals("File opened", getCellContent("A1"));
    }
}
