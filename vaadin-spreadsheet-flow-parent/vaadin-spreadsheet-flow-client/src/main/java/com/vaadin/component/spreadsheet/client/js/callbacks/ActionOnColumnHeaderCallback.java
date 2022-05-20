package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface ActionOnColumnHeaderCallback {

    void apply(String actionKey);

}
