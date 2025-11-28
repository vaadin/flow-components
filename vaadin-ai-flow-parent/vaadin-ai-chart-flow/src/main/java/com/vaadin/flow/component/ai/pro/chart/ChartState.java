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
 * Model representing the serializable state of a chart.
 * <p>
 * This immutable state object captures the chart configuration in JSON format,
 * allowing it to be persisted and restored later.
 * </p>
 *
 * <h3>Example Usage:</h3>
 *
 * <pre>
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
public final class ChartState implements Serializable {

    private final String configurationJson;

    /**
     * Creates a new chart state with the given configuration JSON.
     *
     * @param configurationJson
     *            the chart configuration in JSON format
     */
    public ChartState(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    /**
     * Gets the chart configuration JSON.
     *
     * @return the configuration JSON
     */
    public String getConfigurationJson() {
        return configurationJson;
    }
}
