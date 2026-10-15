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
 * Mixin interface for components that have an internal overlay and support
 * setting CSS class names on it for styling.
 *
 * @author Vaadin Ltd
 */
public interface HasOverlayClassName extends HasElement {

    /**
     * A space-delimited list of CSS class names to set on the overlay element.
     *
     * @return the {@code overlayClass} property
     */
    default String getOverlayClassName() {
        return getElement().getProperty("overlayClass", "");
    }

    /**
     * Sets a space-delimited list of CSS class names to set on the overlay
     * element.
     *
     * @param overlayClassName
     *            The overlay class name to set
     */
    default void setOverlayClassName(String overlayClassName) {
        getElement().setProperty("overlayClass",
                overlayClassName == null ? "" : overlayClassName);
    }
}
