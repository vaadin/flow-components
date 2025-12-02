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
 * Listener for chart state change events.
 * <p>
 * Implement this interface to be notified when the chart's SQL query or
 * configuration changes.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
 * AiChartPlugin plugin = new AiChartPlugin(chart, databaseProvider);
 *
 * plugin.addStateChangeListener(event -> {
 *     ChartState state = event.getState();
 *     System.out.println("Chart updated: " + state.sqlQuery());
 *
 *     // Persist the state
 *     saveToDatabase(state);
 * });
 * </pre>
 *
 * @author Vaadin Ltd
 */
@FunctionalInterface
public interface ChartStateChangeListener extends Serializable {

    /**
     * Called when the chart state changes.
     *
     * @param event the state change event containing the new state and change type
     */
    void onStateChange(ChartStateChangeEvent event);
}
