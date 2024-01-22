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
package com.vaadin.flow.component.grid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a group of {@code <vaadin-grid-column>} or
 * {@code <vaadin-grid-column-group>} components that are on the same hierarchy
 * level. It can be used for a header row or a footer row or both.
 * <p>
 * The bottom-most layer contains {@code <vaadin-grid-column>} elements, the
 * second layer their parent {@code <vaadin-grid-column-group>} elements and so
 * on.
 *
 * @author Vaadin Ltd.
 */
class ColumnLayer implements Serializable {

    private Grid<?> grid;
    private List<AbstractColumn<?>> columns;

    private HeaderRow headerRow;
    private FooterRow footerRow;

    /**
     * Creates a ColumnLayer for tracking one hierarchy layer of column
     * components in a Grid.
     *
     * @param grid
     *            the grid that has the columns
     */
    ColumnLayer(Grid<?> grid) {
        this.grid = grid;
        this.columns = new ArrayList<>();
    }

    /**
     * Creates a ColumnLayer for tracking one hierarchy layer of column
     * components in a Grid.
     *
     * @param grid
     *            the grid that has the columns
     * @param columns
     *            the columns belonging to this layer
     */
    ColumnLayer(Grid<?> grid, List<AbstractColumn<?>> columns) {
        this.grid = grid;
        this.columns = columns;
    }

    /**
     * Adds the given column to the end of this layer.
     *
     * @param column
     *            the column to add
     */
    protected void addColumn(AbstractColumn<?> column) {
        addColumn(this.columns.size(), column);
    }

    /**
     * Inserts the given column to the provided index in this layer.
     *
     * @param index
     *            the index where to insert
     * @param column
     *            the column to insert
     */
    protected void addColumn(int index, AbstractColumn<?> column) {
        this.columns.add(index, column);
        if (isHeaderRow()) {
            headerRow.addCell(index, column);
        }
        if (isFooterRow()) {
            footerRow.addCell(index, column);
        }
    }

    /**
     * Updates this layer and corresponding header and footer rows when the
     * given column is removed.
     *
     * @param column
     *            the component that is removed, must be on this layer
     */
    protected void removeColumn(AbstractColumn<?> column) {
        columns.remove(column);
        if (isHeaderRow()) {
            asHeaderRow().removeCell(column);
        }
        if (isFooterRow()) {
            asFooterRow().removeCell(column);
        }
    }

    /**
     * Gets the HeaderRow representation of this ColumnLayer.
     * <p>
     * If this layer has not been used as a HeaderRow before, the HeaderRow will
     * be instantiated by setting the header templates to the components.
     *
     * @return the HeaderRow representation of this layer
     */
    protected HeaderRow asHeaderRow() {
        if (headerRow == null) {
            headerRow = new HeaderRow(this);
            columns.forEach(col -> col.setHeaderText(""));
        }
        return headerRow;
    }

    /**
     * Gets the FooterRow representation of this ColumnLayer.
     * <p>
     * If this layer has not been used as a FooterRow before, the FooterRow will
     * be instantiated by setting the footer templates to the components.
     *
     * @return the FooterRow representation of this layer
     */
    protected FooterRow asFooterRow() {
        if (footerRow == null) {
            footerRow = new FooterRow(this);
            columns.forEach(col -> col.setFooterText(""));
        }
        return footerRow;
    }

    /**
     * Binds the given HeaderRow to the column components on this layer.
     *
     * @param headerRow
     *            the HeaderRow to update to use the columns on this layer
     */
    protected void setHeaderRow(HeaderRow headerRow) {
        this.headerRow = headerRow;
        if (headerRow != null) {
            headerRow.setLayer(this);
        }
    }

    /**
     * Binds the given FooterRow to the column components on this layer.
     *
     * @param footerRow
     *            the FooterRow to update to use the columns on this layer
     */
    protected void setFooterRow(FooterRow footerRow) {
        this.footerRow = footerRow;
        if (footerRow != null) {
            footerRow.setLayer(this);
        }
    }

    /**
     * Gets whether this layer has a HeaderRow representation instantiated
     * (whether the columns on this layer have header templates).
     *
     * @return whether the column components on this layer have headers or not
     */
    protected boolean isHeaderRow() {
        return headerRow != null;
    }

    /**
     * Gets whether this layer has a FooterRow representation instantiated
     * (whether the columns on this layer have footer templates).
     *
     * @return whether the column components on this layer have footers or not
     */
    protected boolean isFooterRow() {
        return footerRow != null;
    }

    /**
     * Gets the Grid that owns this layer of columns.
     *
     * @return the grid that owns this layer
     */
    protected Grid<?> getGrid() {
        return grid;
    }

    /**
     * Bind this layer and the related HeaderRow and FooterRow representations
     * to the given column components.
     *
     * @param columns
     *            the column components to use for this layer
     */
    protected void setColumns(List<AbstractColumn<?>> columns) {
        this.columns = columns;
        if (headerRow != null) {
            headerRow.setColumns(columns);
        }
        if (footerRow != null) {
            footerRow.setColumns(columns);
        }
    }

    /**
     * Gets the column components that belong to this layer.
     *
     * @return the column components of this layer
     */
    protected List<AbstractColumn<?>> getColumns() {
        return columns;
    }

    /**
     * Sets whether components on this layer should display the sorting
     * indicators if the underlying column is sortable.
     *
     * @param sortingIndicators
     *            {@code true} to make components on this layer to have the
     *            sorting indicators if the column is sortable, {@code false} to
     *            not have sorting indicators
     */
    protected void updateSortingIndicators(boolean sortingIndicators) {
        columns.forEach(col -> col.updateSortingIndicators(sortingIndicators));
    }

    /**
     * Updates the internal column order according to given column total
     * ordering.
     *
     * @param columnsPreOrder
     *            the total column ordering, having parent column groups
     *            preceding children (pre-order).
     */
    protected void updateColumnOrder(List<ColumnBase<?>> columnsPreOrder) {
        columns.sort(Comparator.comparingInt(columnsPreOrder::indexOf));
        setColumns(columns);
    }
}
