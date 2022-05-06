package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class TablePopupButtonFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        spreadsheet.addSelectionChangeListener(e -> {
            List<ItemThing> items = new ArrayList<>();
            items.add(new ItemThing("A"));
            items.add(new ItemThing("A"));
            Grid<ItemThing> content = new Grid<>();
            content.setDataProvider(new ListDataProvider<>(items));
            content.setHeight("200px");
            int columnIndex = spreadsheet.getSelectedCellReference().getCol();
            int columnWidth = (int) spreadsheet.getActiveSheet()
                    .getColumnWidthInPixels(columnIndex);

            content.setWidth(columnWidth, Unit.PIXELS);
            content.addColumn(ItemThing::getValue).setHeader("Foo");
            PopupButton popupButton = new PopupButton(content);
            spreadsheet.setPopup(spreadsheet.getSelectedCellReference(),
                    popupButton);
        });
    }

    public static class ItemThing implements Serializable {
        private String value;

        ItemThing(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }
    }
}
