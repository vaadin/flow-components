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
 * Mixin interface for components that support a clear button.
 * <p>
 * Used to toggle the visibility of the clear button.
 *
 * @author Vaadin Ltd
 */
public interface HasClearButton extends HasElement {

    /**
     * Gets the visibility of the button which clears the field, which is
     * {@code false} by default.
     *
     * @return <code>true</code> if the button is visible, <code>false</code>
     *         otherwise
     */
    default boolean isClearButtonVisible() {
        return getElement().getProperty("clearButtonVisible", false);
    }

    /**
     * Sets the visibility of the button which clears the field.
     *
     * @param clearButtonVisible
     *            <code>true</code> to show the clear button, <code>false</code>
     *            to hide it
     */
    default void setClearButtonVisible(boolean clearButtonVisible) {
        getElement().setProperty("clearButtonVisible", clearButtonVisible);
    }
}
