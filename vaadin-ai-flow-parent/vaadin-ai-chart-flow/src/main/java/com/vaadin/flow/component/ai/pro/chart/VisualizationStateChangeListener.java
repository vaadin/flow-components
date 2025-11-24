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
 * Listener for visualization state change events.
 *
 * @author Vaadin Ltd
 */
@FunctionalInterface
public interface VisualizationStateChangeListener extends Serializable {

    /**
     * Called when the visualization state changes.
     *
     * @param event
     *            the state change event
     */
    void onStateChange(VisualizationStateChangeEvent event);
}
