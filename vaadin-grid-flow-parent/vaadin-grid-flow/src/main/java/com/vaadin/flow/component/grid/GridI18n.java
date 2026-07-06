/*
 * Copyright 2000-2026 Vaadin Ltd.
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
package com.vaadin.flow.component.grid;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The internationalization properties for {@link Grid}. This can be used to
 * customize and translate the accessible names used by the grid.
 *
 * @see Grid#setI18n(GridI18n)
 *
 * @author Vaadin Ltd.
 * @since 25.3
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GridI18n implements Serializable {
    private String selectAll;
    private String selectRow;
    private String sorter;

    /**
     * Gets the accessible name (aria-label) for the select all checkbox in the
     * selection column header cell.
     *
     * @return the accessible name for the select all checkbox
     * @since 25.3
     */
    public String getSelectAll() {
        return selectAll;
    }

    /**
     * Sets the accessible name (aria-label) for the select all checkbox in the
     * selection column header cell.
     *
     * @param selectAll
     *            the accessible name for the select all checkbox
     * @return this instance for method chaining
     * @since 25.3
     */
    public GridI18n setSelectAll(String selectAll) {
        this.selectAll = selectAll;
        return this;
    }

    /**
     * Gets the accessible name (aria-label) template for the select row
     * checkbox in each selection column body cell.
     *
     * @return the accessible name template for the select row checkbox
     * @since 25.3
     */
    public String getSelectRow() {
        return selectRow;
    }

    /**
     * Sets the accessible name (aria-label) template for the select row
     * checkbox in each selection column body cell. The {@code {rowHeader}}
     * placeholder is replaced with the text content of the row's row-header
     * cell (see {@link Grid.Column#setRowHeader(boolean)}), or the 1-based row
     * index when there is no row-header column.
     *
     * @param selectRow
     *            the accessible name template for the select row checkbox
     * @return this instance for method chaining
     * @since 25.3
     */
    public GridI18n setSelectRow(String selectRow) {
        this.selectRow = selectRow;
        return this;
    }

    /**
     * Gets the accessible name (aria-label) template for the grid sorters.
     *
     * @return the accessible name template for the sorters
     * @since 25.3
     */
    public String getSorter() {
        return sorter;
    }

    /**
     * Sets the accessible name (aria-label) template for the grid sorters. The
     * {@code {column}} placeholder is replaced with the column header text.
     *
     * @param sorter
     *            the accessible name template for the sorters
     * @return this instance for method chaining
     * @since 25.3
     */
    public GridI18n setSorter(String sorter) {
        this.sorter = sorter;
        return this;
    }
}
