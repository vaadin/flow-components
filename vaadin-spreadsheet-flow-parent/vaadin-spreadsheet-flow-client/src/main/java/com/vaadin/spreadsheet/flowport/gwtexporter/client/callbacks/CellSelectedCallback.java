package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface CellSelectedCallback {

    void apply(int row, int column, boolean oldSelectionRangeDiscarded);

}
