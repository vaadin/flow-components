package com.vaadin.component.spreadsheet.client.js.callbacks;

import java.util.Map;

@FunctionalInterface
public interface RowsResizedCallback {

    void apply(Map<Integer, Float> newRowSizes, int row1, int col1, int row2,
            int col2);

}
