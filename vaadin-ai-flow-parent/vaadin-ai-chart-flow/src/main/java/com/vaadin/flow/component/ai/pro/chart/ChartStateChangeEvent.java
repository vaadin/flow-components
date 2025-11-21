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
 * Event fired when the chart's state changes.
 * <p>
 * This event is triggered when AI tools modify the chart's SQL query or
 * configuration. Applications can listen to these events to implement
 * auto-save functionality or other reactive behaviors.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * orchestrator.addStateChangeListener(event -> {
 *     ChartState newState = event.getChartState();
 *     stateManager.saveState("my-chart-id", newState);
 * });
 * </pre>
 *
 * @author Vaadin Ltd
 */
public class ChartStateChangeEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final ChartState chartState;
    private final StateChangeType changeType;

    /**
     * Describes what type of state change occurred.
     */
    public enum StateChangeType {
        /**
         * The SQL query was updated.
         */
        DATA_QUERY_UPDATED,

        /**
         * The chart configuration was updated.
         */
        CONFIGURATION_UPDATED,

        /**
         * Both SQL query and configuration were updated.
         */
        BOTH_UPDATED
    }

    /**
     * Creates a new chart state change event.
     *
     * @param source
     *            the orchestrator that fired the event
     * @param chartState
     *            the new chart state
     * @param changeType
     *            the type of change that occurred
     */
    public ChartStateChangeEvent(AiChartOrchestrator source,
            ChartState chartState, StateChangeType changeType) {
        super(source);
        this.chartState = chartState;
        this.changeType = changeType;
    }

    /**
     * Gets the new chart state after the change.
     *
     * @return the chart state
     */
    public ChartState getChartState() {
        return chartState;
    }

    /**
     * Gets the type of change that occurred.
     *
     * @return the change type
     */
    public StateChangeType getChangeType() {
        return changeType;
    }

    /**
     * Gets the orchestrator that fired this event.
     *
     * @return the orchestrator
     */
    @Override
    public AiChartOrchestrator getSource() {
        return (AiChartOrchestrator) super.getSource();
    }
}
