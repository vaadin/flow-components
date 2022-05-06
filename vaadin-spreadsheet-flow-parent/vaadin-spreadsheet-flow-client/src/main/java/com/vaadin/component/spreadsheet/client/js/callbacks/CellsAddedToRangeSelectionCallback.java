package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface CellsAddedToRangeSelectionCallback {

    void apply(int row1, int col1, int row2, int col2);

}
