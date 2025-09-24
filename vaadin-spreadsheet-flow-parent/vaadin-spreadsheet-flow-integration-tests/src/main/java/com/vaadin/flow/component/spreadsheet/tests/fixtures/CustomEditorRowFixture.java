/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextField;

public class CustomEditorRowFixture implements SpreadsheetFixture {

    static final int COMPONENT_ROW_INDEX = 1;

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        spreadsheet.setSpreadsheetComponentFactory(new CustomEditorFactory());
        spreadsheet.setColumnWidth(0, 200);
        spreadsheet.setShowCustomEditorOnFocus(true);
    }

    private static class CustomEditorFactory
            implements SpreadsheetComponentFactory {

        private final Map<Integer, TextField> textFields = new HashMap<>();

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (rowIndex != 0 || columnIndex != 0) {
                return null;
            }

            Button toggleCustomEditorVisibilityButton = new Button(
                    "Toggle custom editor", event -> {
                        spreadsheet.setShowCustomEditorOnFocus(
                                !spreadsheet.isShowCustomEditorOnFocus());
                    });
            toggleCustomEditorVisibilityButton
                    .setId("toggleCustomEditorVisibilityButton");
            return toggleCustomEditorVisibilityButton;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            if (!sheet.getSheetName().equals("Sheet1")
                    || rowIndex != COMPONENT_ROW_INDEX) {
                return null;
            }
            if (!textFields.containsKey(columnIndex)) {
                var textField = new TextField();

                textField.addValueChangeListener(
                        e -> spreadsheet.refreshCells(spreadsheet.createCell(
                                rowIndex, columnIndex, e.getValue())));
                textField.setId("textField" + rowIndex + columnIndex);
                textFields.put(columnIndex, textField);
            }
            return textFields.get(columnIndex);
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (cell == null) {
                return;
            }

            if (customEditor instanceof TextField editor) {
                editor.setValue(cell.getStringCellValue());
            }
        }
    }

}
