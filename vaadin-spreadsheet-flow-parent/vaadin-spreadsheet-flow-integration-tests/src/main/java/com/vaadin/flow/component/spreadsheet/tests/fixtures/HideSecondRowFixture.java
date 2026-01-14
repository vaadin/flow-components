/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;

import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.framework.Action;

/**
 * Fixture to hide the second row.
 *
 */
public class HideSecondRowFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        var newCells = new ArrayList<Cell>();
        for (int row = 2; row < 150 + 2; row++) {
            newCells.add(spreadsheet.createCell(row, 1, row));
        }

        spreadsheet.refreshCells(newCells);

        // for (int row = 10; row < 100; row++) {
        // spreadsheet.setRowHidden(row, true);
        // }

        spreadsheet.addActionHandler(new Action.Handler() {

            @Override
            public Action[] getActions(Object target, Object sender) {
                return new Action[] { new Action("Hide rows 10-99") };
            }

            @Override
            public void handleAction(Action action, Object sender,
                    Object target) {
                if (!"Hide rows 10-99".equals(action.getCaption())) {
                    return;
                }
                for (int row = 10; row < 100; row++) {
                    spreadsheet.setRowHidden(row, true);
                }
            }

        });
    }
}
