package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface CellsAddedToRangeSelectionCallback {

    void apply(int row1, int col1, int row2, int col2);

}
