package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

/**
 * Fixture to set locked false to the default cellstyle.
 *
 */
public class DefaultStyleUnlockedFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        spreadsheet.getWorkbook().getCellStyleAt((short) 0).setLocked(false);
        spreadsheet.reload();
    }
}
