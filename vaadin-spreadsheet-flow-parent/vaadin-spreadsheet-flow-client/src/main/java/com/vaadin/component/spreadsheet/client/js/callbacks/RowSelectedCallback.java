package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface RowSelectedCallback {

    void apply(int row, int firstColumnIndex);

}
