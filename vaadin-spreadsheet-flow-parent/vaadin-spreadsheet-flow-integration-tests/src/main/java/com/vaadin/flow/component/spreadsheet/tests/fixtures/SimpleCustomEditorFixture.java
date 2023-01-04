package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class SimpleCustomEditorFixture implements SpreadsheetFixture {

    static final int HEADER_ROW_INDEX = 0;

    static final int COMPONENT_ROW_INDEX = 1;

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        for (EditorType componentType : EditorType.values()) {
            spreadsheet.createCell(HEADER_ROW_INDEX,
                    componentType.getColumnIndex(),
                    componentType.getHeaderText());
        }
        spreadsheet.setSpreadsheetComponentFactory(new CustomEditorFactory());
    }

    private static class CustomEditorFactory
            implements SpreadsheetComponentFactory {

        private TextField textField;

        private TextArea textArea;

        private Checkbox checkbox;

        private DatePicker datePicker;

        private ComboBox<String> comboBox;

        @Override
        public Component getCustomComponentForCell(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
            return null;
        }

        @Override
        public Component getCustomEditorForCell(Cell cell, final int rowIndex,
                final int columnIndex, final Spreadsheet spreadsheet,
                Sheet sheet) {
            EditorType editorType = EditorType.getEditorTypeByIndex(rowIndex,
                    columnIndex);
            if (editorType == null) {
                return null;
            }
            return getCustomEditor(editorType, rowIndex, columnIndex,
                    spreadsheet);
        }

        @Override
        public void onCustomEditorDisplayed(Cell cell, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
                Component customEditor) {
            if (cell == null) {
                return;
            }
            ((HasValue) customEditor).setValue(
                    getCellValueForEditor(rowIndex, columnIndex, cell));
        }

        private Component getCustomEditor(EditorType editorType, int rowIndex,
                int columnIndex, Spreadsheet spreadsheet) {
            if (editorType != null) {
                switch (editorType) {
                case TEXT_FIELD:
                    if (textField == null) {
                        initTextField();
                        textField.addValueChangeListener(e -> spreadsheet
                                .refreshCells(spreadsheet.createCell(rowIndex,
                                        columnIndex, e.getValue())));
                    }
                    return textField;
                case CHECKBOX:
                    if (checkbox == null) {
                        initCheckbox();
                        checkbox.addValueChangeListener(e -> spreadsheet
                                .refreshCells(spreadsheet.createCell(rowIndex,
                                        columnIndex, e.getValue())));
                    }
                    return checkbox;
                case DATE_PICKER:
                    if (datePicker == null) {
                        initDatePicker();
                        datePicker.addValueChangeListener(e -> spreadsheet
                                .refreshCells(spreadsheet.createCell(rowIndex,
                                        columnIndex, e.getValue().format(
                                                DateTimeFormatter.ISO_DATE))));
                    }
                    return datePicker;
                case TEXT_AREA:
                    if (textArea == null) {
                        initTextArea();
                        textArea.addValueChangeListener(e -> spreadsheet
                                .refreshCells(spreadsheet.createCell(rowIndex,
                                        columnIndex, e.getValue())));
                    }
                    return textArea;
                case COMBO_BOX:
                    if (comboBox == null) {
                        initComboBox();
                        comboBox.addValueChangeListener(e -> spreadsheet
                                .refreshCells(spreadsheet.createCell(rowIndex,
                                        columnIndex, e.getValue())));
                    }
                    return comboBox;
                }
            }
            return null;
        }

        private Object getCellValueForEditor(int rowIndex, int columnIndex,
                Cell cell) {
            EditorType editorType = EditorType.getEditorTypeByIndex(rowIndex,
                    columnIndex);
            if (editorType == EditorType.DATE_PICKER) {
                return LocalDate.parse(cell.getStringCellValue(),
                        DateTimeFormatter.ISO_DATE);
            }
            if (editorType == EditorType.CHECKBOX) {
                return cell.getBooleanCellValue();
            }
            return cell.getStringCellValue();
        }

        private void initComboBox() {
            comboBox = new ComboBox<>();
            comboBox.setItems("10", "20", "30", "40", "50");
        }

        private void initTextArea() {
            textArea = new TextArea();
        }

        private void initDatePicker() {
            datePicker = new DatePicker();
        }

        private void initCheckbox() {
            checkbox = new Checkbox();
        }

        private void initTextField() {
            textField = new TextField();
        }
    }

    private enum EditorType {
        TEXT_FIELD(1, "TextField"), CHECKBOX(2, "Checkbox"), DATE_PICKER(3,
                "DatePicker"), TEXT_AREA(4,
                        "TextArea"), COMBO_BOX(5, "ComboBox");

        private final int columnIndex;

        private final String headerText;

        EditorType(int columnIndex, String headerText) {
            this.columnIndex = columnIndex;
            this.headerText = headerText;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public String getHeaderText() {
            return headerText;
        }

        public static EditorType getEditorTypeByIndex(int row, int col) {
            if (row != COMPONENT_ROW_INDEX) {
                return null;
            }
            return Arrays.stream(values())
                    .filter(type -> type.columnIndex == col).findAny()
                    .orElse(null);
        }
    }
}
