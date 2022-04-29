package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PopupButtonFixture implements SpreadsheetFixture {

    private static final List<String> VALUES = Arrays.asList(new String[] {
            "One", "Two", "Three", "Four", "Five", "Six", "Seven" });

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        spreadsheet.addSelectionChangeListener(event -> {
            if (event.getAllSelectedCells().size() != 1) {
                return;
            }
            List<String> values = new ArrayList<>(VALUES);
            CellReference ref = event.getSelectedCellReference();
            CellReference newRef = new CellReference(ref.getRow(),
                    ref.getCol());
            DataValidationButton popupButton = new DataValidationButton(
                    spreadsheet, values);
            popupButton.setUp();
            event.getSpreadsheet().setPopup(newRef, popupButton);
            popupButton.openPopup();
        });
    }
}

class DataValidationButton extends PopupButton {
    private final CellListSelectComponent cellListSelectComponent;

    public DataValidationButton(Spreadsheet parent, Collection<String> values) {
        super();
        cellListSelectComponent = new CellListSelectComponent(values, this,
                parent);
    }

    public void setUp() {
        setHeaderHidden(false);
        cellListSelectComponent.setSizeFull();
        setContent(cellListSelectComponent);
    }

}

class CellListSelectComponent extends CustomField<String> {
    private static final long serialVersionUID = 1L;
    private final ComboBox<String> listSelect;
    private Collection<String> values;

    public CellListSelectComponent(Collection<String> values,
            DataValidationButton context, Spreadsheet sheet) {
        super();
        this.values = values;
        listSelect = new ComboBox<>();
        listSelect.addValueChangeListener(event -> {
            CellListSelectComponent.this.setValue(event.getValue());
            Cell cell = sheet.getCell(context.getRow(), context.getColumn());
            if (cell == null) {
                cell = sheet.createCell(context.getRow(), context.getColumn(),
                        event.getValue());
            }
            sheet.refreshCells(cell);
            context.closePopup();
        });
        listSelect.setDataProvider(new ListDataProvider<>(values));
        listSelect.setSizeFull();
    }

    @Override
    public String getValue() {
        return listSelect.getValue();
    }

    @Override
    protected String generateModelValue() {
        return null;
    }

    @Override
    protected void setPresentationValue(String s) {

    }
}
