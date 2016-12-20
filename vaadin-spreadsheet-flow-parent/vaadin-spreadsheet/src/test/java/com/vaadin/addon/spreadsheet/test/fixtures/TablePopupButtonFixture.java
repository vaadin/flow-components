package com.vaadin.addon.spreadsheet.test.fixtures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.addon.spreadsheet.PopupButton;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Grid;

@SuppressWarnings("serial")
public class TablePopupButtonFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        spreadsheet
                .addSelectionChangeListener(e -> {
                    List<ItemThing> items = new ArrayList<>();
                    items.add(new ItemThing("A"));
                    items.add(new ItemThing("A"));
                    Grid<ItemThing> content = new Grid<>();
                    content.setDataProvider(new ListDataProvider<>(items));
                    content.setHeight("200px");
                    int columnIndex =spreadsheet.getSelectedCellReference().getCol();
                    int columnWidth = (int) spreadsheet.getActiveSheet().getColumnWidthInPixels(columnIndex);

                    content.setWidth(columnWidth, Sizeable.Unit.PIXELS);
                    content.addColumn(ItemThing::getValue).setCaption("Foo");
                    PopupButton popupButton = new PopupButton(content);
                    spreadsheet.setPopup(
                            spreadsheet.getSelectedCellReference(),
                            popupButton);
                });
    }

    public class ItemThing implements Serializable {
        private String value;

        ItemThing(String s) {
            value = s;
        }

        public String getValue() {
            return value;
        }
    }
}
