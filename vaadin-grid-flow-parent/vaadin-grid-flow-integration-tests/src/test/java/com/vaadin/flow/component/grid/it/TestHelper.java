/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.grid.it;

public class TestHelper {

    /**
     * Strips comments from the given HTML string.
     *
     * @param html
     *            the html String
     * @return the stripped html
     */
    static String stripComments(String html) {
        return html.replaceAll("<!--.*?-->", "");
    }
}
