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
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.vaadin.flow.component.charts.Chart;

/**
 * A registry that provides chart tools with access to charts and their
 * associated state (data sources, pending configuration). Tools created by
 * {@link ChartTools} operate on this registry, allowing them to be reused
 * across different controllers (e.g., ChartAIController,
 * DashboardAIController).
 * <p>
 * The registry does not hold direct references to {@link Chart} instances.
 * Instead, it uses a resolver function to look up charts by ID at execution
 * time. This means chart lifecycle changes (e.g., a chart being removed from a
 * dashboard) are automatically reflected — there are no stale references to
 * manage.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class ChartRegistry implements Serializable {

    private final Map<String, ChartEntry> entries = new LinkedHashMap<>();
    private final Function<String, Chart> chartResolver;
    private final Supplier<Set<String>> chartIdsSupplier;
    private Consumer<String> queryValidator;

    /**
     * Creates a new chart registry.
     *
     * @param chartResolver
     *            a function that resolves a chart ID to a {@link Chart}
     *            instance, returning {@code null} if the chart no longer exists;
     *            not {@code null}
     * @param chartIdsSupplier
     *            a supplier that returns the set of currently available chart
     *            IDs; not {@code null}
     */
    public ChartRegistry(Function<String, Chart> chartResolver,
            Supplier<Set<String>> chartIdsSupplier) {
        this.chartResolver = Objects.requireNonNull(chartResolver,
                "chartResolver must not be null");
        this.chartIdsSupplier = Objects.requireNonNull(chartIdsSupplier,
                "chartIdsSupplier must not be null");
    }

    /**
     * Sets a query validator that tests SQL queries before they are accepted
     * by tools. If the query is invalid, the validator should throw an
     * exception.
     *
     * @param queryValidator
     *            a consumer that validates a SQL query, or {@code null} to
     *            disable validation
     */
    public void setQueryValidator(Consumer<String> queryValidator) {
        this.queryValidator = queryValidator;
    }

    /**
     * Validates a SQL query using the configured validator, if any. Does
     * nothing if no validator is set.
     *
     * @param query
     *            the SQL query to validate
     * @throws RuntimeException
     *             if the query is invalid
     */
    public void validateQuery(String query) {
        if (queryValidator != null) {
            queryValidator.accept(query);
        }
    }

    /**
     * Resolves a chart by ID using the resolver function.
     *
     * @param chartId
     *            the chart ID, not {@code null}
     * @return the chart instance, never {@code null}
     * @throws IllegalArgumentException
     *             if no chart exists for the given ID
     */
    public Chart getChart(String chartId) {
        Objects.requireNonNull(chartId, "chartId must not be null");
        Chart chart = chartResolver.apply(chartId);
        if (chart == null) {
            throw new IllegalArgumentException(
                    "No chart found with ID '" + chartId + "'");
        }
        return chart;
    }

    /**
     * Gets or creates the {@link ChartEntry} for the given chart ID. The entry
     * holds data source bindings and pending state for the chart.
     *
     * @param chartId
     *            the chart ID, not {@code null}
     * @return the chart entry, never {@code null}
     * @throws IllegalArgumentException
     *             if no chart exists for the given ID
     */
    public ChartEntry getEntry(String chartId) {
        Objects.requireNonNull(chartId, "chartId must not be null");
        if (chartResolver.apply(chartId) == null) {
            throw new IllegalArgumentException(
                    "No chart found with ID '" + chartId + "'");
        }
        return entries.computeIfAbsent(chartId, k -> new ChartEntry());
    }

    /**
     * Returns the set of currently available chart IDs.
     *
     * @return the chart IDs, never {@code null}
     */
    public Set<String> getChartIds() {
        return chartIdsSupplier.get();
    }

    /**
     * Returns all chart entries that have been accessed (and thus may have
     * state). Entries for charts that no longer exist are included — callers
     * should check {@link #getChart(String)} and handle missing charts
     * gracefully.
     *
     * @return an unmodifiable map of chart ID to entry, never {@code null}
     */
    public Map<String, ChartEntry> getEntries() {
        return Map.copyOf(entries);
    }
}
