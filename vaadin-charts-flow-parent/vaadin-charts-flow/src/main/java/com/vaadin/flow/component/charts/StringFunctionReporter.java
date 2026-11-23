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
import java.util.Map;

import com.vaadin.flow.internal.ContentSecurityPolicyViolationReporter;

import tools.jackson.databind.JsonNode;

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

    /**
     * The prefix of the JSON keys whose string value the browser compiles into
     * a function.
     */
    private static final String FUNCTION_KEY_PREFIX = "_fn_";

    private StringFunctionReporter() {
        // Only static helpers
    }

    /**
     * Reports the given JSON if it has a JavaScript function as a string, at
     * any depth, and returns it.
     *
     * @param json
     *            the JSON that is about to be sent to the browser
     * @return the given JSON
     */
    static <T extends JsonNode> T report(T json) {
        if (hasStringFunction(json)) {
            ContentSecurityPolicyViolationReporter.reportStringJavaScript();
        }
        return json;
    }

    private static boolean hasStringFunction(JsonNode node) {
        if (node == null) {
            return false;
        }
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> property : node.properties()) {
                if (property.getKey().startsWith(FUNCTION_KEY_PREFIX)
                        && property.getValue().isString()) {
                    return true;
                }
                if (hasStringFunction(property.getValue())) {
                    return true;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) {
                if (hasStringFunction(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
