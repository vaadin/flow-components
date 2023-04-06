/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;

/**
 * Sorting information for {@link Grid}.
 *
 * @param <T>
 *            the grid type
 */
public class GridSortOrder<T> extends SortOrder<Column<T>> {

    /**
     * Construct sorting information for usage in a {@link Grid}.
     *
     * @param column
     *            the column to be sorted
     * @param direction
     *            sorting direction
     */
    public GridSortOrder(Column<T> column, SortDirection direction) {
        super(column, direction);
    }

    /**
     * Gets the column this sorting information is attached to.
     *
     * @return the column being sorted
     */
    @Override
    public Column<T> getSorted() {
        return super.getSorted();
    }

    /**
     * Creates a new grid sort builder with given sorting using ascending sort
     * direction.
     *
     * @param by
     *            the column to sort by
     * @param <T>
     *            the grid type
     * @return the grid sort builder
     */
    public static <T> GridSortOrderBuilder<T> asc(Column<T> by) {
        return new GridSortOrderBuilder<T>().thenAsc(by);
    }

    /**
     * Creates a new grid sort builder with given sorting using descending sort
     * direction.
     *
     * @param by
     *            the column to sort by
     * @param <T>
     *            the grid type
     * @return the grid sort builder
     */
    public static <T> GridSortOrderBuilder<T> desc(Column<T> by) {
        return new GridSortOrderBuilder<T>().thenDesc(by);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GridSortOrder)) {
            return false;
        }
        return this.getSorted() == ((GridSortOrder) obj).getSorted()
                && this.getDirection() == ((GridSortOrder) obj).getDirection();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getSorted()).append(getDirection())
                .toHashCode();
    }
}
