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
package com.vaadin.flow.component.progressbar;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.signals.Signal;

/**
 * Progress Bar shows the completion status of a task or process. The progress
 * can be determinate or indeterminate. Use Progress Bar to show an ongoing
 * process that takes a noticeable time to finish.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-progress-bar")
@NpmPackage(value = "@vaadin/progress-bar", version = "25.1.0-alpha7")
@JsModule("@vaadin/progress-bar/src/vaadin-progress-bar.js")
public class ProgressBar extends Component
        implements HasSize, HasStyle, HasThemeVariant<ProgressBarVariant> {

    /**
     * Constructs a new object with a scale of 0 to 1, and an initial value of
     * 0.
     */
    public ProgressBar() {
        this(0.0, 1.0);
    }

    /**
     * Constructs a new object with a scale of {@code min} to {@code max}, and
     * an initial value of {@code min}.
     * <p/>
     * {@code min} must be less than {@code max}.
     *
     * @param min
     *            the low end of the scale of progress
     * @param max
     *            the high end of the scale of progress
     *
     * @throws IllegalArgumentException
     *             if {@code min} is not less than {@code max}
     */
    public ProgressBar(double min, double max) {
        this(min, max, min);
    }

    /**
     * Constructs a new object with a scale of {@code min} to {@code max}, and
     * an initial value of {@code value}.
     * <p/>
     * {@code min} must be less than {@code max}, and {@code value} must be
     * between {@code min} and {@code max} (inclusive).
     *
     * @param min
     *            the low end of the scale of progress
     * @param max
     *            the high end of the scale of progress
     * @param value
     *            the initial value
     *
     * @throws IllegalArgumentException
     *             if {@code min} is not less than {@code max}, or {@code value}
     *             is not between {@code min} and {@code max}
     */
    public ProgressBar(double min, double max, double value) {
        if (min >= max) {
            throw new IllegalArgumentException(String.format(
                    "min ('%s') must be less than max ('%s')", min, max));
        }
        if (min > value || value > max) {
            throw new IllegalArgumentException(String.format(
                    "value must be between min ('%s') and max ('%s')", min,
                    max));
        }
        setMin(min);
        setMax(max);
        setValue(value);
    }

    /**
     * Sets value to the progressbar.
     *
     * @param value
     *            the double value to set
     */
    public void setValue(double value) {
        getElement().setProperty("value", value);
    }

    /**
     * Gets the current value of the progressbar
     *
     * @return the {@code value} property of the progressbar
     */
    public double getValue() {
        return getElement().getProperty("value", 0.0);
    }

    /**
     * Binds the given signal to the value of the progressbar as a one-way
     * binding so that the property is updated when the signal's value is
     * updated.
     * <p>
     * When a signal is bound, the value is kept synchronized with the signal
     * value while the component is attached. When the component is detached,
     * signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the value manually through
     * {@link #setValue(double)} throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the value to, not {@code null}
     * @see #setValue(double)
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal)
     * @since 25.1
     */
    public void bindValue(Signal<Double> signal) {
        getElement().bindProperty("value", signal);
    }

    /**
     * Sets the maximum bound of the progressbar.
     *
     * @param max
     *            the double value to set
     */
    public void setMax(double max) {
        getElement().setProperty("max", max);
    }

    /**
     * Gets the maximum bound of the progressbar.
     *
     * @return the {@code max} property of the progressbar
     */
    public double getMax() {
        return getElement().getProperty("max", 0.0);
    }

    /**
     * Sets the minimum bound of the progressbar
     *
     * @param min
     *            the double value to set
     */
    public void setMin(double min) {
        getElement().setProperty("min", min);
    }

    /**
     * Gets the minimum bound of the progressbar.
     *
     * @return the {@code min} property of the progressbar
     */
    public double getMin() {
        return getElement().getProperty("min", 0.0);
    }

    /**
     * Binds the given signal to the minimum bound of the progressbar.
     * <p>
     * When a signal is bound, the minimum bound is kept synchronized with the
     * signal value while the component is attached. When the component is
     * detached, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the minimum bound manually
     * through {@link #setMin(double)} throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the minimum bound to, not {@code null}
     * @see #setMin(double)
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal)
     * @since 25.1
     */
    public void bindMin(Signal<Double> signal) {
        getElement().bindProperty("min", signal);
    }

    /**
     * Binds the given signal to the maximum bound of the progressbar.
     * <p>
     * When a signal is bound, the maximum bound is kept synchronized with the
     * signal value while the component is attached. When the component is
     * detached, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the maximum bound manually
     * through {@link #setMax(double)} throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the maximum bound to, not {@code null}
     * @see #setMax(double)
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal)
     * @since 25.1
     */
    public void bindMax(Signal<Double> signal) {
        getElement().bindProperty("max", signal);
    }

    /**
     * Indeterminate state of the progressbar. This property takes precedence
     * over other state properties (min, max, value).
     *
     * @param indeterminate
     *            the boolean value to set
     */
    public void setIndeterminate(boolean indeterminate) {
        getElement().setProperty("indeterminate", indeterminate);
    }

    /**
     * Get the indeterminate state of the progressbar
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code indeterminate} property of the progressbar
     */
    public boolean isIndeterminate() {
        return getElement().getProperty("indeterminate", false);
    }
}
