package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface CellValueEditedCallback {

    void apply(int row, int col, String value);

}
