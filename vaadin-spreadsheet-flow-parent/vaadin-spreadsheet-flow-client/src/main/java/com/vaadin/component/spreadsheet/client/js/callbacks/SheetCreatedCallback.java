package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface SheetCreatedCallback {

    void apply(int scrollLeft, int scrollTop);

}
