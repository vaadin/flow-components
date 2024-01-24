/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.gridpro.testbench;

import com.vaadin.testbench.TestBenchElement;

import java.util.ArrayList;

/**
 * A TestBench element representing a
 * <code>&lt;vaadin-pro-grid-edit-column&gt;</code> element. This is not a
 * TestBenchElement as polyfilled browsers are not capable of finding it or
 * handling it as a web element.
 */
public class GridProColumnElement {

    private GridProElement grid;
    private Long __generatedId;

    public GridProColumnElement(Long __generatedId, GridProElement grid) {
        this.grid = grid;
        this.__generatedId = __generatedId;
    }

    /**
     * For internal use only.
     *
     * @return the generated id for the column
     */
    protected Long get__generatedId() {
        return __generatedId;
    }

    /**
     * Gets the options List for this column.
     *
     * @return the options list
     */
    public ArrayList<String> getOptionsList() {
        ArrayList<String> editorOptions = (ArrayList<String>) execJs(
                "return column.editorOptions");
        return editorOptions;
    }

    /**
     * Gets the header cell for this column.
     * <p>
     * A column always has a header cell, even if the header is not shown.
     *
     * @return the header cell for the column
     */
    public GridTHTDElement getHeaderCell() {
        return ((TestBenchElement) execJs("return column._headerCell"))
                .wrap(GridTHTDElement.class);
    }

    private Object execJs(String js) {
        return grid.getCommandExecutor()
                .executeScript("var grid = arguments[0];" //
                        + "var generatedId = arguments[1];"
                        + "var column = grid._getColumns().filter(function(column) {return column.__generatedTbId == generatedId;})[0];"
                        + js, grid, __generatedId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GridProColumnElement)) {
            return false;
        }

        return get__generatedId()
                .equals(((GridProColumnElement) obj).get__generatedId());
    }

}
