package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface SheetSelectedCallback {

    void apply(int sheetIndex, int scrollLeft, int scrollTop);

}
