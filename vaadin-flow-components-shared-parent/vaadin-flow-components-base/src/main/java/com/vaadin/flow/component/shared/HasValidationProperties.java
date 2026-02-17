/*
 * Copyright 2000-2026 Vaadin Ltd.
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

import java.util.Objects;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.signals.Signal;

/**
 * Mixin interface for components that provide properties for setting invalid
 * state and error message string to show when invalid.
 *
 * @author Vaadin Ltd
 */
public interface HasValidationProperties extends HasElement, HasValidation {

    /**
     * Sets a single error message to display for all constraint violations. The
     * error message will only appear when the component is flagged as invalid,
     * either as a result of constraint validation or by the developer through
     * {@link #setInvalid(boolean)} if manual validation mode is enabled.
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
     * Gets the error message displayed for all constraint violations if it has
     * been set with {@link #setErrorMessage(String)}. Otherwise, gets the
     * current i18n error message if the value is currently invalid.
     *
     * @return the error message
     */
    @Override
    default String getErrorMessage() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * {@inheritDoc}
     * <p>
     * Binds a given signal to the single error message to display for all
     * constraint violations. The error message will only appear when the
     * component is flagged as invalid, either as a result of constraint
     * validation or by the developer through {@link #setInvalid(boolean)} if
     * manual validation mode is enabled.
     * <p>
     * Signal's value {@code null} clears the error message.
     *
     * @param signal
     *            the signal to bind the error message to, not {@code null}
     * @since 25.1
     */
    @Override
    default void bindErrorMessage(Signal<String> signal) {
        Objects.requireNonNull(signal, "Signal cannot be null");
        getElement().bindProperty("errorMessage", signal
                .map(errorMessage -> errorMessage == null ? "" : errorMessage),
                null);
    }

    /**
     * Sets the invalid state of the component.
     * <p>
     * NOTE: If you need to manually control the invalid state, enable manual
     * validation mode with {@link #setManualValidation(boolean)} to avoid
     * potential conflicts between your custom validation and the component's
     * constraint validation.
     *
     * @param invalid
     *            {@code true} for invalid, {@code false} for valid
     */
    @Override
    default void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
        getElement().executeJs("this.invalid = $0", invalid);
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

    /**
     * Binds a given boolean signal to the invalid state of the component.
     * <p>
     * The invalid state controls whether the component is considered invalid
     * and whether a possible error message is shown. If manual validation mode
     * is enabled with {@link #setManualValidation(boolean)}, then the value
     * provided by the bound signal determines the visual invalid state of the
     * component.
     * <p>
     * Signal's value {@code null} is treated as {@code false}.
     *
     * @param signal
     *            the signal to bind the invalid state to, not {@code null}
     * @since 25.1
     */
    @Override
    default void bindInvalid(Signal<Boolean> signal) {
        Objects.requireNonNull(signal, "Signal cannot be null");
        getElement().bindProperty("invalid",
                signal.map(
                        invalid -> invalid == null ? Boolean.FALSE : invalid),
                null);
    }
}
