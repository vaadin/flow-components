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

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;

/**
 * Slider is an input field that allows the user to select a numeric value
 * within a range by dragging a handle along a track or using arrow keys for
 * precise input.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-slider")
// @NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha1")
// @JsModule("@vaadin/slider/src/vaadin-slider.js")
public class Slider extends SliderBase<Slider, Double> {

    private final static double DEFAULT_MIN = 0.0;
    private final static double DEFAULT_MAX = 100.0;

    /**
     * Constructs a {@code Slider} with a default range of 0 to 100 and an
     * initial value of 0.
     */
    public Slider() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_MIN);
    }

    /**
     * Constructs a {@code Slider} with a value change listener, a default range
     * of 0 to 100, and an initial value of 0.
     *
     * @param listener
     *            the value change listener
     */
    public Slider(
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_MIN, listener);
    }

    /**
     * Constructs a {@code Slider} with the given min, max, and initial value.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     */
    public Slider(double min, double max, double value) {
        super(min, max, value);
    }

    /**
     * Constructs a {@code Slider} with the given min, max, initial value, and a
     * value change listener.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     * @param listener
     *            the value change listener
     */
    public Slider(double min, double max, double value,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(min, max, value);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a {@code Slider} with the given label, a default range of 0 to
     * 100, and an initial value of 0.
     *
     * @param label
     *            the text to set as the label
     */
    public Slider(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs a {@code Slider} with the given label and a value change
     * listener, a default range of 0 to 100, and an initial value of 0.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     */
    public Slider(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(listener);
        setLabel(label);
    }

    /**
     * Constructs a {@code Slider} with the given label, min, max, and initial
     * value.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     */
    public Slider(String label, double min, double max, double value) {
        this(min, max, value);
        setLabel(label);
    }

    /**
     * Constructs a {@code Slider} with the given label, min, max, initial
     * value, and a value change listener.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     * @param listener
     *            the value change listener
     */
    public Slider(String label, double min, double max, double value,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(min, max, value, listener);
        setLabel(label);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        boolean enabled = featureFlags
                .isEnabled(SliderFeatureFlagProvider.SLIDER_COMPONENT);

        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * @throws IllegalArgumentException
     *             if the value is not between min and max
     */
    @Override
    public void setValue(Double value) {
        if (value < getMin() || value > getMax()) {
            throw new IllegalArgumentException(
                    "The value must be between min and max");
        }

        super.setValue(value);
    }
}
