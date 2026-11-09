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
package com.vaadin.flow.component.ai.common;

import java.io.Serializable;
import java.util.List;

import com.vaadin.flow.data.provider.QuerySortOrder;

/**
 * The page of a query's result that a lazily loading component asks a
 * {@link com.vaadin.flow.component.ai.provider.DatabaseProvider} for: the rows
 * from {@code offset}, at most {@code limit} of them, in the order of
 * {@code sortOrders}.
 * <p>
 * A sort order names a result column the way the rows returned by the provider
 * name it; a grid sorts by the column the user clicked, whose key is that name.
 * An empty list of sort orders keeps the order the query itself produces.
 * </p>
 *
 * @param offset
 *            the index of the first row to return, {@code 0} or more
 * @param limit
 *            the maximum number of rows to return, {@code 1} or more
 * @param sortOrders
 *            the result columns to order by, applied in list order; an empty
 *            list keeps the query's own order
 * @author Vaadin Ltd
 * @since 25.4
 */
public record QueryPage(int offset, int limit,
        List<QuerySortOrder> sortOrders) implements Serializable {

    /**
     * Creates a page.
     *
     * @param offset
     *            the index of the first row to return, {@code 0} or more
     * @param limit
     *            the maximum number of rows to return, {@code 1} or more
     * @param sortOrders
     *            the result columns to order by, or {@code null} for the
     *            query's own order
     * @throws IllegalArgumentException
     *             if the offset is negative or the limit is not positive
     */
    public QueryPage {
        if (offset < 0) {
            throw new IllegalArgumentException(
                    "offset must not be negative, was " + offset);
        }
        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "limit must be positive, was " + limit);
        }
        sortOrders = sortOrders == null ? List.of() : List.copyOf(sortOrders);
    }

    /**
     * Creates the page holding the first rows of a result, in the query's own
     * order.
     *
     * @param limit
     *            the maximum number of rows, {@code 1} or more
     * @return the page
     */
    public static QueryPage first(int limit) {
        return new QueryPage(0, limit, List.of());
    }
}
