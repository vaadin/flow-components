/**
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.spreadsheet.shared;

/**
 * Defines how the client should interpret textual values.
 *
 * @since 8.0
 */
public enum ContentMode {
    /**
     * Textual values are displayed as plain text.
     */
    TEXT,

    /**
     * Textual values are displayed as preformatted text. In this mode newlines
     * are preserved when rendered on the screen.
     */
    PREFORMATTED,

    /**
     * Textual values are interpreted and displayed as HTML. Care should be
     * taken when using this mode to avoid Cross-site Scripting (XSS) issues.
     */
    HTML

}
