package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface ColumnAddedToSelectionCallback {

    void apply(int firstRowIndex, int column);

}
