package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface ActionOnColumnHeaderCallback {

    void apply(String actionKey);

}
