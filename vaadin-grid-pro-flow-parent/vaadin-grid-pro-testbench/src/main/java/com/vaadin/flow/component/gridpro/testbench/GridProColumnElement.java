/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.testbench;

import java.util.ArrayList;

import com.vaadin.testbench.TestBenchElement;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-pro-grid-edit-column&gt;</code> element.
 */
public class GridProColumnElement extends TestBenchElement {
    /**
     * Gets the options List for this column.
     *
     * @return the options list
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String> getOptionsList() {
        return (ArrayList<String>) getProperty("editorOptions");
    }

    /**
     * Gets the header cell for this column.
     * <p>
     * A column always has a header cell, even if the header is not shown.
     *
     * @return the header cell for the column
     */
    public GridTHTDElement getHeaderCell() {
        return getPropertyElement("_headerCell").wrap(GridTHTDElement.class);
    }
}
