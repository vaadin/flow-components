package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface ColumnHeaderContextMenuOpenCallback {

    void apply(int columnIndex);

}
