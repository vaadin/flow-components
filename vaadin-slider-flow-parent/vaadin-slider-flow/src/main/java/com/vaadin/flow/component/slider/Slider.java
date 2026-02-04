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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableBiFunction;

/**
 * Slider is an input field that allows the user to select a numeric value
 * within a range by dragging a handle along a track or using arrow keys for
 * precise input.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-slider")
@NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha5")
@JsModule("@vaadin/slider/src/vaadin-slider.js")
public class Slider extends SliderBase<Slider, Double> {
    private static final double DEFAULT_MIN = 0.0;
    private static final double DEFAULT_MAX = 100.0;
    private static final double DEFAULT_STEP = 1.0;

    private static final SerializableBiFunction<Slider, Double, Double> PARSER = (
            component, value) -> {
        try {
            component.requireValidValue(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            // Ignore invalid values from the client side
            return component.getValue();
        }

        return value;
    };

    private static final SerializableBiFunction<Slider, Double, Double> FORMATTER = (
            component, value) -> {
        component.requireValidValue(value);
        return value;
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
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    public double getMin() {
        return super.getMinDouble();
    }

    /**
     * Sets the minimum value of the slider.
     * <p>
     * If the current value is less than the new minimum, it's automatically
     * clamped, which triggers a value change event. To set both the minimum and
     * value explicitly, use the {@link #setValue(Double, double, double)
     * setValue(value, min, max)} method instead.
     *
     * @param min
     *            the minimum value
     * @throws IllegalArgumentException
     *             if min is greater than the current max
     */
    public void setMin(double min) {
        requireValidRange(min, getMax());
        setMinDouble(min);

        double adjustedValue = Math.max(getValue(), min);
        setValue(adjustedValue);
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public double getMax() {
        return super.getMaxDouble();
    }

    /**
     * Sets the maximum value of the slider.
     * <p>
     * If the current value is greater than the new maximum, it's automatically
     * clamped, which triggers a value change event. To set both the maximum and
     * value explicitly, use the {@link #setValue(Double, double, double)
     * setValue(value, min, max)} method instead.
     *
     * @param max
     *            the maximum value
     * @throws IllegalArgumentException
     *             if max is less than the current min
     */
    public void setMax(double max) {
        requireValidRange(getMin(), max);
        setMaxDouble(max);

        double adjustedValue = Math.min(getValue(), max);
        setValue(adjustedValue);
    }

    /**
     * Gets the step value of the slider.
     * <p>
     * Valid slider values are calculated relative to the minimum value:
     * {@code min}, {@code min + step}, {@code min + 2*step}, etc.
     *
     * @return the step value
     */
    public double getStep() {
        return super.getStepDouble();
    }

    /**
     * Sets the step value of the slider.
     * <p>
     * If the current value is not aligned with the new step, it's automatically
     * adjusted to the nearest value that matches the step, which triggers a
     * value change event. To set both the step and value explicitly, use the
     * {@link #setValue(Double, double, double, double) setValue(value, min,
     * max, step)} method instead.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if step is not positive
     */
    public void setStep(double step) {
        requireValidStep(step);
        setStepDouble(step);

        BigDecimal minBd = BigDecimal.valueOf(getMin());
        BigDecimal maxBd = BigDecimal.valueOf(getMax());
        BigDecimal stepBd = BigDecimal.valueOf(step);
        BigDecimal valueBd = BigDecimal.valueOf(getValue());

        // Equivalent to Math.round((value - min) / step)
        BigDecimal stepsFromMinBd = valueBd.subtract(minBd).divide(stepBd, 0,
                RoundingMode.HALF_UP);

        // Equivalent to Math.min(min + stepsFromMin * step, max)
        BigDecimal adjustedValue = minBd.add(stepsFromMinBd.multiply(stepBd))
                .min(maxBd);

        setValue(adjustedValue.doubleValue());
    }

    /**
     * Clears the slider value, setting it to the minimum value.
     *
     * @see #getMin()
     */
    @Override
    public void clear() {
        setValue(getMin());
    }

    @Override
    void requireValidValue(double min, double max, double step, Double value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    "Value must be between min and max");
        }

        if (BigDecimal.valueOf(value).remainder(BigDecimal.valueOf(step))
                .compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Value is not aligned with step");
        }
    }
}
