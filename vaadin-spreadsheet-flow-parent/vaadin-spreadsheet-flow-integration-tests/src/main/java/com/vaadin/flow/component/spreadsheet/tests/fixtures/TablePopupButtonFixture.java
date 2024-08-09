/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import java.io.Serializable;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

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
