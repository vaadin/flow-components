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

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.signals.Signal;

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

    /**
     * Binds a given signal to the visibility of the clear button.
     * <p>
     * When a signal is bound, the clear button visibility is kept synchronized
     * with the signal value while the element is in the attached state. When
     * the element is detached, signal value changes have no effect.
     * <p>
     * Passing {@code null} as the signal unbinds the existing binding.
     * <p>
     * While a signal is bound, any attempt to set the visibility manually
     * through {@link #setClearButtonVisible(boolean)} throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     * <p>
     * Attempting to bind a new signal while one is already bound throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     * <p>
     * Signal's value {@code null} is treated as {@code false}.
     *
     * @param signal
     *            the signal to bind the clear button visibility to, or
     *            {@code null} to unbind
     * @see #setClearButtonVisible(boolean)
     * @since 25.1
     */
    default void bindClearButtonVisible(Signal<Boolean> signal) {
        getElement().bindProperty("clearButtonVisible", signal == null ? null
                : signal.map(
                        visible -> visible == null ? Boolean.FALSE : visible));
    }
}
