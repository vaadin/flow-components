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

/**
 * Represents a snapshot of the chart's state that can be persisted and
 * restored. This includes the SQL query and chart configuration.
 * <p>
 * Chart states are immutable and can be safely stored in databases, session
 * storage, or other persistence mechanisms. The state captures only the
 * essential information needed to restore a chart:
 * </p>
 * <ul>
 * <li>SQL query - used to fetch the chart data from the database</li>
 * <li>Chart configuration - Highcharts JSON configuration for visual
 * appearance</li>
 * </ul>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * // Capture current state
 * ChartState state = orchestrator.captureState();
 *
 * // Store it (application-specific)
 * stateManager.saveState("my-chart-id", state);
 *
 * // Later, restore it
 * ChartState savedState = stateManager.loadState("my-chart-id");
 * orchestrator.restoreState(savedState);
 * </pre>
 *
 * @author Vaadin Ltd
 * @see AiChartOrchestrator#captureState()
 * @see AiChartOrchestrator#restoreState(ChartState)
 */
public interface ChartState extends Serializable {

    /**
     * Gets the SQL query used to fetch the chart data.
     * <p>
     * This query will be re-executed when the state is restored to populate
     * the chart with current data from the database.
     * </p>
     *
     * @return the SQL query, or null if no data query has been set
     */
    String getSqlQuery();

    /**
     * Gets the chart configuration in Highcharts JSON format.
     * <p>
     * This configuration defines the chart's visual appearance including chart
     * type, title, axis labels, colors, and other display properties.
     * </p>
     *
     * @return the chart configuration JSON, or null if no configuration has
     *         been set
     */
    String getChartConfig();

    /**
     * Creates a new ChartState instance with the specified SQL query and chart
     * configuration.
     *
     * @param sqlQuery
     *            the SQL query to fetch chart data, may be null
     * @param chartConfig
     *            the Highcharts JSON configuration, may be null
     * @return a new ChartState instance
     */
    static ChartState of(String sqlQuery, String chartConfig) {
        return new DefaultChartState(sqlQuery, chartConfig);
    }
}
