/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextField;

public class AdjacentCustomEditorsFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        spreadsheet.setShowCustomEditorOnFocus(true);
        spreadsheet.setSpreadsheetComponentFactory(new CustomEditorFactory());
    }

    private static class CustomEditorFactory
            implements SpreadsheetComponentFactory {

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            if (rowIndex != 1 || columnIndex < 1 || columnIndex > 5) {
                return null;
            }
            var textField = new TextField();
            textField.addValueChangeListener(
                    e -> spreadsheet.refreshCells(spreadsheet
                            .createCell(rowIndex, columnIndex, e.getValue())));
            return textField;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (cell == null) {
                return;
            }
            String cellValue = cell.getStringCellValue();
            ((HasValue) customEditor).setValue(cellValue);
        }
    }
}
