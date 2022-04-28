package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

public class SimpleCustomEditorFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        spreadsheet.setSpreadsheetComponentFactory(new ComponentFactory());

        List<Cell> cellsToRefresh = new ArrayList<Cell>();
        cellsToRefresh.add(spreadsheet.createCell(0, 0, 1));

        // this has no effect. sheet must be scrolled to "redisplay" the cell.
        spreadsheet.refreshCells(cellsToRefresh);
    }

    @SuppressWarnings("serial")
    public static class ComponentFactory
            implements SpreadsheetComponentFactory {

        private final TextField textField = new TextField("");

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            if (cell != null) {
                // textField.focus();
                return textField;
            }
            return null;
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (cell != null) {
                String cellValue = spreadsheet.getCellValue(cell);
                textField.setValue(cellValue);
            }
        }
    }

}
