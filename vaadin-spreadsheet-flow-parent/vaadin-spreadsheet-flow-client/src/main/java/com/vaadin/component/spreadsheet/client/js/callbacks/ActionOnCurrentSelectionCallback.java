package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface ActionOnCurrentSelectionCallback {

    void apply(String actionKey);

}
