package com.vaadin.addon.spreadsheet.test.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vaadin.addon.spreadsheet.PopupButton;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeListener;
import com.vaadin.addon.spreadsheet.test.demoapps.TestexcelsheetUI;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.ListSelect;

@SuppressWarnings("serial")
public class PopupButtonFixture implements SpreadsheetFixture {

    private DataValidationButton popupButton;
    private boolean popupButtonActive = false;
    private TestexcelsheetUI ui;
    private static final List<Object> VALUES = Arrays.asList(new Object[] {
            "One", "Two", "Three", "Four", "Five", "Six", "Seven" });

    public PopupButtonFixture(TestexcelsheetUI ui) {
        this.ui = ui;
    }

    private Cell retrieveCell(Sheet sheet, int row, int column) {
        Row r = sheet.getRow(row);
        if (r == null) {
            r = sheet.createRow(row);
        }
        Cell cell = r.getCell(column);
        if (cell == null) {
            cell = r.createCell(column);
        }
        return cell;
    }

    private class DataValidationButton extends PopupButton {
        private final CellListSelectComponent cellListSelectComponent;
        private final ValueChangeListenerImpl valueChangeListener = new ValueChangeListenerImpl();

        public DataValidationButton(Collection<Object> values) {
            super();
            cellListSelectComponent = new CellListSelectComponent(values, this);
        }

        public void setUp() {
            setHeaderHidden(false);
            cellListSelectComponent.setSizeFull();
            cellListSelectComponent.addValueChangeListener(valueChangeListener);
            addComponents(cellListSelectComponent);
        }

        private final class ValueChangeListenerImpl implements
                ValueChangeListener {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(ValueChangeEvent event) {
                Object value = event.getProperty().getValue();
                Cell cell = retrieveCell(ui.getSpreadsheet().getActiveSheet(),
                        getRow(), getColumn());
                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value instanceof String) {
                    cell.setCellValue((String) value);
                }
                ui.getSpreadsheet().refreshCells(cell);
            }
        }
    }

    @Override
    public void loadFixture(Spreadsheet spreadsheet) {
        try {
            final XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Sheet1");
            retrieveCell(sheet, 5, 5);
            if (spreadsheet == null) {
                spreadsheet = new Spreadsheet(workbook);
            } else {
                spreadsheet.setWorkbook(workbook);
            }

            spreadsheet
                    .addSelectionChangeListener(new SelectionChangeListener() {
                        @Override
                        public void onSelectionChange(SelectionChangeEvent event) {
                            List<Object> values = new ArrayList<Object>(VALUES);
                            CellReference ref = event
                                    .getSelectedCellReference();
                            Spreadsheet spreadsheet2 = ui.getSpreadsheet();
                            if (popupButtonActive) {
                                spreadsheet2.setPopup(ref, null);
                                popupButtonActive = false;
                            }
                            CellReference newRef = new CellReference(ref
                                    .getRow(), ref.getCol());
                            popupButton = new DataValidationButton(values);
                            popupButton.setUp();
                            popupButtonActive = true;
                            spreadsheet2.setPopup(newRef, popupButton);
                            popupButton.openPopup();
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public class CellListSelectComponent extends CustomField<Object> {
        private static final long serialVersionUID = 1L;
        private final ListSelect listSelect;
        private Collection<Object> values;
        private DataValidationButton dataValidationButton;

        public CellListSelectComponent(Collection<Object> values,
                DataValidationButton dataValidationButton) {
            super();
            this.values = values;
            this.dataValidationButton = dataValidationButton;
            listSelect = new ListSelect();
            listSelect.setMultiSelect(false);
            listSelect.setImmediate(true);
            listSelect
                    .addValueChangeListener(new Property.ValueChangeListener() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void valueChange(
                                com.vaadin.data.Property.ValueChangeEvent event) {
                            final Object value = listSelect.getValue();
                            setValue(value);
                            CellListSelectComponent.this.dataValidationButton
                                    .closePopup();
                        }
                    });
        }

        @Override
        protected Component initContent() {
            for (Object val : values) {
                listSelect.addItem(val);
            }
            listSelect.setRows(5);
            listSelect.setSizeFull();
            return listSelect;
        }

        public void changeValues(Collection<Object> values) {
            this.values = values;
            listSelect.removeAllItems();
            for (Object val : values) {
                listSelect.addItem(val);
            }
        }

        @Override
        public Class<? extends Object> getType() {
            return Object.class;
        }
    }

}