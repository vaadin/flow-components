/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.component.spreadsheet.client.js.callbacks;

@FunctionalInterface
public interface CellRangeSelectedCallback {

    void apply(int row1, int col1, int row2, int col2);

}
