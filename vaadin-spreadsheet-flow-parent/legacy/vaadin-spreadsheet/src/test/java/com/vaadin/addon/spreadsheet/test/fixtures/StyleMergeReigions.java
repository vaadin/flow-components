package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class StyleMergeReigions implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.addMergedRegion(2,0,3,0);
    }
}
