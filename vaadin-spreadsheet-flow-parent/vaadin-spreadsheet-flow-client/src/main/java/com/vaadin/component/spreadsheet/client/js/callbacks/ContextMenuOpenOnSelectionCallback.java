package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface ContextMenuOpenOnSelectionCallback {

    void apply(int row, int column);

}
