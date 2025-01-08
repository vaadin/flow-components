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
 * A configuration object to configure style of connectors (dependencies)
 * between two points in a Gantt chart.
 */
@SuppressWarnings("unused")
public class ChartConnectors extends ConnectorStyle {

    private Number algorithmMargin;
    private Boolean enabled;

    /**
     * @see #setAlgorithmMargin(Number)
     */
    public Number getAlgorithmMargin() {
        return algorithmMargin;
    }

    /**
     * Set the default pathfinder margin to use, in pixels. Some Pathfinder
     * algorithms attempt to avoid obstacles, such as other points in the chart.
     * These algorithms use this margin to determine how close lines can be to
     * an obstacle. The default is to compute this automatically from the size
     * of the obstacles in the chart. To draw connecting lines close to existing
     * points, set this to a low number. For more space around existing points,
     * set this number higher.
     */
    public void setAlgorithmMargin(Number algorithmMargin) {
        this.algorithmMargin = algorithmMargin;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable connectors for this chart. Requires Highcharts Gantt. Defaults to
     * true.
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
