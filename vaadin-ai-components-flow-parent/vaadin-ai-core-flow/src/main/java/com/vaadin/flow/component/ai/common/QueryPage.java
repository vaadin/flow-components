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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

/**
 * The page of a query's result that a lazily loading component asks a
 * {@link com.vaadin.flow.component.ai.provider.QueryableProvider} for: the rows
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

    /**
     * Selects this page from a complete result, for a provider whose backend
     * cannot sort and page a query itself. The rows are sorted by the sort
     * orders, then the rows from {@code offset} up to {@code limit} of them are
     * returned. The given list is not modified.
     * <p>
     * Values are compared per sort order: nulls come last in either direction,
     * numbers are compared as numbers whatever their class, values of one
     * {@link Comparable} class are compared naturally, and anything else is
     * compared as text.
     * </p>
     *
     * @param rows
     *            the complete result, not {@code null}
     * @return the rows of this page, never {@code null}
     */
    public List<Map<String, Object>> apply(List<Map<String, Object>> rows) {
        var sorted = new ArrayList<>(rows);
        if (!sortOrders.isEmpty()) {
            sorted.sort(comparator());
        }
        int from = Math.min(offset, sorted.size());
        int to = Math.min(from + limit, sorted.size());
        return List.copyOf(sorted.subList(from, to));
    }

    private Comparator<Map<String, Object>> comparator() {
        Comparator<Map<String, Object>> comparator = null;
        for (var sortOrder : sortOrders) {
            var column = sortOrder.getSorted();
            var descending = sortOrder
                    .getDirection() == SortDirection.DESCENDING;
            Comparator<Map<String, Object>> next = (a, b) -> compareValues(
                    a.get(column), b.get(column), descending);
            comparator = comparator == null ? next
                    : comparator.thenComparing(next);
        }
        return comparator;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static int compareValues(Object a, Object b, boolean descending) {
        if (a == null || b == null) {
            return a == null ? (b == null ? 0 : 1) : -1;
        }
        int result;
        if (a instanceof Number x && b instanceof Number y) {
            result = toBigDecimal(x).compareTo(toBigDecimal(y));
        } else if (a instanceof Comparable comparable
                && a.getClass().equals(b.getClass())) {
            result = comparable.compareTo(b);
        } else {
            result = String.valueOf(a).compareTo(String.valueOf(b));
        }
        return descending ? -result : result;
    }

    private static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigDecimal decimal) {
            return decimal;
        }
        if (number instanceof BigInteger integer) {
            return new BigDecimal(integer);
        }
        if (number instanceof Double || number instanceof Float) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return BigDecimal.valueOf(number.longValue());
    }
}
