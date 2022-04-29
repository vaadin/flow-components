package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface ColumnSelectedCallback {

    void apply(int column, int firstRowIndex);

}
