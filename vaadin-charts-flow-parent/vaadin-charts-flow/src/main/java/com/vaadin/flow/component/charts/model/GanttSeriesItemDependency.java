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
 * A configuration object to express dependencies between tasks in a Gantt
 * chart.
 */
@SuppressWarnings("unused")
public class GanttSeriesItemDependency extends ConnectorStyle {

    private String to;

    public GanttSeriesItemDependency() {
    }

    public GanttSeriesItemDependency(String to) {
        this.to = to;
    }

    /**
     * @see #setTo(String)
     */
    public String getTo() {
        return to;
    }

    /**
     * The ID of the point to connect to.
     */
    public void setTo(String to) {
        this.to = to;
    }
}
