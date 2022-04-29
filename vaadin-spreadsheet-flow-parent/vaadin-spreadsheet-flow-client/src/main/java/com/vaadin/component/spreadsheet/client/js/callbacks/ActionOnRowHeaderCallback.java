package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface ActionOnRowHeaderCallback {

    void apply(String actionKey);
}
