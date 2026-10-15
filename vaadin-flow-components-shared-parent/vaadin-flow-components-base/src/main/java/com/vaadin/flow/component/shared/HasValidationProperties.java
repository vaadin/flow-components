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
import com.vaadin.flow.component.HasValidation;

/**
 * Mixin interface for components that provide properties for setting invalid
 * state and error message string to show when invalid.
 *
 * @author Vaadin Ltd
 */
public interface HasValidationProperties extends HasElement, HasValidation {

    /**
     * Sets the error message to show to the user when the component is invalid.
     *
     * @param errorMessage
     *            the error message or {@code null} to clear it
     */
    @Override
    default void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * Gets the error message to show to the when the component is invalid.
     *
     * @return the error message or {@code null} if not set
     */
    @Override
    default String getErrorMessage() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * Sets the invalid state of the component.
     * <p>
     * NOTE: If you need to manually control the invalid state, consider
     * enabling manual validation mode with
     * {@link #setManualValidation(boolean)} to avoid potential conflicts
     * between your custom validation and the component's built-in validation.
     *
     * @param invalid
     *            {@code true} for invalid, {@code false} for valid
     */
    @Override
    default void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    /**
     * Gets whether the component is currently in invalid state.
     *
     * @return {@code true} for invalid, {@code false} for valid
     */
    @Override
    default boolean isInvalid() {
        return getElement().getProperty("invalid", false);
    }
}
