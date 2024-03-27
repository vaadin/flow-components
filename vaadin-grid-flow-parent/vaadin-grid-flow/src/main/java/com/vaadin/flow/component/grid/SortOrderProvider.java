/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.grid;

import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Generates the sort orders when rows are sorted by a column.
 *
 * @see Column#setSortOrderProvider
 *
 * @author Vaadin Ltd
 */
@FunctionalInterface
public interface SortOrderProvider
        extends SerializableFunction<SortDirection, Stream<QuerySortOrder>> {

    /**
     * Generates the sort orders when rows are sorted by a column.
     *
     * @param sortDirection
     *            desired sort direction
     *
     * @return sort information
     */
    @Override
    Stream<QuerySortOrder> apply(SortDirection sortDirection);
}
