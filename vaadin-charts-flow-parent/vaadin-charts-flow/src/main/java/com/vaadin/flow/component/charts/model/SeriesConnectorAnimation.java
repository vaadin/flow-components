/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Animation of Gantt series connector
 */
@SuppressWarnings("unused")
public class SeriesConnectorAnimation extends AbstractConfigurationObject {

    private Boolean reversed;

    /**
     * @see #setReversed(Boolean)
     */
    public Boolean getReversed() {
        return reversed;
    }

    /**
     * Defaults to true.
     * 
     * @param reversed
     */
    public void setReversed(Boolean reversed) {
        this.reversed = reversed;
    }
}
