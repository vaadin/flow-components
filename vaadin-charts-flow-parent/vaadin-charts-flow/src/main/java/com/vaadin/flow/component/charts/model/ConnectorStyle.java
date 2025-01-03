/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * A configuration object to configure style of connectors (dependencies)
 * between two points in a Gantt chart.
 */
@SuppressWarnings("unused")
public class ConnectorStyle extends AbstractConfigurationObject {

    private DashStyle dashStyle;
    private Marker endMarker;
    private Color lineColor;
    private Number lineWidth;
    private Marker marker;
    private Marker startMarker;
    private PathfinderType type;

    /**
     * @see #setDashStyle(DashStyle)
     */
    public DashStyle getDashStyle() {
        return dashStyle;
    }

    /**
     * Set the default dash style for this chart's connecting lines. Defaults to
     * solid.
     */
    public void setDashStyle(DashStyle dashStyle) {
        this.dashStyle = dashStyle;
    }

    /**
     * @see #setEndMarker(Marker)
     */
    public Marker getEndMarker() {
        return endMarker;
    }

    /**
     * Marker options specific to the end markers for this chart's Pathfinder
     * connectors. Overrides the generic marker options.
     */
    public void setEndMarker(Marker endMarker) {
        this.endMarker = endMarker;
    }

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Set the default color for this chart's Pathfinder connecting lines.
     * Defaults to the color of the point being connected.
     */
    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    /**
     * @see #setLineWidth(Number)
     */
    public Number getLineWidth() {
        return lineWidth;
    }

    /**
     * Set the default pixel width for this chart's Pathfinder connecting lines.
     * Defaults to 1.
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setMarker(Marker)
     */
    public Marker getMarker() {
        return marker;
    }

    /**
     * Marker options for this chart's Pathfinder connectors. Note that this
     * option is overridden by the startMarker and endMarker options.
     */
    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    /**
     * @see #setStartMarker(Marker)
     */
    public Marker getStartMarker() {
        return startMarker;
    }

    /**
     * Marker options specific to the start markers for this chart's Pathfinder
     * connectors. Overrides the generic marker options.
     */
    public void setStartMarker(Marker startMarker) {
        this.startMarker = startMarker;
    }

    /**
     * @see #setType(PathfinderType)
     */
    public PathfinderType getType() {
        return type;
    }

    /**
     * Set the default pathfinder algorithm to use for this chart.
     * 
     * @see PathfinderType
     */
    public void setType(PathfinderType type) {
        this.type = type;
    }
}
