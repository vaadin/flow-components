package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface ColumnAddedToSelectionCallback {

    void apply(int firstRowIndex, int column);

}
