/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.testbench;

import com.vaadin.testbench.TestBenchElement;

/**
 * A TestBench element representing a <code>&lt;tr&gt;</code> element in a grid.
 */
public class GridTRElement extends TestBenchElement {

    /**
     * Gets the cell for the given column in this row.
     *
     * @param column
     *            the column element
     * @return the cell for the given column
     */
    public GridTHTDElement getCell(GridProColumnElement column) {
        TestBenchElement cell = (TestBenchElement) executeScript("""
                const [row, column] = arguments;
                return Array.from(row.children)
                    .find((cell) => cell._column === column) ?? null;
                """, this, column);
        return cell == null ? null : cell.wrap(GridTHTDElement.class);
    }

}
