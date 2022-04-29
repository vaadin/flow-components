package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface OnSheetScrollCallback {

    void apply(int firstRow, int firstColumn, int lastRow, int lastColumn);

}
