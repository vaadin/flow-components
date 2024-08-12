/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See  {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
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
