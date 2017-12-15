/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import com.vaadin.ui.common.HasSize;

/**
 * Server-side component for the {@code vaadin-progress-bar} element.
 *
 * @author Vaadin Ltd.
 */
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
            throw new IllegalArgumentException("min must be less than max");
        }
        setMin(min);
        setMax(max);
        setValue(value);
    }

    @Override
    public void setValue(double value) {
        if (getMin() > value || value > getMax()) {
            throw new IllegalArgumentException(
                    "value must be between min and max");
        }
        super.setValue(value);
    }
}
