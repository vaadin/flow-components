/**
 * Copyright (C) 2000-2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for field components that support setting allowed char
 * pattern to prevent user from entering invalid characters when typing or
 * pasting text.
 *
 * @author Vaadin Ltd
 */
public interface HasAllowedCharPattern extends HasElement {

    /**
     * A regular expression that the user input is checked against. The allowed
     * pattern must matches a single character, not the sequence of characters.
     *
     * @return the {@code allowedCharPattern} property
     */
    default String getAllowedCharPattern() {
        return getElement().getProperty("allowedCharPattern", "");
    }

    /**
     * Sets a regular expression for the user input to pass on the client-side.
     * The allowed char pattern must be a valid JavaScript Regular Expression
     * that matches a single character, not the sequence of characters.
     * <p>
     * For example, to allow entering only numbers and slash character, use
     * {@code setAllowedCharPattern("[0-9/]")}`.
     * </p>
     *
     * @param pattern
     *            the String pattern to set
     */
    default void setAllowedCharPattern(String pattern) {
        getElement().setProperty("allowedCharPattern",
                pattern == null ? "" : pattern);
    }
}
