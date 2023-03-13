package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

import java.io.Serializable;

@SuppressWarnings("serial")
public class TablePopupButtonFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {

        spreadsheet.addSelectionChangeListener(e -> {
            PopupButton popupButton = new PopupButton(new Span("TABLE"));
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
