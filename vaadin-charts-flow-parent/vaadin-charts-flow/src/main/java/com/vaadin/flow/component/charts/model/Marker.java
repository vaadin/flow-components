package com.vaadin.flow.component.charts.model;

/*-
 * #%L
 * Vaadin Charts for Flow
 * %%
 * Copyright 2000-2022 Vaadin Ltd.
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
 */

import com.vaadin.flow.component.charts.model.style.Color;

/**
 * In Highcharts 1.0, the appearance of all markers belonging to the hovered
 * series. For settings on the hover state of the individual point, see
 * <a href="#plotOptions.series.marker.states.hover">marker.states.hover</a>.
 */
public class Marker extends AbstractConfigurationObject {

    private Boolean enabled;
    private Color fillColor;
    private Number height;
    private Color lineColor;
    private Number lineWidth;
    private Number radius;
    private States states;
    private Number width;
    private MarkerSymbol symbol;

    public Marker() {
    }

    public Marker(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the point marker. If <code>null</code>, the markers are
     * hidden when the data is dense, and shown for more widespread data points.
     * <p>
     * Defaults to: null
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setFillColor(Color)
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * The fill color of the point marker. When <code>null</code>, the series'
     * or point's color is used.
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    /**
     * @see #setHeight(Number)
     */
    public Number getHeight() {
        return height;
    }

    /**
     * Image markers only. Set the image width explicitly. When using this
     * option, a <code>width</code> must also be set.
     * <p>
     * Defaults to: null
     */
    public void setHeight(Number height) {
        this.height = height;
    }

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * The color of the point marker's outline. When <code>null</code>, the
     * series' or point's color is used.
     * <p>
     * Defaults to: #ffffff
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
     * The width of the point marker's outline.
     * <p>
     * Defaults to: 0
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setRadius(Number)
     */
    public Number getRadius() {
        return radius;
    }

    /**
     * The radius of the point marker.
     * <p>
     * Defaults to: 4
     */
    public void setRadius(Number radius) {
        this.radius = radius;
    }

    /**
     * @see #setStates(States)
     */
    public States getStates() {
        if (states == null) {
            states = new States();
        }
        return states;
    }

    public void setStates(States states) {
        this.states = states;
    }

    /**
     * @see #setWidth(Number)
     */
    public Number getWidth() {
        return width;
    }

    /**
     * Image markers only. Set the image width explicitly. When using this
     * option, a <code>height</code> must also be set.
     * <p>
     * Defaults to: null
     */
    public void setWidth(Number width) {
        this.width = width;
    }

    /**
     * @see #setSymbol(MarkerSymbol)
     */
    public MarkerSymbol getSymbol() {
        return symbol;
    }

    public void setSymbol(MarkerSymbol symbol) {
        this.symbol = symbol;
    }
}
