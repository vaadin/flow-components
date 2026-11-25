/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts.model;

/**
 * Under which conditions the rule applies.
 * 
 * @since 6.0.1
 */
public class Condition extends AbstractConfigurationObject {

    private String _fn_callback;
    private ChartFunction callback;
    private Number maxHeight;
    private Number maxWidth;
    private Number minHeight;
    private Number minWidth;

    public Condition() {
    }

    public String getCallback() {
        return _fn_callback;
    }

    /**
     * Sets the JavaScript function of the <code>callback</code> option, as the
     * source code of the function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setCallbackFunction(ChartFunction)}, which works without
     * compiling JavaScript in the browser.
     *
     * @param _fn_callback
     *            the source code of the JavaScript function
     */
    public void setCallback(String _fn_callback) {
        this._fn_callback = _fn_callback;
        this.callback = null;
    }

    /**
     * @see #setCallbackFunction(ChartFunction)
     */
    public ChartFunction getCallbackFunction() {
        return callback;
    }

    /**
     * Sets the function of the <code>callback</code> option, declared in a
     * {@link com.vaadin.flow.js.JsDefinition JsDefinition} interface. The
     * browser runs the function from the frontend bundle, so it works under a
     * Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setCallback(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAiController</code>.
     *
     * @param callback
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setCallbackFunction(ChartFunction callback) {
        this.callback = callback;
        this._fn_callback = null;
    }

    /**
     * @see #setMaxHeight(Number)
     */
    public Number getMaxHeight() {
        return maxHeight;
    }

    /**
     * The responsive rule applies if the chart height is less than this.
     */
    public void setMaxHeight(Number maxHeight) {
        this.maxHeight = maxHeight;
    }

    /**
     * @see #setMaxWidth(Number)
     */
    public Number getMaxWidth() {
        return maxWidth;
    }

    /**
     * The responsive rule applies if the chart width is less than this.
     */
    public void setMaxWidth(Number maxWidth) {
        this.maxWidth = maxWidth;
    }

    /**
     * @see #setMinHeight(Number)
     */
    public Number getMinHeight() {
        return minHeight;
    }

    /**
     * The responsive rule applies if the chart height is greater than this.
     * <p>
     * Defaults to: 0
     */
    public void setMinHeight(Number minHeight) {
        this.minHeight = minHeight;
    }

    /**
     * @see #setMinWidth(Number)
     */
    public Number getMinWidth() {
        return minWidth;
    }

    /**
     * The responsive rule applies if the chart width is greater than this.
     * <p>
     * Defaults to: 0
     */
    public void setMinWidth(Number minWidth) {
        this.minWidth = minWidth;
    }
}
