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

/**
 * @since 24.7
 */
@SuppressWarnings("unused")
public class DragHandle extends AbstractConfigurationObject {

    private String className;
    private Color color;
    private String cursor;
    private Color lineColor;
    private Number lineWidth;
    private String _fn_pathFormatter;
    private ChartFunction pathFormatter;
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
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.CspViolationException}.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setPathFormatterFunction(ChartFunction)}, which works without
     * compiling JavaScript in the browser.
     *
     * @param _fn_pathFormatter
     *            the source code of the JavaScript function
     */
    public void setPathFormatter(String _fn_pathFormatter) {
        this._fn_pathFormatter = _fn_pathFormatter;
        this.pathFormatter = null;
    }

    /**
     * @see #setPathFormatterFunction(ChartFunction)
     */
    public ChartFunction getPathFormatterFunction() {
        return pathFormatter;
    }

    /**
     * Sets the function of the <code>pathFormatter</code> option, declared in a
     * {@link com.vaadin.flow.js.JsDefinition JsDefinition} interface. The
     * browser runs the function from the frontend bundle, so it works under a
     * Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setPathFormatter(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAIController</code>.
     *
     * @param pathFormatter
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setPathFormatterFunction(ChartFunction pathFormatter) {
        this.pathFormatter = pathFormatter;
        this._fn_pathFormatter = null;
    }
}
