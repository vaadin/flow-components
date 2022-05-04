package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface SelectionDecreasePaintedCallback {

    void apply(int row, int col);

}
