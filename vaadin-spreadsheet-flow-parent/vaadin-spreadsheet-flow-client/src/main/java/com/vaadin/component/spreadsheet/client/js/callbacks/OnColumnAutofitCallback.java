package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface OnColumnAutofitCallback {

    void apply(int columnIndex);

}
