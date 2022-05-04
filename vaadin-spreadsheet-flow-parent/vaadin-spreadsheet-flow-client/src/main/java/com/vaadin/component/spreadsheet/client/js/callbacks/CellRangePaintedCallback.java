package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface CellRangePaintedCallback {

    void apply(int selectedCellRow, int selectedCellColumn, int row1, int col1,
            int row2, int col2);

}
