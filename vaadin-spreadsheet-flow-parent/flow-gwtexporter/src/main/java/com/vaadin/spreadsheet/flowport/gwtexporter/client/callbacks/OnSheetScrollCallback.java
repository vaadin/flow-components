package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface OnSheetScrollCallback {

    void apply(int firstRow, int firstColumn, int lastRow, int lastColumn);

}
