/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrderBuilder;

/**
 * Helper classes with fluent API for constructing {@link GridSortOrder} lists.
 * When the sort order is ready to be passed on, calling {@link #build()} will
 * create the list of sort orders.
 *
 * @see GridSortOrder
 * @see GridSortOrderBuilder#thenAsc(com.vaadin.flow.component.grid.Grid.Column)
 * @see GridSortOrderBuilder#thenDesc(com.vaadin.flow.component.grid.Grid.Column)
 * @see #build()
 *
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the type of the grid
 */
public class GridSortOrderBuilder<T>
        extends SortOrderBuilder<GridSortOrder<T>, Column<T>> {

    @Override
    public GridSortOrderBuilder<T> thenAsc(Column<T> by) {
        return (GridSortOrderBuilder<T>) super.thenAsc(by);
    }

    @Override
    public GridSortOrderBuilder<T> thenDesc(Column<T> by) {
        return (GridSortOrderBuilder<T>) super.thenDesc(by);
    }

    @Override
    protected GridSortOrder<T> createSortOrder(Column<T> by,
            SortDirection direction) {
        return new GridSortOrder<>(by, direction);
    }
}
