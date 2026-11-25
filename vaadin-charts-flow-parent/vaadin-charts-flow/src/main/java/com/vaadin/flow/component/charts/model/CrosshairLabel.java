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
 * <p>
 * A label on the axis next to the crosshair.
 * </p>
 *
 * <p>
 * In
 * <a href="http://www.highcharts.com/docs/chart-design-and-style/style-by-css"
 * >styled mode</a>, the label is styled with the
 * <code>.highcharts-crosshair-label</code> class.
 * </p>
 * 
 * @since 6.0.1
 */
public class CrosshairLabel extends AbstractConfigurationObject {

    private HorizontalAlign align;
    private Color backgroundColor;
    private Color borderColor;
    private Number borderRadius;
    private Number borderWidth;
    private String format;
    private String _fn_formatter;
    private ChartFunction formatter;
    private Number padding;
    private Shape shape;
    private Boolean enabled;

    public CrosshairLabel() {
    }

    /**
     * @see #setAlign(HorizontalAlign)
     */
    public HorizontalAlign getAlign() {
        return align;
    }

    /**
     * Alignment of the label compared to the axis. Defaults to
     * <code>left</code> for right-side axes, <code>right</code> for left-side
     * axes and <code>center</code> for horizontal axes.
     */
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }

    /**
     * @see #setBackgroundColor(Color)
     * @since 18.0
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * The background color for the label. Defaults to the related series color,
     * or <code>#666666</code> if that is not available.
     * 
     * @since 18.0
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * @see #setBorderColor(Color)
     * @since 18.0
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * The border color for the crosshair label
     * 
     * @since 18.0
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * @see #setBorderRadius(Number)
     */
    public Number getBorderRadius() {
        return borderRadius;
    }

    /**
     * The border corner radius of the crosshair label.
     * <p>
     * Defaults to: 3
     */
    public void setBorderRadius(Number borderRadius) {
        this.borderRadius = borderRadius;
    }

    /**
     * @see #setBorderWidth(Number)
     * @since 18.0
     */
    public Number getBorderWidth() {
        return borderWidth;
    }

    /**
     * The border width for the crosshair label.
     * <p>
     * Defaults to: 0
     * 
     * @since 18.0
     */
    public void setBorderWidth(Number borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * @see #setFormat(String)
     */
    public String getFormat() {
        return format;
    }

    /**
     * A format string for the crosshair label. Defaults to <code>{value}</code>
     * for numeric axes and <code>{value:%b %d, %Y}</code> for datetime axes.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormatter() {
        return _fn_formatter;
    }

    /**
     * Sets the JavaScript function of the <code>formatter</code> option, as the
     * source code of the function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * Where a format string is enough, use {@link #setFormat(String)}, which
     * needs no JavaScript.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setFormatterFunction(ChartFunction)}, which works without
     * compiling JavaScript in the browser.
     *
     * @param _fn_formatter
     *            the source code of the JavaScript function
     */
    public void setFormatter(String _fn_formatter) {
        this._fn_formatter = _fn_formatter;
        this.formatter = null;
    }

    /**
     * @see #setFormatterFunction(ChartFunction)
     */
    public ChartFunction getFormatterFunction() {
        return formatter;
    }

    /**
     * Sets the function of the <code>formatter</code> option, declared in a
     * {@link com.vaadin.flow.js.JsDefinition JsDefinition} interface. The
     * browser runs the function from the frontend bundle, so it works under a
     * Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setFormatter(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAiController</code>.
     *
     * @param formatter
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setFormatterFunction(ChartFunction formatter) {
        this.formatter = formatter;
        this._fn_formatter = null;
    }

    /**
     * @see #setPadding(Number)
     */
    public Number getPadding() {
        return padding;
    }

    /**
     * Padding inside the crosshair label.
     * <p>
     * Defaults to: 8
     */
    public void setPadding(Number padding) {
        this.padding = padding;
    }

    /**
     * @see #setShape(Shape)
     */
    public Shape getShape() {
        return shape;
    }

    /**
     * The shape to use for the label box.
     * <p>
     * Defaults to: callout
     */
    public void setShape(Shape shape) {
        this.shape = shape;
    }

    public CrosshairLabel(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @see #setEnabled(Boolean)
     */
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
