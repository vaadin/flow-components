package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface ColumnSelectedCallback {

    void apply(int column, int firstRowIndex);

}
