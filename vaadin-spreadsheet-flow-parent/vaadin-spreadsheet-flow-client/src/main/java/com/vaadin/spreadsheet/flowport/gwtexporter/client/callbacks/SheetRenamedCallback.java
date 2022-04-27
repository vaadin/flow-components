package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface SheetRenamedCallback {

    void apply(int sheetIndex, String newName);

}
