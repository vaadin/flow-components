package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface RowSelectedCallback {

    void apply(int row, int firstColumnIndex);

}
