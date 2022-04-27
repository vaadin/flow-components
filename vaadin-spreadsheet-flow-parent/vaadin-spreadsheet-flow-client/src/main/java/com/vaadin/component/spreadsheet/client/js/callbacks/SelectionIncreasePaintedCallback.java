package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface SelectionIncreasePaintedCallback {

    void apply(int r1, int c1, int r2, int c2);

}
