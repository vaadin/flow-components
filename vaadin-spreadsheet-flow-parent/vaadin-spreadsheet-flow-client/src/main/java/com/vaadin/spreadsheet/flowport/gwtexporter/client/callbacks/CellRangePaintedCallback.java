package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface CellRangePaintedCallback {

    void apply(int selectedCellRow, int selectedCellColumn, int row1, int col1, int row2, int col2);

}
