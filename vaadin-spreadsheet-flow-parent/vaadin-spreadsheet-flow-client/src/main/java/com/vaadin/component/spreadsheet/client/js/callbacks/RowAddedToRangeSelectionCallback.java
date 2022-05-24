package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface RowAddedToRangeSelectionCallback {

    void apply(int row, int firstColumnIndex);

}
