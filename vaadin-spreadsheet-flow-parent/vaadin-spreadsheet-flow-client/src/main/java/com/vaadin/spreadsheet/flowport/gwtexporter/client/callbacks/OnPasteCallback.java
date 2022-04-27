package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface OnPasteCallback {

    void apply(String text);

}
