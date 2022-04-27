package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface SheetCreatedCallback {

    void apply(int scrollLeft, int scrollTop);

}
