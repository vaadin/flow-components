package com.vaadin.spreadsheet.flowport.gwtexporter.client.callbacks;

@FunctionalInterface
public interface SelectionIncreasePaintedCallback {

    void apply(int r1, int c1, int r2, int c2);

}
