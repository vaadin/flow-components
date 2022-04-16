package com.vaadin.flow.component.gridpro.testbench;

/*
 * #%L
 * Vaadin GridPro Testbench API
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * See the file license.html distributed with this software for more
 * information about licensing.
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

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
        TestBenchElement e = (TestBenchElement) executeScript(
                "const grid = arguments[0];" //
                        + "const columnId = arguments[1];" //
                        + "return Array.from(grid.children)."
                        + "filter(function(cell) { return cell._column && cell._column.__generatedTbId == columnId;})[0]",
                this, column.get__generatedId());
        return e == null ? null : e.wrap(GridTHTDElement.class);
    }

}
