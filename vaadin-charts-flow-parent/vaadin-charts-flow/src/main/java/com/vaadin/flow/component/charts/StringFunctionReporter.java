/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.charts;

import java.io.Serializable;

import com.vaadin.flow.component.charts.util.ChartSerialization.JsonWithFunctions;
import com.vaadin.flow.internal.ContentSecurityPolicyViolationReporter;

/**
 * Reports chart configuration that is sent to the browser with a JavaScript
 * function as a string, such as the one of
 * {@link com.vaadin.flow.component.charts.model.Tooltip#setFormatter(String)}.
 * The browser compiles such a function, which a Content Security Policy without
 * <code>unsafe-eval</code> blocks. The report follows the CSP mode: nothing
 * when it is off, a warning in the warn mode and a
 * {@link com.vaadin.flow.server.ContentSecurityPolicyViolationException} in the
 * strict mode.
 * <p>
 * For internal use only. May be renamed or removed in a future release.
 */
final class StringFunctionReporter implements Serializable {

    private StringFunctionReporter() {
        // Only static helpers
    }

    /**
     * Reports the given serialized configuration if it has a JavaScript
     * function as a string, at any depth, and returns it.
     *
     * @param json
     *            the configuration that is about to be sent to the browser
     * @return the given configuration
     */
    static JsonWithFunctions report(JsonWithFunctions json) {
        if (json.hasStringFunction()) {
            ContentSecurityPolicyViolationReporter.reportStringJavaScript();
        }
        return json;
    }
}
