/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import com.vaadin.flow.component.ai.state.AiStateSupport;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.internal.JacksonUtils;

/**
 * Helper that captures and restores ChartState using AiStateSupport.
 * <p>
 * This class provides a simple way to capture and restore the state of a chart
 * component for persistence or session management.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * Chart chart = new Chart();
 * ChartStateSupport stateSupport = new ChartStateSupport(chart);
 *
 * // Capture current state
 * ChartState snapshot = stateSupport.capture();
 *
 * // Save to database or session
 * persistToDatabase(snapshot);
 *
 * // Later: restore from saved state
 * ChartState savedState = loadFromDatabase();
 * stateSupport.restore(savedState);
 * </pre>
 *
 * @author Vaadin Ltd
 */
public final class ChartStateSupport implements AiStateSupport<ChartState> {

    private final Chart chart;

    /**
     * Creates a new chart state support instance.
     *
     * @param chart
     *            the chart to manage state for
     */
    public ChartStateSupport(Chart chart) {
        this.chart = chart;
    }

    @Override
    public ChartState capture() {
        try {
            String config = JacksonUtils.beanToJson(chart.getConfiguration()).toString();
            return new ChartState(config);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to capture chart state: " + e.getMessage(), e);
        }
    }

    @Override
    public void restore(ChartState state) {
        try {
            var configNode = JacksonUtils.readTree(state.getConfigurationJson());
            Configuration config = JacksonUtils.readToObject(configNode, Configuration.class);

            // Copy configuration to the chart
            chart.setConfiguration(config);
            chart.drawChart();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to restore chart state: " + e.getMessage(), e);
        }
    }
}
