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
 * Slider is an input field that allows the user to select a numeric value
 * within a range by dragging a handle along a track or using arrow keys for
 * precise input.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-slider")
@NpmPackage(value = "@vaadin/slider", version = "25.2.0-alpha6")
@JsModule("@vaadin/slider/src/vaadin-slider.js")
public class Slider extends NumberSlider<Slider, Double> {
    /**
     * Constructs a {@code Slider} with min 0 and max 100. The initial value is
     * 0.
     * <p>
     * The step defaults to 1.
     */
    public Slider() {
        this(DEFAULT_MIN, DEFAULT_MAX);
    }

    /**
     * Constructs a {@code Slider} with the given min and max. The initial value
     * is set to the minimum value.
     * <p>
     * The step defaults to 1.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public Slider(double min, double max) {
        super(min, max, (v) -> v, (v) -> v);
    }

    /**
     * Constructs a {@code Slider} with the given label, min 0, and max 100. The
     * initial value is 0.
     * <p>
     * The step defaults to 1.
     *
     * @param label
     *            the text to set as the label
     */
    public Slider(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs a {@code Slider} with the given label, min and max. The
     * initial value is set to the minimum value.
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
    public Slider(String label, double min, double max) {
        this(min, max);
        setLabel(label);
    }
}
