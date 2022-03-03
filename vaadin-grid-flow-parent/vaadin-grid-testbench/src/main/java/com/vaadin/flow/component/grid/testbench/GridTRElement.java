/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.testbench;

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
    public GridTHTDElement getCell(GridColumnElement column) {
        TestBenchElement e = (TestBenchElement) executeScript(
                "const grid = arguments[0];" //
                        + "const columnId = arguments[1];" //
                        + "return Array.from(grid.children)."
                        + "filter(function(cell) { return cell._column && cell._column.__generatedTbId == columnId;})[0]",
                this, column.get__generatedId());
        return e == null ? null : e.wrap(GridTHTDElement.class);
    }

    /**
     * Gets the row details for this row.
     *
     * @return the details cell
     */
    public GridTHTDElement getDetails() {
        TestBenchElement e = (TestBenchElement) executeScript(
                "const grid = arguments[0];" //
                        + "return Array.from(grid.children)."
                        + "filter(function(cell) { return cell.getAttribute('part') && cell.getAttribute('part').includes('cell details-cell');})[0]",
                this);
        return e == null ? null : e.wrap(GridTHTDElement.class);
    }

    /**
     * Checks if the row is selected
     *
     * @return <code>true</code> if the row is selected, <code>false</code>
     *         otherwise
     */
    @Override
    public boolean isSelected() {
        return hasAttribute("selected");
    }

    /**
     * Selects the row if it is not already selected.
     */
    public void select() {
        getGrid().select(this);
    }

    /**
     * Deselects the row if it is selected.
     */
    public void deselect() {
        getGrid().deselect(this);
    }

    /**
     * Gets the grid containing this element.
     *
     * @return the grid for this element
     */
    public GridElement getGrid() {
        return ((TestBenchElement) executeScript(
                "return arguments[0].getRootNode().host", this))
                        .wrap(GridElement.class);
    }

    /**
     * Gets the details container for this row.
     *
     * @return the element containing the details, if any
     */
    public GridTHTDElement getDetailsRow() {
        return ((TestBenchElement) executeScript("const tr = arguments[0];"
                + "const grid = tr.getRootNode().host;" //
                + "return grid._detailsCells" //
                + ".filter(function(cell) {return cell.parentElement.rowIndex && cell.parentElement.rowIndex == tr.rowIndex;})[0]"
                + ";", this)).wrap(GridTHTDElement.class);
    }
}
