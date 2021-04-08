package com.example.application.views.demoUI.fixtures;


import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Fixture to disable charts overlays.
 *
 */
public class DisableChartsFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.setChartsEnabled(false);
    }
}
