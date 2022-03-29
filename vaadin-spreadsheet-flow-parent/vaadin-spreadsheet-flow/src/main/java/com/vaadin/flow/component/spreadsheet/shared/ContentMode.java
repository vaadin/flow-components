/*
 * #%L
 * Vaadin Spreadsheet
 * %%
 * Copyright (C) 2013 - 2022 Vaadin Ltd
 * %%
 * This program is available under Commercial Vaadin Developer License
 * 4.0 (CVDLv4).
 *
 * For the full License, see <https://vaadin.com/license/cvdl-4.0>.
 * #L%
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
