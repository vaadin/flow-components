package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.component.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomComponentFixture implements SpreadsheetFixture {

    static public String getCompByCell(int row, int col) {
        if (row == 1 && col == 1) {
            return "textfield";
        }
        if (row == 1 && col == 2) {
            return "checkbox";
        }
        if (row == 1 && col == 3) {
            return "datefield";
        }
        if (row == 1 && col == 6) {
            return "textarea";
        }
        if (row == 1 && col == 7) {
            return "slider";
        }
        if (row == 1 && col == 8) {
            return "combobox";
        }
        if (row == 11 && col == 1) {
            return "label";
        }
        if (row == 1 && col == 9) {
            return "button";
        }
        return "";
    }

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        spreadsheet.createCell(0, 1, "TextField");
        spreadsheet.createCell(0, 2, "CheckBox");
        spreadsheet.createCell(0, 3, "DateField");
        spreadsheet.createCell(0, 6, "TextArea");
        spreadsheet.createCell(0, 7, "Slider");
        spreadsheet.createCell(0, 8, "ComboBox");

        spreadsheet
                .setSpreadsheetComponentFactory(new ComponentEditorFactory());

        spreadsheet.refreshAllCellValues();
    }

}

abstract class AbstractComponentFactory implements SpreadsheetComponentFactory {

    @Override
    public Component getCustomComponentForCell(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
        return null;
    }

}

class ComponentEditorFactory extends AbstractComponentFactory {

    @Override
    public Component getCustomEditorForCell(Cell cell, final int rowIndex,
            final int columnIndex, final Spreadsheet spreadsheet, Sheet sheet) {

        Component comp = null;
        switch (CustomComponentFixture.getCompByCell(rowIndex, columnIndex)) {

        case "textfield": {
            comp = new TextField();
            break;
        }
        case "textarea": {
            comp = new TextArea();
            break;
        }
        case "checkbox": {
            comp = new Checkbox();
            break;
        }
        case "datefield": {
            comp = new DatePicker();
            break;
        }
        case "slider": {
            Notification.show("slider is not supported");
            // comp = new Slider();
            break;
        }
        case "combobox": {
            comp = createCombo(spreadsheet, rowIndex, columnIndex);
            break;
        }
        }
        if (comp != null && comp instanceof HasValue) {
            HasValue tmp = (HasValue) comp;
            tmp.addValueChangeListener(e -> {
                spreadsheet.createCell(rowIndex, columnIndex, e.getValue());
                spreadsheet.refreshCells(
                        spreadsheet.getCell(rowIndex, columnIndex));

            });
        }
        return comp;
    }

    @Override
    public void onCustomEditorDisplayed(Cell cell, int rowIndex,
            int columnIndex, Spreadsheet spreadsheet, Sheet sheet,
            Component customEditor) {
        final Cell curCell = spreadsheet.getCell(rowIndex, columnIndex);
        if (curCell != null) {
            switch (CustomComponentFixture.getCompByCell(rowIndex,
                    columnIndex)) {
            case "textfield": {
                final TextField field = (TextField) customEditor;
                String value = spreadsheet.getCellValue(curCell);
                field.setValue(value);
                break;
            }
            case "textarea": {
                final TextArea ta = (TextArea) customEditor;
                String value = spreadsheet.getCellValue(curCell);
                ta.setValue(value);
                break;
            }
            case "checkbox": {
                final Checkbox cb = (Checkbox) customEditor;
                String value = spreadsheet.getCellValue(curCell);
                cb.setValue(Boolean.valueOf(value));
                break;
            }
            case "datefield": {
                final DatePicker df = (DatePicker) customEditor;

                String value = spreadsheet.getCellValue(curCell);
                DateTimeFormatter formatter = DateTimeFormatter
                        .ofPattern("yyyy-MM-DD");
                LocalDate date = LocalDate.parse(value, formatter);
                df.setValue(date);
                break;
            }

            case "slider": {
                Notification.show("slider is not supported");
                /*
                 * final Slider slider = (Slider) customEditor; String value =
                 * spreadsheet.getCellValue(curCell);
                 *
                 * slider.setValue(Double.valueOf(value));
                 */
                break;
            }
            case "combobox": {
                @SuppressWarnings("unchecked")
                final ComboBox<String> select = (ComboBox<String>) customEditor;
                String value = spreadsheet.getCellValue(curCell);
                select.setValue(value);
                break;
            }
            }

        }

    }

    private ComboBox<String> createCombo(Spreadsheet spreadsheet, int row,
            int col) {
        ComboBox<String> cb = new ComboBox<>();
        String[] keys = { "10", "20", "30", "40", "50" };
        List<String> items = new ArrayList<>(Arrays.asList(keys));
        cb.setItems(items);
        cb.addValueChangeListener(e -> {
            spreadsheet.createCell(row, col, e.getValue());
            spreadsheet.refreshCells(spreadsheet.getCell(row, col));
        });
        // cb.setEmptySelectionAllowed(false);
        cb.setSizeFull();
        return cb;
    }

}
