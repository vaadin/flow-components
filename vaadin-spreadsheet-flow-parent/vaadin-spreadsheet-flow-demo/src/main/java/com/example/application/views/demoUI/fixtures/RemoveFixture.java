package com.example.application.views.demoUI.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class RemoveFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.deleteSheet(1);
    }
}
