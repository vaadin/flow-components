package com.vaadin.addon.spreadsheet.test.fixtures;

import com.vaadin.addon.spreadsheet.Spreadsheet;

public class RemoveFixture implements SpreadsheetFixture{

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.deleteSheet(1);
    }
}
