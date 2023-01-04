package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

public class CustomComponentFixture implements SpreadsheetFixture {

    static final int HEADER_ROW_INDEX = 0;

    static final int COMPONENT_ROW_INDEX = 1;

    static final int COMPONENT_COLUMN_INDEX = 1;

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        spreadsheet.createCell(HEADER_ROW_INDEX, COMPONENT_COLUMN_INDEX,
                "Button");
        spreadsheet
                .setSpreadsheetComponentFactory(new CustomComponentFactory());
    }

    private static class CustomComponentFactory
            implements SpreadsheetComponentFactory {

        private final Button button = new Button("Click",
                e -> Notification.show("Button clicked!"));

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (rowIndex != COMPONENT_ROW_INDEX
                    || columnIndex != COMPONENT_COLUMN_INDEX) {
                return null;
            }
            return button;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            return null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
        }
    }
}