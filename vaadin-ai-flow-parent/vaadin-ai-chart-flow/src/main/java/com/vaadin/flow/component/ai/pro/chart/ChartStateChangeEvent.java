/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.ai.pro.chart;

import java.util.EventObject;

/**
 * Event fired when the chart state changes.
 * <p>
 * This event is triggered when either the SQL query or chart configuration
 * is updated through the plugin's tools.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class ChartStateChangeEvent extends EventObject {

    private final AiChartPlugin.ChartState state;

    /**
     * Creates a new chart state change event.
     *
     * @param source the plugin that fired the event
     * @param state the new chart state
     */
    public ChartStateChangeEvent(AiChartPlugin source,
                                  AiChartPlugin.ChartState state) {
        super(source);
        this.state = state;
    }

    /**
     * Gets the plugin that fired this event.
     *
     * @return the source plugin
     */
    @Override
    public AiChartPlugin getSource() {
        return (AiChartPlugin) super.getSource();
    }

    /**
     * Gets the current chart state after the change.
     *
     * @return the chart state
     */
    public AiChartPlugin.ChartState getState() {
        return state;
    }
}
