package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface CellValueEditedCallback {

    void apply(int row, int col, String value);

}
