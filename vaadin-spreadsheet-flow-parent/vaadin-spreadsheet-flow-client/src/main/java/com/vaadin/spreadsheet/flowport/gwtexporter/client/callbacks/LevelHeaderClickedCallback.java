package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface LevelHeaderClickedCallback {

    void apply(boolean cols, int level);
}
