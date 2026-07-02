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
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GridI18n implements Serializable {
    private String selectAllCheckboxAriaLabel;
    private String selectRowCheckboxAriaLabel;

    /**
     * Gets the accessible name (aria-label) for the select all checkbox in the
     * selection column header cell.
     *
     * @return the accessible name for the select all checkbox
     */
    public String getSelectAllCheckboxAriaLabel() {
        return selectAllCheckboxAriaLabel;
    }

    /**
     * Sets the accessible name (aria-label) for the select all checkbox in the
     * selection column header cell.
     *
     * @param selectAllCheckboxAriaLabel
     *            the accessible name for the select all checkbox
     * @return this instance for method chaining
     */
    public GridI18n setSelectAllCheckboxAriaLabel(
            String selectAllCheckboxAriaLabel) {
        this.selectAllCheckboxAriaLabel = selectAllCheckboxAriaLabel;
        return this;
    }

    /**
     * Gets the accessible name (aria-label) template for the select row
     * checkbox in each selection column body cell.
     *
     * @return the accessible name template for the select row checkbox
     */
    public String getSelectRowCheckboxAriaLabel() {
        return selectRowCheckboxAriaLabel;
    }

    /**
     * Sets the accessible name (aria-label) template for the select row
     * checkbox in each selection column body cell. The {@code {0}} placeholder
     * is replaced with the text content of the row's row-header cell (see
     * {@link Grid.Column#setRowHeader(boolean)}), or the 1-based row index when
     * there is no row-header column.
     *
     * @param selectRowCheckboxAriaLabel
     *            the accessible name template for the select row checkbox
     * @return this instance for method chaining
     */
    public GridI18n setSelectRowCheckboxAriaLabel(
            String selectRowCheckboxAriaLabel) {
        this.selectRowCheckboxAriaLabel = selectRowCheckboxAriaLabel;
        return this;
    }
}
