package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface OnPasteCallback {

    void apply(String text);

}
