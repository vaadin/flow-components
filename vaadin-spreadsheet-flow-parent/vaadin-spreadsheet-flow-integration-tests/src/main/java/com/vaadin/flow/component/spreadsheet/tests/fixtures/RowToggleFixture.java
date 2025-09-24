/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.util.CellReference;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;

public class RowToggleFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        List<Integer> rowIndexes = new ArrayList<Integer>();

        for (CellReference cellRef : spreadsheet.getSelectedCellReferences()) {
            if (!rowIndexes.contains(cellRef.getRow())) {
                rowIndexes.add(cellRef.getRow());
            }
        }

        for (Integer row : rowIndexes) {
            spreadsheet.setRowHidden(row, !spreadsheet.isRowHidden(row));
        }

        spreadsheet.refreshAllCellValues();
    }
}
