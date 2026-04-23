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
 * IntegerRangeSlider is an input field that allows the user to select an
 * integer range within bounds by dragging two thumbs along a track or using
 * arrow keys for precise input.
 * <p>
 * IntegerRangeSlider uses {@link Integer} as the value type for its start and
 * end values, see {@link DecimalRangeSlider} for a version of the component
 * that supports decimal values.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-range-slider")
@NpmPackage(value = "@vaadin/slider", version = "25.2.0-alpha8")
@JsModule("@vaadin/slider/src/vaadin-range-slider.js")
public class IntegerRangeSlider extends
        NumberRangeSlider<IntegerRangeSlider, IntegerRangeSliderValue, Integer> {

    /**
     * Constructs an {@code IntegerRangeSlider} with min 0 and max 100. The
     * initial value is [0, 100].
     * <p>
     * The step defaults to 1.
     */
    public IntegerRangeSlider() {
        this((int) DEFAULT_MIN, (int) DEFAULT_MAX);
    }

    /**
     * Constructs an {@code IntegerRangeSlider} with the given min and max. The
     * initial value is set to [min, max].
     * <p>
     * The step defaults to 1.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public IntegerRangeSlider(int min, int max) {
        super(min, max, IntegerRangeSliderValue::new, Number::intValue,
                Number::doubleValue);
    }

    /**
     * Constructs an {@code IntegerRangeSlider} with the given label, min 0, and
     * max 100. The initial value is [0, 100].
     * <p>
     * The step defaults to 1.
     *
     * @param label
     *            the text to set as the label
     */
    public IntegerRangeSlider(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an {@code IntegerRangeSlider} with the given label, min and
     * max. The initial value is set to [min, max].
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
    public IntegerRangeSlider(String label, int min, int max) {
        this(min, max);
        setLabel(label);
    }
}
