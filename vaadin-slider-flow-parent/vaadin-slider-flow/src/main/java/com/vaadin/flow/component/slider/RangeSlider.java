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
package com.vaadin.flow.component.slider;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * RangeSlider is an input field that allows the user to select a numeric range
 * within bounds by dragging two thumbs along a track or using arrow keys for
 * precise input.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-range-slider")
@NpmPackage(value = "@vaadin/slider", version = "25.2.0-alpha7")
@JsModule("@vaadin/slider/src/vaadin-range-slider.js")
public class RangeSlider
        extends NumberRangeSlider<RangeSlider, RangeSliderValue, Double> {

    /**
     * Constructs a {@code RangeSlider} with min 0 and max 100. The initial
     * value is [0, 100].
     * <p>
     * The step defaults to 1.
     */
    public RangeSlider() {
        this(DEFAULT_MIN, DEFAULT_MAX);
    }

    /**
     * Constructs a {@code RangeSlider} with the given min and max. The initial
     * value is set to [min, max].
     * <p>
     * The step defaults to 1.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public RangeSlider(double min, double max) {
        super(min, max, RangeSliderValue::new, v -> v, v -> v);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min 0, and max
     * 100. The initial value is [0, 100].
     * <p>
     * The step defaults to 1.
     *
     * @param label
     *            the text to set as the label
     */
    public RangeSlider(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min and max. The
     * initial value is set to [min, max].
     * <p>
     * The step defaults to 1.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public RangeSlider(String label, double min, double max) {
        this(min, max);
        setLabel(label);
    }
}
