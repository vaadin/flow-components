/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.HasElement;

/**
 * Mixin interface for field components that support setting auto open to
 * control whether the overlay should open on input click or not.
 *
 * @author Vaadin Ltd
 */
public interface HasAutoOpen extends HasElement {

    /**
     * Gets whether dropdown will open automatically or not.
     *
     * @return {@code true} if enabled, {@code false} otherwise
     */
    default boolean isAutoOpen() {
        return !getElement().getProperty("autoOpenDisabled", false);
    }

    /**
     * Enables or disables the dropdown opening automatically. If {@code false}
     * the dropdown is only opened when clicking the toggle button or pressing
     * Up or Down arrow keys.
     *
     * @param autoOpen
     *            {@code false} to prevent the dropdown from opening
     *            automatically
     */
    default void setAutoOpen(boolean autoOpen) {
        getElement().setProperty("autoOpenDisabled", !autoOpen);
    }
}
