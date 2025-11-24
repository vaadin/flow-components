/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the state of a data visualization (chart, grid, KPI, etc.).
 * <p>
 * This immutable state object captures all information needed to restore a
 * visualization:
 * </p>
 * <ul>
 * <li>Visualization type (CHART, GRID, KPI)</li>
 * <li>SQL query used to fetch data</li>
 * <li>Type-specific configuration (chart type, grid columns, KPI format,
 * etc.)</li>
 * </ul>
 * <p>
 * The state can be captured via
 * {@link AiDataVisualizationOrchestrator#captureState()} and restored via
 * {@link AiDataVisualizationOrchestrator#restoreState(VisualizationState)}.
 * </p>
 *
 * @author Vaadin Ltd
 */
public final class VisualizationState implements Serializable {

    private final VisualizationType type;
    private final String sqlQuery;
    private final Map<String, Object> configuration;

    private VisualizationState(VisualizationType type, String sqlQuery,
            Map<String, Object> configuration) {
        this.type = type;
        this.sqlQuery = sqlQuery;
        this.configuration = configuration != null
                ? new HashMap<>(configuration)
                : new HashMap<>();
    }

    /**
     * Creates a new visualization state.
     *
     * @param type
     *            the visualization type
     * @param sqlQuery
     *            the SQL query
     * @param configuration
     *            the type-specific configuration
     * @return a new immutable state object
     */
    public static VisualizationState of(VisualizationType type,
            String sqlQuery, Map<String, Object> configuration) {
        return new VisualizationState(type, sqlQuery, configuration);
    }

    /**
     * Creates a new visualization state for a chart.
     *
     * @param sqlQuery
     *            the SQL query
     * @param chartConfig
     *            the chart configuration JSON
     * @return a new immutable state object
     */
    public static VisualizationState ofChart(String sqlQuery,
            String chartConfig) {
        Map<String, Object> config = new HashMap<>();
        if (chartConfig != null && !chartConfig.isEmpty()) {
            config.put("chartConfig", chartConfig);
        }
        return new VisualizationState(VisualizationType.CHART, sqlQuery,
                config);
    }

    /**
     * Gets the visualization type.
     *
     * @return the type
     */
    public VisualizationType getType() {
        return type;
    }

    /**
     * Gets the SQL query.
     *
     * @return the SQL query, may be null
     */
    public String getSqlQuery() {
        return sqlQuery;
    }

    /**
     * Gets the configuration map.
     *
     * @return an immutable copy of the configuration
     */
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    /**
     * Gets the chart configuration JSON (for CHART type).
     *
     * @return the chart config, or null if not set
     */
    public String getChartConfig() {
        return (String) configuration.get("chartConfig");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof VisualizationState other))
            return false;
        return type == other.type && Objects.equals(sqlQuery, other.sqlQuery)
                && Objects.equals(configuration, other.configuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sqlQuery, configuration);
    }

    @Override
    public String toString() {
        return "VisualizationState{" + "type=" + type + ", sqlQuery='"
                + sqlQuery + '\'' + ", configuration=" + configuration + '}';
    }
}
