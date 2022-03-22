package com.vaadin.addon.spreadsheet.test;

import java.io.IOException;

import org.junit.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.addon.spreadsheet.test.pageobjects.SpreadsheetPage;
import com.vaadin.testbench.parallel.BrowserUtil;

public class BordersWithHiddenColumnsAndRowsTest
    extends AbstractSpreadsheetTestCase {

    private SpreadsheetPage spreadsheetPage;

    @Test
    public void test() throws Exception {
        spreadsheetPage = headerPage
            .loadFile("borders_with_hidden_rows_and_columns.xlsx", this);

        spreadsheetPage.selectSheetAt(0);
        compareScreen("sheet1_row_3_hidden");

        if (!isPhantomOrFF()) {
            // Context click doesn't work in those FF and Phantom
            compareSheet1WithRow3Shown();
            compareSheet1WithColumnHShown();
        }

        spreadsheetPage.selectSheetAt(1);
        compareScreen("sheet2_initial");

        if (!isPhantomOrFF()) {
            // Context click doesn't work in those FF and Phantom
            compareSheet2WithRowsShown();
            compareSheet2WithColumnsShown();
        }
    }

    private boolean isPhantomOrFF() {
        DesiredCapabilities capabilities = getDesiredCapabilities();
        return BrowserUtil.isFirefox(capabilities)
                || BrowserUtil.isPhantomJS(capabilities);
    }

    private void compareSheet1WithRow3Shown()
        throws IOException {
        spreadsheetPage.contextClickOnRowHeader(2);

        spreadsheetPage.clickContextMenuItem("Unhide row 3");

        compareScreen("sheet1_row_3_shown");
    }

    private void compareSheet1WithColumnHShown()
        throws IOException {

        spreadsheetPage.contextClickOnColumnHeader('G');

        spreadsheetPage.clickContextMenuItem("Unhide column H");

        compareScreen("sheet1_column_H_shown");
    }

    private void compareSheet2WithRowsShown()
        throws IOException {
        spreadsheetPage.unhideRow(5);
        spreadsheetPage.unhideRow(4);
        spreadsheetPage.unhideRow(3);

        compareScreen("sheet2_rows_shown");
    }

    private void compareSheet2WithColumnsShown()
        throws IOException {
        spreadsheetPage.unhideColumn('H');
        spreadsheetPage.unhideColumn('G');

        compareScreen("sheet2_columns_shown");
    }
}
