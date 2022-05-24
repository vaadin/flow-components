package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface LevelHeaderClickedCallback {

    void apply(boolean cols, int level);
}
