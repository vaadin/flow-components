package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface RowAddedToRangeSelectionCallback {

    void apply(int row, int firstColumnIndex);

}
