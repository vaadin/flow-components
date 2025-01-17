/**
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

public class GuideBoxDefaultState extends AbstractConfigurationObject {

    private String className;
    private Color color;
    private String cursor;
    private Color lineColor;
    private Number lineWidth;
    private Number zIndex;

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * CSS class name of the guide box in this state. Defaults to
     * highcharts-drag-box-default. Defaults to highcharts-drag-box-default.
     *
     * @param className
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @see #setColor(Color)
     */
    public Color getColor() {
        return color;
    }

    /**
     * Guide box fill color. Defaults to rgba(0, 0, 0, 0.1).
     *
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see #setCursor(String)
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * Guide box cursor. Defaults to "move".
     * 
     * @param cursor
     */
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    /**
     * @see #setLineColor(Color)
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Color of the border around the guide box. Defaults to #888.
     *
     * @param lineColor
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
     * Width of the line around the guide box. Defaults to 1.
     * 
     * @param lineWidth
     */
    public void setLineWidth(Number lineWidth) {
        this.lineWidth = lineWidth;
    }

    /**
     * @see #setzIndex(Number)
     */
    public Number getzIndex() {
        return zIndex;
    }

    /**
     * Guide box zIndex. Defaults to 900.
     * 
     * @param zIndex
     */
    public void setzIndex(Number zIndex) {
        this.zIndex = zIndex;
    }
}
