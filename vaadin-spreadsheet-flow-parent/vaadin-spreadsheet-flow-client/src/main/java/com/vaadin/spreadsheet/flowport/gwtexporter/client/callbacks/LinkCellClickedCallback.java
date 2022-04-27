package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface LinkCellClickedCallback {

    void apply(int row, int column);

}
