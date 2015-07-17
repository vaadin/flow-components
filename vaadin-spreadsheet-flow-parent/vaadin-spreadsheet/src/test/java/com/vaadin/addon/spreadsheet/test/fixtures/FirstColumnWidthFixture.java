package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.Spreadsheet;

/**
 * Fixture to decrease the first column size.
 *
 */
public class FirstColumnWidthFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.setColumnWidth(0, 60);
    }
}
