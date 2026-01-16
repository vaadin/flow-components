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
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Slider is an input component that allows the user to select a numeric value
 * within a range by dragging a handle along a track.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-slider")
// @NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha1")
// @JsModule("@vaadin/slider/src/vaadin-slider.js")
public class Slider extends AbstractSinglePropertyField<Slider, Double>
        implements HasSize {

    /**
     * Constructs a new Slider with default values (min=0, max=100, value=0).
     */
    public Slider() {
        this(0, 100);
    }

    /**
     * Constructs a new Slider with the given min and max values. The initial
     * value is set to min.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public Slider(double min, double max) {
        this(min, max, min);
    }

    /**
     * Constructs a new Slider with the given min, max, and initial value.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     */
    public Slider(double min, double max, double value) {
        super("value", 0.0, false);
        setMin(min);
        setMax(max);
        setValue(value);
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

    /**
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     * @throws IllegalArgumentException
     *             if the min is greater than the max value
     */
    public void setMin(double min) {
        if (min > getMax()) {
            throw new IllegalArgumentException(
                    "The min value cannot be greater than the max value");
        }

        getElement().setProperty("min", min);
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    public double getMin() {
        return getElement().getProperty("min", 0.0);
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     * @throws IllegalArgumentException
     *             if the max is less than the min value
     */
    public void setMax(double max) {
        if (max < getMin()) {
            throw new IllegalArgumentException(
                    "The max value cannot be less than the min value");
        }

        getElement().setProperty("max", max);
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public double getMax() {
        return getElement().getProperty("max", 100.0);
    }

    /**
     * Sets the step value of the slider. The step is the amount the value
     * changes when the user moves the handle.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if the step is less than or equal to zero
     */
    public void setStep(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException(
                    "The step value must be a positive number");
        }

        getElement().setProperty("step", step);
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    public double getStep() {
        return getElement().getProperty("step", 1.0);
    }
}
