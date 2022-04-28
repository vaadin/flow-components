package com.vaadin.component.spreadsheet.client.js.callbacks;

import java.util.Map;

@FunctionalInterface
public interface ColumnResizedCallback {

    void apply(Map<Integer, Integer> newColumnSizes, int row1, int col1,
            int row2, int col2);

}
