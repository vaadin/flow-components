package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface GroupingCollapsedCallback {

    void apply(boolean cols, int colIndex, boolean collapsed);

}
