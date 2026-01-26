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

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.function.SerializableBiFunction;

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
    private static final double DEFAULT_MIN = 0;
    private static final double DEFAULT_MAX = 100;
    private static final double DEFAULT_STEP = 1;

    private static final SerializableBiFunction<Slider, Double, Double> PARSER = (component, value) -> {
        return component.requireValidValue(value);
    };

    private static final SerializableBiFunction<Slider, Double, Double> FORMATTER = (component, value) -> {
        return component.requireValidValue(value);
    };

    /**
     * Constructs a {@code Slider} with range 0-100 and initial value 0.
     * <p>
     * The step defaults to 1.
     */
    public Slider() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP, DEFAULT_MIN);
    }

    /**
     * Constructs a {@code Slider} with range 0-100, initial value 0, and a
     * value change listener.
     * <p>
     * The step defaults to 1.
     *
     * @param listener
     *            the value change listener
     */
    public Slider(
            ValueChangeListener<? super ComponentValueChangeEvent<Slider, Double>> listener) {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP, DEFAULT_MIN, listener);
    }

    /**
     * Constructs a {@code Slider} with the given range and initial value.
     * <p>
     * The step defaults to 1.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     */
    public Slider(double min, double max, double value) {
        this(min, max, DEFAULT_STEP, value);
    }

    /**
     * Constructs a {@code Slider} with the given range, initial value, and a
     * value change listener.
     * <p>
     * The step defaults to 1.
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
        this(min, max, DEFAULT_STEP, value, listener);
    }

    /**
     * Constructs a {@code Slider} with the given range, step, and initial
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
        super(min, max, step, value, Double.class, PARSER, FORMATTER);
    }

    /**
     * Constructs a {@code Slider} with the given range, step, initial value,
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
     * Constructs a {@code Slider} with the given label, range 0-100, and
     * initial value 0.
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
     * Constructs a {@code Slider} with the given label, range 0-100, initial
     * value 0, and a value change listener.
     * <p>
     * The step defaults to 1.
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
     * Constructs a {@code Slider} with the given label, range, and initial
     * value.
     * <p>
     * The step defaults to 1.
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
     * Constructs a {@code Slider} with the given label, range, initial value,
     * and a value change listener.
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

    /**
     * Constructs a {@code Slider} with the given label, range, step, and
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
     * Constructs a {@code Slider} with the given label, range, step, initial
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
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     * @throws IllegalArgumentException
     *             if the min is greater than the max value
     * @throws IllegalArgumentException
     *             if the current value is less than the new minimum value
     */
    public void setMin(double min) {
        if (getValue() < min) {
            throw new IllegalArgumentException(
                    "The current value {} is less than the new minimum value {}".formatted(
                            getValue(), min));
        }

        super.setMinDouble(min);
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
     * @throws IllegalArgumentException
     *             if the current value is greater than the new maximum value
     */
    public void setMax(double max) {
        if (getValue() > max) {
            throw new IllegalArgumentException(
                    "The current value {} is greater than the new maximum value {}".formatted(
                            getValue(), max));
        }

        super.setMaxDouble(max);
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
     * @throws IllegalArgumentException
     *             if the current value is not aligned with the new step value
     */
    public void setStep(double step) {
        if (getValue() % step != 0) {
            throw new IllegalArgumentException(
                    "The current value {} is not aligned with the new step value {}".formatted(
                            getValue(), step));
        }

        super.setStepDouble(step);
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    public double getStep() {
        return getStepDouble();
    }

    private double requireValidValue(Double value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value < getMin() || value > getMax()) {
            throw new IllegalArgumentException(
                    "The value must be between min and max");
        }

        if (value % getStep() != 0) {
            throw new IllegalArgumentException(
                    "The value is not aligned with the step value");
        }

        return value;
    }
}
