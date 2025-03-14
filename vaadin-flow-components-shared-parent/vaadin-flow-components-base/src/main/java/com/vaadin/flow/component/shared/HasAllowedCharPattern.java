/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
