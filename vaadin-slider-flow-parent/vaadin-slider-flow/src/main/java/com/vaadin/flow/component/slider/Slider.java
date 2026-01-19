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

import java.util.Objects;

import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Tag;

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

    private final static double DEFAULT_MIN = 0;
    private final static double DEFAULT_MAX = 100;
    private final static double DEFAULT_STEP = 1;

    /**
     * Constructs a {@code Slider} with a default range of 0 to 100, a step of
     * 1, and an initial value of 0.
     */
    public Slider() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP, DEFAULT_MIN);
    }

    /**
     * Constructs a {@code Slider} with a value change listener, a default range
     * of 0 to 100, a step of 1, and an initial value of 0.
     *
     * @param listener
     *            the value change listener
     */
    public Slider(
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP, DEFAULT_MIN, listener);
    }

    /**
     * Constructs a {@code Slider} with the given min, max, step, and initial
     * value.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @param value
     *            the initial value
     */
    public Slider(double min, double max, double step, double value) {
        super(min, max, step, value);
    }

    /**
     * Constructs a {@code Slider} with the given min, max, step, initial value,
     * and a value change listener.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @param value
     *            the initial value
     * @param listener
     *            the value change listener
     */
    public Slider(double min, double max, double step, double value,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(min, max, step, value);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a {@code Slider} with the given label, a default range of 0 to
     * 100, a step of 1, and an initial value of 0.
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
     * listener, a default range of 0 to 100, a step of 1, and an initial value
     * of 0.
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
     * Constructs a {@code Slider} with the given label, min, max, step, and
     * initial value.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @param value
     *            the initial value
     */
    public Slider(String label, double min, double max, double step,
            double value) {
        this(min, max, step, value);
        setLabel(label);
    }

    /**
     * Constructs a {@code Slider} with the given label, min, max, step, initial
     * value, and a value change listener.
     *
     * @param label
     *            the text to set as the label
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @param value
     *            the initial value
     * @param listener
     *            the value change listener
     */
    public Slider(String label, double min, double max, double step,
            double value,
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(min, max, step, value, listener);
        setLabel(label);
    }

    /**
     * @throws IllegalArgumentException
     *             if the value is not between min and max or not aligned with
     *             the step value
     */
    @Override
    public void setValue(Double value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value < getMin() || value > getMax()) {
            throw new IllegalArgumentException(
                    "The value must be between min and max");
        }

        if (value % getStep() != 0) {
            throw new IllegalArgumentException(
                    "The value is not aligned with the step value");
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
        super.setMinDouble(min);

        scheduleBeforeClientResponse("min", () -> {
            if (getValue() < getMin()) {
                LoggerFactory.getLogger(Slider.class).warn(
                        """
                                Value {} is below the minimum of {}. \
                                This may happen when the minimum was changed but the value was not updated. \
                                Consider increasing the value or decreasing the minimum to avoid inconsistent behavior.
                                """,
                        getValue(), getMin());
            }
        });
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    public double getMin() {
        return getMinDouble();
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
        super.setMaxDouble(max);

        scheduleBeforeClientResponse("max", () -> {
            if (getValue() > getMax()) {
                LoggerFactory.getLogger(Slider.class).warn(
                        """
                                Current value {} exceeds the maximum of {}. \
                                This may happen when the maximum was changed but the value was not updated. \
                                Consider reducing the value or increasing the maximum to avoid inconsistent behavior.
                                """,
                        getValue(), getMax());
            }
        });
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public double getMax() {
        return getMaxDouble();
    }

    /**
     * Sets the step value of the slider. The step is the amount the value
     * changes when the user moves the handle.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentExceptionm
     *             if the step is less than or equal to zero
     */
    public void setStep(double step) {
        super.setStepDouble(step);

        scheduleBeforeClientResponse("step", () -> {
            if (getValue() % getStep() != 0) {
                LoggerFactory.getLogger(Slider.class).warn(
                        """
                                Value {} is not aligned with the step {}. \
                                This may happen when the step was changed but the value was not updated. \
                                Consider adjusting the value to align with the step to avoid inconsistent behavior.
                                """,
                        getValue(), getStep());
            }
        });
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    public double getStep() {
        return getStepDouble();
    }
}
