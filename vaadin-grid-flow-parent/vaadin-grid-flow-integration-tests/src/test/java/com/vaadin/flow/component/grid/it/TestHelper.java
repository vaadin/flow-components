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
