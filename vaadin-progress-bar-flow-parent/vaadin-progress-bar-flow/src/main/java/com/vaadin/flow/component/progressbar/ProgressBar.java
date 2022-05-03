/*
 * Copyright 2000-2022 Vaadin Ltd.
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
 *
 */

package com.vaadin.flow.component.progressbar;

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Progress Bar shows the completion status of a task or process. The progress
 * can be determinate or indeterminate. Use Progress Bar to show an ongoing
 * process that takes a noticeable time to finish.
 *
 * @author Vaadin Ltd.
 */
@NpmPackage(value = "@vaadin/progress-bar", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-progress-bar", version = "23.1.0-beta1")
public class ProgressBar extends GeneratedVaadinProgressBar<ProgressBar>
        implements HasSize {

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
        double min = getMin();
        double max = getMax();
        if (min > value || value > max) {
            throw new IllegalArgumentException(String.format(
                    "value must be between min ('%s') and max ('%s')", min,
                    max));
        }
        super.setValue(value);
    }

    /**
     * Gets the current value of the progressbar
     *
     * @return the {@code value} property of the progressbar
     */
    public double getValue() {
        return getValueDouble();
    }

    /**
     * Sets the maximum bound of the progressbar.
     *
     * @param max
     *            the double value to set
     */
    public void setMax(double max) {
        super.setMax(max);
    }

    /**
     * Gets the maximum bound of the progressbar.
     *
     * @return the {@code max} property of the progressbar
     */
    public double getMax() {
        return getMaxDouble();
    }

    /**
     * Sets the minimum bound of the progressbar
     *
     * @param min
     *            the double value to set
     */
    public void setMin(double min) {
        super.setMin(min);
    }

    /**
     * Gets the minimum bound of the progressbar.
     *
     * @return the {@code min} property of the progressbar
     */
    public double getMin() {
        return getMinDouble();
    }

    @Override
    public void setIndeterminate(boolean indeterminate) {
        super.setIndeterminate(indeterminate);
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
        return isIndeterminateBoolean();
    }
}
