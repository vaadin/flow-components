package com.vaadin.addon.spreadsheet.test.fixtures;

import java.io.Serializable;

import com.vaadin.addon.spreadsheet.PopupButton;
import com.vaadin.addon.spreadsheet.Spreadsheet;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Table;

@SuppressWarnings("serial")
public class TablePopupButtonFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        spreadsheet
                .addSelectionChangeListener(new Spreadsheet.SelectionChangeListener() {
                    @Override
                    public void onSelectionChange(
                            Spreadsheet.SelectionChangeEvent selectionChangeEvent) {
                        BeanItemContainer<ItemThing> beans = new BeanItemContainer<ItemThing>(
                                ItemThing.class);
                        beans.addBean(new ItemThing("A"));
                        beans.addBean(new ItemThing("B"));

                        final Table content = new Table("",
                                beans);
                        content.setHeight("200px");

                        PopupButton popupButton = new PopupButton(content);
                        spreadsheet.setPopup(
                                spreadsheet.getSelectedCellReference(),
                                popupButton);
                    }
                });
    }

    public class ItemThing implements Serializable {
        private String string;

        ItemThing(String s) {
            string = s;
        }

        public String getString() {
            return string;
        }
    }
}
