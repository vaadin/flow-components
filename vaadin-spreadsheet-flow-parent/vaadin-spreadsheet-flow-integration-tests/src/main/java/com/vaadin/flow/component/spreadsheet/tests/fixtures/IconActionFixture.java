/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.apache.poi.ss.util.CellRangeAddress;

import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.framework.Action;
import com.vaadin.flow.component.spreadsheet.tests.SpreadsheetActionHandler;
import com.vaadin.flow.theme.lumo.LumoIcon;

/**
 * Fixture that verifies Actions created with Icon constructors render icons in
 * the context menu.
 */
public class IconActionFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {

        SpreadsheetActionHandler handler = new SpreadsheetActionHandler();

        // Row action with a Lumo icon
        handler.addRowHandler(new SpreadsheetActionHandler.Row() {
            @Override
            public Action[] getActions(CellRangeAddress target,
                    Spreadsheet sender) {
                return new Action[] {
                        new Action("Row action", LumoIcon.CALENDAR.create()) };
            }

            @Override
            public void handleAction(Action action, CellRangeAddress sender,
                    Spreadsheet target) {
                // no-op
            }
        });

        // Cell action with Lumo icon
        handler.addCellHandler(new SpreadsheetActionHandler.Cell() {
            @Override
            public void handleAction(Action action,
                    Spreadsheet.SelectionChangeEvent sender,
                    Spreadsheet target) {
                // no-op
            }

            @Override
            public Action[] getActions(
                    Spreadsheet.SelectionChangeEvent selection,
                    Spreadsheet sender) {
                return new Action[] {
                        new Action("Lumo icon", LumoIcon.ANGLE_UP.create()) };
            }
        });

        // Cell action with Vaadin icon
        handler.addCellHandler(new SpreadsheetActionHandler.Cell() {
            @Override
            public void handleAction(Action action,
                    Spreadsheet.SelectionChangeEvent sender,
                    Spreadsheet target) {
                // no-op
            }

            @Override
            public Action[] getActions(Spreadsheet.SelectionChangeEvent target,
                    Spreadsheet sender) {
                return new Action[] { new Action("Vaadin icon",
                        VaadinIcon.ACADEMY_CAP.create()) };
            }
        });

        // Column action without icon to ensure mixed items render fine
        handler.addColumnHandler(new SpreadsheetActionHandler.Column() {
            @Override
            public void handleAction(Action action, CellRangeAddress sender,
                    Spreadsheet target) {
                // no-op
            }

            @Override
            public Action[] getActions(CellRangeAddress target,
                    Spreadsheet sender) {
                return new Action[] {
                        new Action("Column action", LumoIcon.COG.create()) };
            }
        });

        spreadsheet.removeDefaultActionHandler();
        spreadsheet.addActionHandler(handler);
    }

}
