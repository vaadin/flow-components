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
 * @since 6.0.1
 */
public class ContextButtonMenuItem extends AbstractConfigurationObject {

    private String text;
    private String _fn_onclick;
    private ChartFunction onclick;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOnclick() {
        return _fn_onclick;
    }

    /**
     * Sets the JavaScript function of the <code>onclick</code> option, as the
     * source code of the function.
     * <p>
     * The browser compiles the function from this string, which a Content
     * Security Policy without <code>unsafe-eval</code> blocks: in the strict
     * CSP mode, sending a chart configuration that has a function throws a
     * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException}.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setOnclickFunction(ChartFunction)}, which works without compiling
     * JavaScript in the browser.
     *
     * @param _fn_onclick
     *            the source code of the JavaScript function
     */
    public void setOnclick(String _fn_onclick) {
        this._fn_onclick = _fn_onclick;
        this.onclick = null;
    }

    /**
     * @see #setOnclickFunction(ChartFunction)
     */
    public ChartFunction getOnclickFunction() {
        return onclick;
    }

    /**
     * Sets the function of the <code>onclick</code> option, declared in a
     * {@link com.vaadin.flow.js.JsDefinition JsDefinition} interface. The
     * browser runs the function from the frontend bundle, so it works under a
     * Content Security Policy without <code>unsafe-eval</code>.
     * <p>
     * Setting a function this way clears a function set with
     * {@link #setOnclick(String)}.
     * <p>
     * A chart exported to SVG with <code>SVGGenerator</code> does not have the
     * function. Neither does a chart state saved or restored with
     * <code>ChartAIController</code>.
     *
     * @param onclick
     *            the function, or <code>null</code> to remove it
     * @since 25.4
     */
    public void setOnclickFunction(ChartFunction onclick) {
        this.onclick = onclick;
        this._fn_onclick = null;
    }
}
