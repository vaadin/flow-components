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
     * {@link com.vaadin.flow.server.CspViolationException}.
     *
     * @param _fn_onclick
     *            the source code of the JavaScript function
     */
    public void setOnclick(String _fn_onclick) {
        this._fn_onclick = _fn_onclick;
    }
}
