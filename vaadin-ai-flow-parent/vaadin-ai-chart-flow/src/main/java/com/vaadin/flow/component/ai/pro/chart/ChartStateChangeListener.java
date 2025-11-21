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
import java.util.EventListener;

/**
 * Listener for chart state change events.
 * <p>
 * Implement this interface to receive notifications when the chart's state
 * changes (SQL query or configuration updates).
 * </p>
 *
 * @author Vaadin Ltd
 * @see ChartStateChangeEvent
 * @see AiChartOrchestrator#addStateChangeListener(ChartStateChangeListener)
 */
@FunctionalInterface
public interface ChartStateChangeListener
        extends EventListener, Serializable {

    /**
     * Called when the chart's state changes.
     *
     * @param event
     *            the state change event
     */
    void onStateChange(ChartStateChangeEvent event);
}
