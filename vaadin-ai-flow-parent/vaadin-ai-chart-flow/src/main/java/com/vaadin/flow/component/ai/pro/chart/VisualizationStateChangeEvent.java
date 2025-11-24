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
import java.util.Objects;

/**
 * Event fired when a visualization's state changes.
 * <p>
 * This event is fired when the visualization type, SQL query, or configuration
 * is updated through AI tools.
 * </p>
 *
 * @author Vaadin Ltd
 */
public class VisualizationStateChangeEvent implements Serializable {

    private final AiDataVisualizationOrchestrator source;
    private final VisualizationState visualizationState;

    /**
     * Creates a new state change event.
     *
     * @param source
     *            the orchestrator that fired the event
     * @param visualizationState
     *            the new state
     */
    public VisualizationStateChangeEvent(
            AiDataVisualizationOrchestrator source,
            VisualizationState visualizationState) {
        this.source = Objects.requireNonNull(source,
                "Source cannot be null");
        this.visualizationState = Objects.requireNonNull(visualizationState,
                "Visualization state cannot be null");
    }

    /**
     * Gets the orchestrator that fired this event.
     *
     * @return the source orchestrator
     */
    public AiDataVisualizationOrchestrator getSource() {
        return source;
    }

    /**
     * Gets the new visualization state.
     *
     * @return the visualization state
     */
    public VisualizationState getVisualizationState() {
        return visualizationState;
    }

    @Override
    public String toString() {
        return "VisualizationStateChangeEvent{" + "source=" + source
                + ", visualizationState=" + visualizationState + '}';
    }
}
