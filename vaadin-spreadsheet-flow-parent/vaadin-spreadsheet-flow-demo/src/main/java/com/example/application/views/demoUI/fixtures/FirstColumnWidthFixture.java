package com.example.application.views.demoUI.fixtures;


import com.vaadin.flow.component.spreadsheet.Spreadsheet;

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
