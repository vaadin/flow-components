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

import java.util.Arrays;
import java.util.Objects;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;

/**
 * RangeSlider is an input field that allows the user to select a numeric range
 * within bounds by dragging two thumbs along a track or using arrow keys for
 * precise input.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-range-slider")
@NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha6")
@JsModule("@vaadin/slider/src/vaadin-range-slider.js")
public class RangeSlider extends SliderBase<RangeSlider, RangeSliderValue> {
    private static final SerializableFunction<ArrayNode, RangeSliderValue> PARSER = (
            arrayValue) -> {
        return new RangeSliderValue(arrayValue.get(0).asDouble(),
                arrayValue.get(1).asDouble());
    };

    private static final SerializableFunction<RangeSliderValue, ArrayNode> FORMATTER = (
            value) -> {
        return JacksonUtils
                .listToJson(Arrays.asList(value.start(), value.end()));
    };

    private static final double DEFAULT_MIN = 0.0;
    private static final double DEFAULT_MAX = 100.0;
    private static final double DEFAULT_STEP = 1.0;

    /**
     * Constructs a {@code RangeSlider} with min 0, max 100, and initial value
     * [0, 100].
     * <p>
     * The step defaults to 1.
     */
    public RangeSlider() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP,
                new RangeSliderValue(DEFAULT_MIN, DEFAULT_MAX));
    }

    /**
     * Constructs a {@code RangeSlider} with min 0, max 100, initial value [0,
     * 100], and a value change listener.
     * <p>
     * The step defaults to 1.
     *
     * @param listener
     *            the value change listener
     */
    public RangeSlider(
            ValueChangeListener<? super ComponentValueChangeEvent<RangeSlider, RangeSliderValue>> listener) {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP,
                new RangeSliderValue(DEFAULT_MIN, DEFAULT_MAX), listener);
    }

    /**
     * Constructs a {@code RangeSlider} with the given min, max, and initial
     * value.
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
    public RangeSlider(double min, double max, RangeSliderValue value) {
        this(min, max, DEFAULT_STEP, value);
    }

    /**
     * Constructs a {@code RangeSlider} with the given min, max, initial value,
     * and a value change listener.
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
    public RangeSlider(double min, double max, RangeSliderValue value,
            ValueChangeListener<? super ComponentValueChangeEvent<RangeSlider, RangeSliderValue>> listener) {
        this(min, max, DEFAULT_STEP, value, listener);
    }

    /**
     * Constructs a {@code RangeSlider} with the given min, max, step, and
     * initial value.
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
    public RangeSlider(double min, double max, double step,
            RangeSliderValue value) {
        super(min, max, step, value, ArrayNode.class, PARSER, FORMATTER);
    }

    /**
     * Constructs a {@code RangeSlider} with the given min, max, step, initial
     * value, and a value change listener.
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
    public RangeSlider(double min, double max, double step,
            RangeSliderValue value,
            ValueChangeListener<? super ComponentValueChangeEvent<RangeSlider, RangeSliderValue>> listener) {
        this(min, max, step, value);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min 0, max 100,
     * and initial value [0, 100].
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
     * Constructs a {@code RangeSlider} with the given label, min 0, max 100,
     * initial value [0, 100], and a value change listener.
     * <p>
     * The step defaults to 1.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     */
    public RangeSlider(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<RangeSlider, RangeSliderValue>> listener) {
        this(listener);
        setLabel(label);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min, max, and
     * initial value.
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
    public RangeSlider(String label, double min, double max,
            RangeSliderValue value) {
        this(min, max, value);
        setLabel(label);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min, max, initial
     * value, and a value change listener.
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
     * @param listener
     *            the value change listener
     */
    public RangeSlider(String label, double min, double max,
            RangeSliderValue value,
            ValueChangeListener<? super ComponentValueChangeEvent<RangeSlider, RangeSliderValue>> listener) {
        this(min, max, value, listener);
        setLabel(label);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min, max, step,
     * and initial value.
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
    public RangeSlider(String label, double min, double max, double step,
            RangeSliderValue value) {
        this(min, max, step, value);
        setLabel(label);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min, max, step,
     * initial value, and a value change listener.
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
    public RangeSlider(String label, double min, double max, double step,
            RangeSliderValue value,
            ValueChangeListener<? super ComponentValueChangeEvent<RangeSlider, RangeSliderValue>> listener) {
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
     * This method automatically clamps both the current start and end values to
     * be no less than the new minimum, which may trigger a value change event.
     * To set the value explicitly along with the new minimum, use the
     * {@link #setValue(RangeSliderValue, double, double) setValue(value, min,
     * max)} method instead.
     *
     * @param min
     *            the minimum value
     * @throws IllegalArgumentException
     *             if min is greater than the current max
     */
    public void setMin(double min) {
        SliderUtil.requireValidMinMax(min, getMax());
        setMinDouble(min);

        RangeSliderValue value = getValue();
        setValue(new RangeSliderValue(
                SliderUtil.clampToMinMax(value.start(), min, getMax()),
                SliderUtil.clampToMinMax(value.end(), min, getMax())));
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
     * This method automatically clamps both the current start and end values to
     * be no greater than the new maximum, which may trigger a value change
     * event. To set the value explicitly along with the new maximum, use the
     * {@link #setValue(RangeSliderValue, double, double) setValue(value, min,
     * max)} method instead.
     *
     * @param max
     *            the maximum value
     * @throws IllegalArgumentException
     *             if max is less than the current min
     */
    public void setMax(double max) {
        SliderUtil.requireValidMinMax(getMin(), max);
        setMaxDouble(max);

        RangeSliderValue value = getValue();
        setValue(new RangeSliderValue(
                SliderUtil.clampToMinMax(value.start(), getMin(), max),
                SliderUtil.clampToMinMax(value.end(), getMin(), max)));
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
     * This method automatically adjusts both the current start and end values
     * to be aligned with the new step, which may trigger a value change event.
     * To set the value explicitly along with the new step, use the
     * {@link #setValue(RangeSliderValue, double, double, double)
     * setValue(value, min, max, step)} method instead.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if step is not positive
     */
    public void setStep(double step) {
        SliderUtil.requireValidStep(step);
        setStepDouble(step);

        RangeSliderValue value = getValue();
        setValue(new RangeSliderValue(
                SliderUtil.snapToStep(value.start(), getMin(), getMax(), step),
                SliderUtil.snapToStep(value.end(), getMin(), getMax(), step)));
    }

    /**
     * Clears the slider value, setting it to the full range from minimum to
     * maximum.
     *
     * @see #getMin()
     * @see #getMax()
     */
    @Override
    public void clear() {
        setValue(new RangeSliderValue(getMin(), getMax()));
    }

    @Override
    protected boolean hasValidValue() {
        try {
            ArrayNode arrayValue = (ArrayNode) getElement()
                    .getPropertyRaw("value");
            requireValidValue(PARSER.apply(arrayValue));
            return true;
        } catch (IllegalArgumentException | NullPointerException
                | ClassCastException e) {
            return false;
        }
    }

    @Override
    void requireValidValue(double min, double max, double step,
            RangeSliderValue value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (SliderUtil.clampToMinMax(value.start(), min, max) != value
                .start()) {
            throw new IllegalArgumentException(
                    "Start value must be between min and max");
        }

        if (SliderUtil.clampToMinMax(value.end(), min, max) != value.end()) {
            throw new IllegalArgumentException(
                    "End value must be between min and max");
        }

        if (SliderUtil.snapToStep(value.start(), min, max, step) != value
                .start()) {
            throw new IllegalArgumentException(
                    "Start value must be aligned with step");
        }

        if (SliderUtil.snapToStep(value.end(), min, max, step) != value.end()) {
            throw new IllegalArgumentException(
                    "End value must be aligned with step");
        }
    }
}
