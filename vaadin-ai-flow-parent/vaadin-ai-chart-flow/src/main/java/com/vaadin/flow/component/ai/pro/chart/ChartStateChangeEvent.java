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
 * is updated through the controller's tools.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class ChartStateChangeEvent extends EventObject {

    private final ChartAiController.ChartState state;

    /**
     * Creates a new chart state change event.
     *
     * @param source the controller that fired the event
     * @param state the new chart state
     */
    public ChartStateChangeEvent(ChartAiController source,
                                  ChartAiController.ChartState state) {
        super(source);
        this.state = state;
    }

    /**
     * Gets the controller that fired this event.
     *
     * @return the source controller
     */
    @Override
    public ChartAiController getSource() {
        return (ChartAiController) super.getSource();
    }

    /**
     * Gets the current chart state after the change.
     *
     * @return the chart state
     */
    public ChartAiController.ChartState getState() {
        return state;
    }
}
