package com.vaadin.flow.component.spreadsheet.tests.fixtures;

import com.vaadin.flow.component.spreadsheet.PopupButton;
import com.vaadin.flow.component.spreadsheet.Spreadsheet;

@SuppressWarnings("serial")
public class TabsheetPopupButtonFixture implements SpreadsheetFixture {

    @Override
    public void loadFixture(final Spreadsheet spreadsheet) {
        spreadsheet.addSelectionChangeListener(
                new Spreadsheet.SelectionChangeListener() {
                    @Override
                    public void onSelectionChange(
                            Spreadsheet.SelectionChangeEvent selectionChangeEvent) {

                        PopupButton popupButton = new PopupButton();
                        // todo: ver que hacemos con esto
                        /*
                         * TabSheet tabSheet = new TabSheet();
                         * tabSheet.setId("tabsheet");
                         * tabSheet.setHeight("150px");
                         * tabSheet.setWidth("200px");
                         * popupButton.setContent(tabSheet); TabSheet.Tab tab1 =
                         * tabSheet.addTab(new Label("A"));
                         * tab1.setCaption("TAB1"); TabSheet.Tab tab2 =
                         * tabSheet.addTab(new Label("B"));
                         * tab2.setCaption("TAB2"); spreadsheet.setPopup(
                         * spreadsheet.getSelectedCellReference(), popupButton);
                         */
                    }
                });
    }
}
