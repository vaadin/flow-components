package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface CellSelectedCallback {

    void apply(int row, int column, boolean oldSelectionRangeDiscarded);

}
