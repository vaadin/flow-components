package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface LinkCellClickedCallback {

    void apply(int row, int column);

}
