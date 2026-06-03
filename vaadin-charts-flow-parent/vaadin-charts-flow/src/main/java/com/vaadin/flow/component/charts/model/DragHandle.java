/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

import com.vaadin.flow.component.charts.model.style.Color;

@SuppressWarnings("unused")
public class DragHandle extends AbstractConfigurationObject {

    private String className;
    private Color color;
    private String cursor;
    private Color lineColor;
    private Number lineWidth;
    private String _fn_pathFormatter;
    private Number zIndex;

    /**
     * @see #setClassName(String)
     */
    public String getClassName() {
        return className;
    }

    /**
     * The class name of the drag handles. Defaults to highcharts-drag-handle.
     * Defaults to highcharts-drag-handle.
     *
     * @param className
     *            the class name
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
     * The fill color of the drag handles. Defaults to #fff.
     *
     * @param color
     *            the color
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
     * The mouse cursor to use for the drag handles. By default this is
     * intelligently switching between ew-resize and ns-resize depending on the
     * direction the point is being dragged.
     *
     * @param cursor
     *            the cursor
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
     * The line color of the drag handles. Defaults to rgba(0, 0, 0, 0.6).
     *
     * @param lineColor
     *            the line color
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
     * The line width for the drag handles. Defaults to 1.
     *
     * @param lineWidth
     *            the line width
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
     * The z index for the drag handles. Defaults to 901.
     *
     * @param zIndex
     *            the z index
     */
    public void setzIndex(Number zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * @see #setPathFormatter(String)
     */
    public String getPathFormatter() {
        return _fn_pathFormatter;
    }

    /**
     * Function to define the SVG path to use for the drag handles. Takes the
     * point as argument. Should return an SVG path in array format. The SVG
     * path is automatically positioned on the point.
     *
     * @param _fn_pathFormatter
     *            the path formatter
     */
    public void setPathFormatter(String _fn_pathFormatter) {
        this._fn_pathFormatter = _fn_pathFormatter;
    }
}
