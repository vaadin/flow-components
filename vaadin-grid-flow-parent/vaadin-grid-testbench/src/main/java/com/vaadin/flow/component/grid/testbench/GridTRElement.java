/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.grid.testbench;

import com.vaadin.testbench.TestBenchElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        List<GridTHTDElement> cells = getCells(column);
        return cells.size() == 1 ? cells.get(0) : null;
    }

    /**
     * Gets the cells for the given columns in this row.
     *
     * @param columns
     *            the column elements
     * @return a {@link GridTHTDElement} list with the cells for the given
     *         columns
     */
    public List<GridTHTDElement> getCells(GridColumnElement... columns) {
        Object cells = executeScript("const row = arguments[0];" //
                + "const columnIds = arguments[1];"
                + "return Array.from(row.children)."
                + "filter(function(cell) { return cell._column && columnIds.includes(cell._column.__generatedTbId);})",
                this, Arrays.stream(columns)
                        .map(GridColumnElement::get__generatedId).toArray());
        if (cells != null) {
            return ((ArrayList<?>) cells).stream()
                    .map(elem -> ((TestBenchElement) elem)
                            .wrap(GridTHTDElement.class))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
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
