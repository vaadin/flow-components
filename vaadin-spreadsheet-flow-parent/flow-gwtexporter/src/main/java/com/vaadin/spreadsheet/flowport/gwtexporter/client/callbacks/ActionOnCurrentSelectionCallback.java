package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface ActionOnCurrentSelectionCallback {

    void apply(String actionKey);

}
