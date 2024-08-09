/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.component.spreadsheet.client.js.callbacks;

import java.util.Map;

@FunctionalInterface
public interface RowsResizedCallback {

    void apply(Map<Integer, Float> newRowSizes, int row1, int col1, int row2,
            int col2);

}
