package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface SheetRenamedCallback {

    void apply(int sheetIndex, String newName);

}
