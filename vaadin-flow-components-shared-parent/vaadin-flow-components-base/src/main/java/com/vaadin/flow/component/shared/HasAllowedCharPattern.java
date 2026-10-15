/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
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
     * A regular expression that is used on the client-side to determine which
     * characters the user is allowed to type into the field. Each typed
     * character is matched individually against this expression, and only added
     * to the input if it matches.
     *
     * @return the {@code allowedCharPattern} property
     */
    default String getAllowedCharPattern() {
        return getElement().getProperty("allowedCharPattern", "");
    }

    /**
     * Sets a regular expression that is used on the client-side to determine
     * which characters the user is allowed to type into the field. Each typed
     * character is matched individually against this expression, and only added
     * to the input if it matches. The allowed char pattern must be a valid
     * JavaScript Regular Expression that matches a single character, not the
     * sequence of characters.
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
