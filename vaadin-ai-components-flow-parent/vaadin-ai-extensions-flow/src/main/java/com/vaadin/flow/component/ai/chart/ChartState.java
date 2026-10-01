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
import com.vaadin.flow.component.charts.util.ChartSerialization;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ObjectNode;

/**
 * Serializable chart state for persistence across sessions. Captured via
 * {@link ChartAIController#getState()} and restored via
 * {@link ChartAIController#restoreState(ChartState)}.
 *
 * @param queries
 *            the SQL queries for the chart's data series
 * @param configuration
 *            the chart configuration
 * @param llmConfiguration
 *            the part of the chart configuration the LLM has set, which is what
 *            it sees of the chart after a restore
 * @author Vaadin Ltd
 * @since 25.3
 */
public record ChartState(List<String> queries, Configuration configuration,
        Configuration llmConfiguration) implements Serializable {
    /**
     * Creates a new state instance.
     *
     * @param queries
     *            the SQL queries, not {@code null}
     * @param configuration
     *            the chart configuration, not {@code null}
     * @param llmConfiguration
     *            the part of the chart configuration the LLM has set, or
     *            {@code null} to use the chart configuration without its series
     *            and x-axis categories, as those may come from the query
     *            results. The LLM gets it as is after a restore, so it must not
     *            hold values from the query results.
     */
    public ChartState {
        queries = List.copyOf(queries);
        Objects.requireNonNull(configuration, "Configuration cannot be null");
        if (llmConfiguration == null) {
            llmConfiguration = withoutQueryResultValues(configuration);
        }
    }

    /**
     * Creates a new state instance without the part of the chart configuration
     * the LLM has set. The LLM then sees the chart configuration without its
     * series and x-axis categories, as those may come from the query results.
     *
     * @param queries
     *            the SQL queries, not {@code null}
     * @param configuration
     *            the chart configuration, not {@code null}
     */
    public ChartState(List<String> queries, Configuration configuration) {
        this(queries, configuration, null);
    }

    private static Configuration withoutQueryResultValues(
            Configuration configuration) {
        var json = JacksonUtils
                .readTree(ChartSerialization.toJSON(configuration));
        json.remove(ConfigurationKeys.SERIES);
        // The parser only reads an x axis given as a single object
        if (json.get(ConfigurationKeys.X_AXIS) instanceof ObjectNode xAxis) {
            xAxis.remove(ConfigurationKeys.CATEGORIES);
        }
        return ChartConfigurationParser.parse(json.toString());
    }
}
