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
 * A TestBench element representing a <code>&lt;vaadin-grid-column&gt;</code>
 * element. This is not a TestBenchElement as polyfilled browsers are not
 * capable of finding it or handling it as a web element.
 */
public class GridColumnElement {

    private GridElement grid;
    private Long __generatedId;

    public GridColumnElement(Long __generatedId, GridElement grid) {
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

    /**
     * Gets the footer cell for this column.
     * <p>
     * A column always has a footer cell, even if the footer is not shown.
     *
     * @return the footer cell for the column
     */
    public GridTHTDElement getFooterCell() {
        return ((TestBenchElement) execJs("return column._footerCell"))
                .wrap(GridTHTDElement.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GridColumnElement)) {
            return false;
        }

        return get__generatedId()
                .equals(((GridColumnElement) obj).get__generatedId());
    }

}
