package com.example.application.views.demoUI.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Fixture to hide the second row.
 *
 */
public class HideSecondRowFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.setRowHidden(1, true);
    }
}
