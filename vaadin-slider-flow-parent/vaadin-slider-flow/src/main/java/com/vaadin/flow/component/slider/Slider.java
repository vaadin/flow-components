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
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;

/**
 * Slider is an input component that allows the user to select a numeric value
 * within a range by dragging a handle along a track.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-slider")
// @NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha1")
// @JsModule("@vaadin/slider/src/vaadin-slider.js")
public class Slider extends SliderBase<Slider, Double>
        implements HasSize, Focusable<Slider>, KeyNotifier {

    private final static double DEFAULT_MIN = 0.0;
    private final static double DEFAULT_MAX = 100.0;

    /**
     * Constructs a slider with a default range of 0 to 100 and an initial value
     * of 0.
     */
    public Slider() {
        this((String) null);
    }

    /**
     * Constructs a slider with a value change listener, a default range of 0 to
     * 100, and an initial value of 0.
     *
     * @param listener
     *            the value change listener
     */
    public Slider(
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this((String) null, listener);
    }

    /**
     * Constructs a slider with the given label, a default range of 0 to 100,
     * and an initial value of 0.
     *
     * @param label
     *            the text to set as the label
     */
    public Slider(String label) {
        this(label, DEFAULT_MIN, DEFAULT_MAX);
    }

    /**
     * Constructs a slider with the given label, value change listener, a
     * default range of 0 to 100, and an initial value of 0.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     */
    public Slider(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(label, DEFAULT_MIN, DEFAULT_MAX, listener);
    }

    /**
     * Constructs a slider with the given min and max values. The initial value
     * is set to the minimum.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public Slider(double min, double max) {
        this((String) null, min, max);
    }

    /**
     * Constructs a slider with the given min and max values, and a value change
     * listener. The initial value is set to the minimum.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param listener
     *            the value change listener
     */
    public Slider(double min, double max,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this((String) null, min, max, listener);
    }

    /**
     * Constructs a slider with the given label, min, and max values. The
     * initial value is set to the minimum.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public Slider(String label, double min, double max) {
        super(label, min, max, min);
    }

    /**
     * Constructs a slider with the given label, min, and max values, and a
     * value change listener. The initial value is set to the minimum.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param listener
     *            the value change listener
     */
    public Slider(String label, double min, double max,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        super(label, min, max, min, listener);
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
