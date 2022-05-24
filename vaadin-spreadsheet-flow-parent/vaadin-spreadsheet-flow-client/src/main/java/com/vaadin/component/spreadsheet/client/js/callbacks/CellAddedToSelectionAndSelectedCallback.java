package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface CellAddedToSelectionAndSelectedCallback {

    void apply(int row, int column);

}
