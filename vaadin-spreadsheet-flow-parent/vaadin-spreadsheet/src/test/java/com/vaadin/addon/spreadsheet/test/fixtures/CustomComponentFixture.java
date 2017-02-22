package com.vaadin.addon.spreadsheet.test.fixtures;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.SpreadsheetComponentFactory;
import com.vaadin.data.HasValue;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;


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
        if(row == 11 && col ==1) {
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

        spreadsheet.setSpreadsheetComponentFactory(new ComponentEditorFactory());

        spreadsheet.refreshAllCellValues();
    }

}

abstract class AbstractComponentFactory implements SpreadsheetComponentFactory {

    @Override
    public Component getCustomComponentForCell(Cell cell, int rowIndex, int columnIndex, Spreadsheet spreadsheet, Sheet sheet) {
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
                comp  = new TextArea();
                break;
            }
            case "checkbox": {
                comp = new CheckBox();
                break;
            }
            case "datefield": {
                comp = new DateField();
                break;
            }
            case "slider": {
                comp = new Slider();
                break;
            }
            case "combobox": {
                comp = createCombo(spreadsheet, rowIndex, columnIndex);
                break;
            }
        }
        if(comp != null && comp instanceof HasValue) {
            HasValue<?> tmp=(HasValue<?>) comp;
            tmp.addValueChangeListener(e -> {
                spreadsheet.createCell(rowIndex, columnIndex, e.getValue());
                spreadsheet.refreshCells(spreadsheet.getCell(rowIndex, columnIndex));

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
            switch (CustomComponentFixture.getCompByCell(rowIndex, columnIndex)) {
                case "textfield": {
                    @SuppressWarnings("unchecked")
                    final TextField field = (TextField) customEditor;
                    String value = spreadsheet.getCellValue(curCell);
                    field.setValue(value);
                    break;
                }
                case "textarea": {
                    @SuppressWarnings("unchecked")
                    final TextArea ta = (TextArea) customEditor;
                    String value = spreadsheet.getCellValue(curCell);
                    ta.setValue(value);
                    break;
                }
                case "checkbox": {
                    @SuppressWarnings("unchecked")
                    final CheckBox cb = (CheckBox) customEditor;
                    String value = spreadsheet.getCellValue(curCell);
                    cb.setValue(Boolean.parseBoolean(value));
                    break;
                }
                case "datefield": {
                    final DateField df = (DateField) customEditor;

                    String value = spreadsheet.getCellValue(curCell);
                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("yyyy-MM-DD");
                    LocalDate date = LocalDate.parse(value, formatter);
                                     df.setValue(date);
                    break;
                }

                case "slider": {
                    final Slider slider = (Slider) customEditor;
                    String value = spreadsheet.getCellValue(curCell);

                    slider.setValue(Double.parseDouble(value));
                    break;
                }
                case "combobox": {
                    @SuppressWarnings("unchecked")
                    final NativeSelect<String> select = (NativeSelect) customEditor;
                    String value = spreadsheet.getCellValue(curCell);
                    select.setValue(value);
                    break;
                }
            }

        }

    }

    private NativeSelect<String> createCombo(Spreadsheet spreadsheet, int row,
            int col) {
        NativeSelect<String> cb = new NativeSelect<>();
        String[] keys = {"10","20","30","40","50"};
        List<String> items = new ArrayList<>(Arrays.asList(keys));
        cb.setItems(items);
        cb.addValueChangeListener(e -> {
            spreadsheet.createCell(row, col, e.getValue());
            spreadsheet.refreshCells(spreadsheet.getCell(row, col));
        });
        cb.setEmptySelectionAllowed(false);
        cb.setSizeFull();
        return cb;
    }

}



