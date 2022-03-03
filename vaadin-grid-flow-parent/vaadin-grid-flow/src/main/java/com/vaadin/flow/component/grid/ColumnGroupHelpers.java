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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.dom.Element;

/**
 * Helper methods for wrapping {@code <vaadin-grid-column>} elements inside
 * {@code <vaadin-grid-column-group>} elements.
 *
 * @author Vaadin Ltd
 *
 */
class ColumnGroupHelpers {

    /**
     * Wraps each of the given columns inside a column group and places those
     * wrapper groups in the original columns' places.
     *
     * @param cols
     *            the columns to wrap
     * @param grid
     *            the grid that has the columns
     * @return the new column groups that wrap the given columns
     */
    public static List<AbstractColumn<?>> wrapInSeparateColumnGroups(
            Collection<AbstractColumn<?>> cols, Grid<?> grid) {
        return cols.stream().map(col -> wrapSingleColumn(col, grid))
                .collect(Collectors.toList());
    }

    /**
     * Wraps the given columns inside a column group and places this wrapper on
     * the first wrapped column's place.
     *
     * @param grid
     *            the grid that has the columns
     * @param columns
     *            the columns to wrap
     * @return the new column group that wraps the given columns
     */
    public static ColumnGroup wrapInColumnGroup(Grid<?> grid,
            AbstractColumn<?>... columns) {
        ColumnGroup group = wrapSingleColumn(columns[0], grid);
        for (int i = 1; i < columns.length; i++) {
            group.getElement().appendChild(columns[i].getElement());
        }
        return group;
    }

    /**
     * Wraps the given columns inside a column group and places this wrapper on
     * the first wrapped column's place.
     *
     * @param grid
     *            the grid that has the columns
     * @param columns
     *            the columns to wrap
     * @return the new column group that wraps the given columns
     */
    public static ColumnGroup wrapInColumnGroup(Grid<?> grid,
            List<AbstractColumn<?>> columns) {
        return wrapInColumnGroup(grid,
                columns.toArray(new AbstractColumn<?>[columns.size()]));
    }

    /**
     * Wraps the given column inside a column group and places this wrapper
     * group to the original column's place.
     *
     * @param column
     *            the column to wrap
     * @param grid
     *            the grid that has the column
     * @return the new column group that wraps the column
     */
    private static ColumnGroup wrapSingleColumn(AbstractColumn<?> column,
            Grid<?> grid) {

        Element parent = column.getElement().getParent();
        int index = parent.indexOfChild(column.getElement());

        column.getElement().removeFromParent();

        ColumnGroup group = new ColumnGroup(grid, column);
        parent.insertChild(index, group.getElement());

        return group;
    }

}
