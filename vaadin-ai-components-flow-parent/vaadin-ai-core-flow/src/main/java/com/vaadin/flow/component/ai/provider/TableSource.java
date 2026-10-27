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
 * A source that can filter or project on its own declares that with
 * {@link #getCapabilities()}, and the engine pushes those parts of a query down
 * to {@link #fetch(Pushdown)} so that fewer rows travel. The engine applies the
 * same filters again to the rows it receives, so a source may push down less
 * than it was asked for without changing the result.
 * </p>
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
         * see {@link Pushdown#equalityFilters()}.
         */
        EQUALITY_FILTER
    }

    /**
     * The parts of a query the engine pushes down to a source. A source without
     * the matching {@link Capability} receives nothing; the engine applies the
     * filters again either way.
     *
     * @param equalityFilters
     *            column name to the value the column must equal, of the
     *            column's type; empty when nothing is pushed
     * @since 25.4
     */
    record Pushdown(
            Map<String, Object> equalityFilters) implements Serializable {

        /**
         * A pushdown of nothing: every row of the source.
         */
        public static final Pushdown NONE = new Pushdown(Map.of());

        /**
         * Creates a pushdown.
         *
         * @param equalityFilters
         *            column name to value, not {@code null}
         */
        public Pushdown {
            Objects.requireNonNull(equalityFilters,
                    "equalityFilters must not be null");
            equalityFilters = Map.copyOf(equalityFilters);
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
}
