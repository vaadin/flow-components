package com.vaadin.addon.spreadsheet.test.fixtures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;

import com.vaadin.addon.spreadsheet.PopupButton;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeEvent;
import com.vaadin.addon.spreadsheet.Spreadsheet.SelectionChangeListener;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.ListSelect;

@SuppressWarnings("serial")
public class PopupButtonFixture implements SpreadsheetFixture {

    private static final List<String> VALUES = Arrays.asList(new String[] {
            "One", "Two", "Three", "Four", "Five", "Six", "Seven" });

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        spreadsheet.addSelectionChangeListener(new SelectionChangeListener() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if(event.getAllSelectedCells().size() != 1) {
                    return;
                }
                List<String> values = new ArrayList<String>(VALUES);
                CellReference ref = event.getSelectedCellReference();
                CellReference newRef = new CellReference(ref.getRow(), ref
                        .getCol());
                DataValidationButton popupButton = new DataValidationButton(
                        values);
                popupButton.setUp();
                event.getSpreadsheet().setPopup(newRef, popupButton);
                popupButton.openPopup();
            }
        });
    }
}

class DataValidationButton extends PopupButton {
    private final CellListSelectComponent cellListSelectComponent;

    private final ValueChangeListener valueChangeListener = new ValueChangeListener() {
        private static final long serialVersionUID = 1L;

        @Override
        public void valueChange(ValueChangeEvent event) {
            String value = (String) event.getProperty().getValue();
            Spreadsheet sheet = (Spreadsheet) getParent();
            Cell cell = sheet.getCell(getRow(), getColumn());
            cell.setCellValue(value);
            sheet.refreshCells(cell);
        }
    };

    public DataValidationButton(Collection<String> values) {
        super();
        cellListSelectComponent = new CellListSelectComponent(values, this);
    }

    public void setUp() {
        setHeaderHidden(false);
        cellListSelectComponent.setSizeFull();
        cellListSelectComponent.addValueChangeListener(valueChangeListener);
        setContent(cellListSelectComponent);
    }

}

class CellListSelectComponent extends CustomField<String> {
    private static final long serialVersionUID = 1L;
    private final ListSelect listSelect;
    private Collection<String> values;
    private DataValidationButton dataValidationButton;

    public CellListSelectComponent(Collection<String> values,
            DataValidationButton dataValidationButton) {
        super();
        this.values = values;
        this.dataValidationButton = dataValidationButton;
        listSelect = new ListSelect();
        listSelect.setMultiSelect(false);
        listSelect.setImmediate(true);
        listSelect.addValueChangeListener(new Property.ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(
                    com.vaadin.data.Property.ValueChangeEvent event) {
                final String value = (String) listSelect.getValue();
                setValue(value);
                CellListSelectComponent.this.dataValidationButton.closePopup();
            }
        });
    }

    @Override
    protected Component initContent() {
        for (String val : values) {
            listSelect.addItem(val);
        }
        listSelect.setRows(5);
        listSelect.setSizeFull();
        return listSelect;
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }
}
