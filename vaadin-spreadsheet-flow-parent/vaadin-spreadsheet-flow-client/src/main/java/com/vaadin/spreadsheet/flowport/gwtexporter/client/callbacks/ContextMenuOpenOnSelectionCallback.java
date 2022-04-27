package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface ContextMenuOpenOnSelectionCallback {

    void apply(int row, int column);

}
