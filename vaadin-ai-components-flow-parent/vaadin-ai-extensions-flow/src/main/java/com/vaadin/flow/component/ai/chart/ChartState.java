/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.chart;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.charts.model.Configuration;

/**
 * Serializable chart state for persistence across sessions. Captured via
 * {@link ChartAIController#getState()} and restored via
 * {@link ChartAIController#restoreState(ChartState)}.
 *
 * @param queries
 *            the SQL queries for the chart's data series
 * @param configuration
 *            the chart configuration
 * @author Vaadin Ltd
 * @since 25.2
 */
public record ChartState(List<String> queries,
        Configuration configuration) implements Serializable {
    /**
     * Creates a new state instance.
     *
     * @param queries
     *            the SQL queries, not {@code null}
     * @param configuration
     *            the chart configuration, not {@code null}
     */
    public ChartState {
        queries = List.copyOf(queries);
        Objects.requireNonNull(configuration, "Configuration cannot be null");
    }
}
