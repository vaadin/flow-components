package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface CellAddedToSelectionAndSelectedCallback {

    void apply(int row, int column);

}
