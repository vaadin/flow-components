/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class SelectionFixture implements SpreadsheetFixture {
    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        for (CellReference cellRef : spreadsheet.getSelectedCellReferences()) {
            spreadsheet.createCell(cellRef.getRow(), cellRef.getCol(),
                    "SELECTED");
        }
        spreadsheet.refreshAllCellValues();
    }
}
