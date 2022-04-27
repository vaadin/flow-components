package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface ActionOnRowHeaderCallback {

    void apply(String actionKey);
}
