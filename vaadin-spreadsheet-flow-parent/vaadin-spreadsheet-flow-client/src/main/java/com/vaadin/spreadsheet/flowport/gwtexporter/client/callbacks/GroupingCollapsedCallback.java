package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface GroupingCollapsedCallback {

    void apply(boolean cols, int colIndex, boolean collapsed);

}
