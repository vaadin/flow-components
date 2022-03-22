package com.vaadin.addon.spreadsheet.test;

import org.junit.Test;

public class RowColumnStylesTest extends AbstractSpreadsheetTestCase {

    @Test
    public void styles_sheetHasRowAndColumnStyles_spreadsheetIsRenderedCorrectly()
            throws Exception {
        headerPage.loadFile("row_and_column_styles.xlsx", this);

        compareScreen("row_and_column_styles");
    }
}
