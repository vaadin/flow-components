package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Fixture to disable charts overlays.
 *
 */
public class DisableChartsFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        // TODO Re-enable when Charts implementation is done
        // spreadsheet.setChartsEnabled(false);
    }
}
