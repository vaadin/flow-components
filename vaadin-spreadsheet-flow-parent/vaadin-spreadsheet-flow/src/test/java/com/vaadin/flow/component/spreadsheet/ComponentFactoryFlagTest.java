/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.Component;

/**
 * Whether the client is told that the sheet has a component factory: it then
 * asks only for the cells it draws, as the factory is asked about each cell
 * loaded, and what it returns is made and sent.
 */
class ComponentFactoryFlagTest {

    private final Spreadsheet spreadsheet = new Spreadsheet();

    private boolean told() {
        return spreadsheet.getElement().getProperty("hasComponentFactory",
                false);
    }

    private static SpreadsheetComponentFactory factory() {
        return new SpreadsheetComponentFactory() {
            @Override
            public Component getCustomComponentForCell(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                return null;
            }

            @Override
            public Component getCustomEditorForCell(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
                return null;
            }

            @Override
            public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                    int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                    Component customEditor) {
            }
        };
    }

    @Test
    void aNewSheet_isNotSaidToHaveOne() {
        assertFalse(told());
    }

    @Test
    void settingAFactory_tellsTheClient() {
        spreadsheet.setSpreadsheetComponentFactory(factory());

        assertTrue(told());
    }

    @Test
    void removingTheFactory_tellsTheClientToo() {
        spreadsheet.setSpreadsheetComponentFactory(factory());

        spreadsheet.setSpreadsheetComponentFactory(null);

        assertFalse(told());
    }
}
