package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface SheetSelectedCallback {

    void apply(int sheetIndex, int scrollLeft, int scrollTop);

}
