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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellAddress;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextField;

public class CustomEditorSharedFixture implements SpreadsheetFixture {

    static final int COMPONENT_ROW_INDEX = 1;
    static final int COMPONENT_COL_INDEX = 1;

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        System.out.println("Loading CustomEditorRowFixture");
        spreadsheet.setSpreadsheetComponentFactory(new CustomEditorFactory());
        spreadsheet.setColumnWidth(0, 200);
        spreadsheet.setShowCustomEditorOnFocus(true);

        var cells = new ArrayList<Cell>();
        cells.add(spreadsheet.createCell(COMPONENT_ROW_INDEX,
                COMPONENT_COL_INDEX, ""));
        cells.add(spreadsheet.createCell(COMPONENT_ROW_INDEX + 1,
                COMPONENT_COL_INDEX, ""));
        spreadsheet.refreshCells(cells);
    }

    private static class CustomEditorFactory
            implements SpreadsheetComponentFactory {

        private TextField textField;

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            if (!sheet.getSheetName().equals("Sheet1")
                    || rowIndex < COMPONENT_ROW_INDEX
                    || rowIndex > COMPONENT_ROW_INDEX + 1
                    || columnIndex != COMPONENT_COL_INDEX) {
                return null;
            }

            if (textField == null) {
                textField = new TextField();
                textField.addValueChangeListener(e -> spreadsheet.refreshCells(
                        spreadsheet.createCell(activeCell.getRow(),
                                activeCell.getColumn(), e.getValue())));
            }
            return textField;
        }

        private CellAddress activeCell;

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (cell == null) {
                return;
            }

            activeCell = new CellAddress(rowIndex, columnIndex);

            if (customEditor instanceof TextField editor) {
                editor.setValue(cell.getStringCellValue());
            }
        }
    }

}
