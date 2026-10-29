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
package com.vaadin.flow.component.ai.provider;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A source of rows that a {@link FederatedDatabaseProvider} presents to the LLM
 * as one SQL table: a REST collection, a CSV file, a document collection, or
 * anything else that can list its items as rows. The provider's SQL engine
 * joins, filters, sorts and aggregates over the rows a source returns, so a
 * source only needs to know how to list them.
 * <p>
 * A source that can do more on its own declares that with
 * {@link #getCapabilities()}, and the engine pushes those parts of a query down
 * to {@link #fetch(Pushdown)} and {@link #count(Pushdown)} so that fewer rows
 * travel:
 * </p>
 * <ul>
 * <li>Equality filters are pushed into every read of the source. The engine
 * applies them again to the rows it receives, so a source may filter less than
 * it was asked to without changing the result, unless the read also has a
 * limit.</li>
 * <li>A limit, with an offset and a sort, is pushed only when the source takes
 * the whole query below it: every filter, and the order when the query sorts.
 * This is how a grid's pages reach the source. Joins, aggregates and
 * expressions the source cannot evaluate keep the read in the engine.</li>
 * <li>A count of all rows is pushed under the same condition, which is how a
 * grid learns its size.</li>
 * </ul>
 *
 * @author Vaadin Ltd
 * @since 25.4
 */
public interface TableSource extends Serializable {

    /**
     * A column of a source. The type is one of {@link String}, {@link Integer},
     * {@link Long}, {@link Double}, {@link java.math.BigDecimal},
     * {@link Boolean}, {@link java.time.LocalDate},
     * {@link java.time.LocalDateTime} or {@link java.time.LocalTime}; the
     * values a source returns for the column are of that type, or any
     * {@link Number} for a numeric type. Every column is nullable.
     *
     * @param name
     *            the column name, as the LLM refers to it in SQL
     * @param type
     *            the Java type of the values
     * @since 25.4
     */
    record Column(String name, Class<?> type) implements Serializable {

        /**
         * Creates a column.
         *
         * @param name
         *            the column name, not {@code null}
         * @param type
         *            the Java type of the values, not {@code null}
         */
        public Column {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(type, "type must not be null");
        }
    }

    /**
     * What a source can do on its own, so that the engine pushes that part of a
     * query down to it.
     *
     * @since 25.4
     */
    enum Capability {
        /**
         * The source can return only the rows whose columns equal given values,
         * see {@link Pushdown#equalityFilters()} and
         * {@link TableSource#supportsEqualityFilter(String)}.
         */
        EQUALITY_FILTER,
        /**
         * The source can return its rows in a given order, see
         * {@link Pushdown#sort()} and {@link #supportsSort(List)}. A sort is
         * pushed only together with a limit, since that is when it saves
         * reading rows.
         */
        SORT,
        /**
         * The source can skip a number of rows and return at most a number of
         * rows, see {@link Pushdown#offset()} and {@link Pushdown#limit()}. The
         * rows are skipped and limited after the pushed filters and the pushed
         * sort, so a source with this capability applies every pushed filter
         * exactly. Without a sort, the rows come in the source's own order,
         * which has to be the same on every read so that pages do not overlap.
         */
        LIMIT,
        /**
         * The source can count the rows that match the pushed filters, see
         * {@link TableSource#count(Pushdown)}.
         */
        COUNT
    }

    /**
     * One key of the order in which a source returns its rows. Null values sort
     * after every other value when ascending and before every other value when
     * descending, as they do in the provider's SQL.
     *
     * @param column
     *            the name of the column to sort by
     * @param descending
     *            {@code true} for descending order, {@code false} for ascending
     * @since 25.4
     */
    record SortKey(String column, boolean descending) implements Serializable {

        /**
         * Creates a sort key.
         *
         * @param column
         *            the column name, not {@code null}
         * @param descending
         *            {@code true} for descending order
         */
        public SortKey {
            Objects.requireNonNull(column, "column must not be null");
        }
    }

    /**
     * The parts of a query the engine pushes down to a source. A source
     * receives only the parts its {@link Capability capabilities} declare;
     * every other part is empty.
     *
     * @param equalityFilters
     *            column name to the value the column must equal, of the
     *            column's type; empty when nothing is pushed
     * @param sort
     *            the order of the rows, first key first; empty for the source's
     *            own order
     * @param offset
     *            the number of rows to skip after filtering and sorting;
     *            {@code 0} when nothing is pushed
     * @param limit
     *            the largest number of rows to return after the offset, or
     *            {@code null} for all of them
     * @since 25.4
     */
    record Pushdown(Map<String, Object> equalityFilters, List<SortKey> sort,
            long offset, Long limit) implements Serializable {

        /**
         * A pushdown of nothing: every row of the source.
         */
        public static final Pushdown NONE = new Pushdown(Map.of());

        /**
         * Creates a pushdown.
         *
         * @param equalityFilters
         *            column name to value, not {@code null}
         * @param sort
         *            the sort keys, not {@code null}
         * @param offset
         *            the rows to skip, not negative
         * @param limit
         *            the largest number of rows, not negative, or {@code null}
         *            for no limit
         */
        public Pushdown {
            Objects.requireNonNull(equalityFilters,
                    "equalityFilters must not be null");
            Objects.requireNonNull(sort, "sort must not be null");
            if (offset < 0) {
                throw new IllegalArgumentException(
                        "offset must not be negative");
            }
            if (limit != null && limit < 0) {
                throw new IllegalArgumentException(
                        "limit must not be negative");
            }
            equalityFilters = Map.copyOf(equalityFilters);
            sort = List.copyOf(sort);
        }

        /**
         * Creates a pushdown of equality filters only.
         *
         * @param equalityFilters
         *            column name to value, not {@code null}
         */
        public Pushdown(Map<String, Object> equalityFilters) {
            this(equalityFilters, List.of(), 0, null);
        }
    }

    /**
     * Returns the table name the LLM uses for this source in SQL. Names are
     * matched case-insensitively.
     *
     * @return the table name, never {@code null}
     */
    String getName();

    /**
     * Returns the columns of the source, in the order the LLM sees them.
     *
     * @return the columns, at least one
     */
    List<Column> getColumns();

    /**
     * Returns what the source can do on its own. The default is nothing: the
     * source returns every row and the engine does the rest.
     *
     * @return the capabilities, never {@code null}
     */
    default Set<Capability> getCapabilities() {
        return Set.of();
    }

    /**
     * Returns whether the source can filter by the value of the given column,
     * for a source with the {@link Capability#EQUALITY_FILTER} capability.
     * Override it when the source filters by some columns only, for example by
     * the arguments of an API; the engine then filters by the other columns in
     * memory, and keeps a limit in memory too when a query filters by them. The
     * default is {@code true}.
     *
     * @param column
     *            the column name
     * @return {@code true} if {@link #fetch(Pushdown)} and
     *         {@link #count(Pushdown)} can filter by the column
     */
    default boolean supportsEqualityFilter(String column) {
        return true;
    }

    /**
     * Returns whether the source can return its rows in the given order, for a
     * source with the {@link Capability#SORT} capability. Override it when the
     * source sorts by some orders only, for example by one column at a time;
     * the engine then sorts in memory instead. The default is {@code true}.
     *
     * @param sort
     *            the sort keys, first key first, never empty
     * @return {@code true} if {@link #fetch(Pushdown)} can return the rows in
     *         that order
     */
    default boolean supportsSort(List<SortKey> sort) {
        return true;
    }

    /**
     * Returns a short description of where the rows come from, shown to the LLM
     * next to the table name, for example {@code "REST collection
     * /api/sales"}. The default is {@code null}, no description.
     *
     * @return the description, or {@code null}
     */
    default String getDescription() {
        return null;
    }

    /**
     * Returns the rows of the source, every column of each. Each row is a map
     * from column name to value; a missing column is {@code null}.
     * <p>
     * When a request fails for a reason the LLM can act on, for example a
     * filter on a field the backend rejects, throw a {@link ToolException}
     * whose message is safe to show; any other exception is reported to the LLM
     * as a generic error.
     * </p>
     *
     * @param pushdown
     *            the parts of the query the source may apply, never
     *            {@code null}
     * @return the rows, never {@code null} but may be empty
     * @throws ToolException
     *             if the request fails and the LLM should learn why
     */
    List<Map<String, Object>> fetch(Pushdown pushdown);

    /**
     * Returns the number of rows that match the pushed equality filters, for a
     * source with the {@link Capability#COUNT} capability. The engine calls it
     * instead of {@link #fetch(Pushdown)} for a count of all the rows of a
     * query the source takes entirely. The default throws
     * {@link UnsupportedOperationException}.
     * <p>
     * Failures are reported as for {@link #fetch(Pushdown)}.
     * </p>
     *
     * @param pushdown
     *            the equality filters to count with; its sort, offset and limit
     *            are empty
     * @return the number of matching rows
     * @throws ToolException
     *             if the request fails and the LLM should learn why
     */
    default long count(Pushdown pushdown) {
        throw new UnsupportedOperationException(
                getName() + " cannot count its rows");
    }
}
