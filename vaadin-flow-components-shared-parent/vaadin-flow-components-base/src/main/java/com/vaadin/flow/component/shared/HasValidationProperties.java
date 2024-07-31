/*
 * Copyright 2000-2024 Vaadin Ltd.
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
import com.vaadin.flow.component.HasValidation;

/**
 * Mixin interface for components that provide properties for setting invalid
 * state and error message string to show when invalid.
 *
 * @author Vaadin Ltd
 */
public interface HasValidationProperties extends HasElement, HasValidation {

    /**
     * Sets an error message to display for all constraint violations.
     * <p>
     * This error message takes priority over i18n error messages when both are
     * set.
     *
     * @param errorMessage
     *            the error message to set, or {@code null} to clear
     */
    @Override
    default void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * Gets the error message displayed for all constraint violations.
     *
     * @return the error message
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
