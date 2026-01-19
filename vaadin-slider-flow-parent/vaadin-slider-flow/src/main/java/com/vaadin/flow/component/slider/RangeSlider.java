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
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;

/**
 * RangeSlider is an input field that allows the user to select a numeric range
 * within bounds by dragging two handles along a track or using arrow keys for
 * precise input.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-range-slider")
// @NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha1")
// @JsModule("@vaadin/slider/src/vaadin-range-slider.js")
public class RangeSlider extends SliderBase<RangeSlider, RangeSliderValue> {

    private static final SerializableFunction<ArrayNode, RangeSliderValue> PARSER = arrayNode -> new RangeSliderValue(
            arrayNode.get(0).asDouble(), arrayNode.get(1).asDouble());

    private static final SerializableFunction<RangeSliderValue, ArrayNode> FORMATTER = value -> {
        ArrayNode arrayNode = JacksonUtils.createArrayNode();
        arrayNode.add(value.start());
        arrayNode.add(value.end());
        return arrayNode;
    };

    private final static double DEFAULT_MIN = 0;
    private final static double DEFAULT_MAX = 100;
    private final static double DEFAULT_STEP = 1;

    /**
     * Constructs a {@code RangeSlider} with range 0-100 and initial value
     * 0-100.
     * <p>
     * The step defaults to 1.
     */
    public RangeSlider() {
        this(DEFAULT_MIN, DEFAULT_MAX, DEFAULT_STEP,
                new RangeSliderValue(DEFAULT_MIN, DEFAULT_MAX));
    }

    /**
     * Constructs a {@code RangeSlider} with the given range, step, and initial
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
    public RangeSlider(double min, double max, double step,
            RangeSliderValue value) {
        super(min, max, step, value, ArrayNode.class, PARSER, FORMATTER);
    }

    /**
     * @throws IllegalArgumentException
     *             if the value is not between min and max, not aligned with the
     *             step value, or if start is greater than end
     */
    @Override
    public void setValue(RangeSliderValue value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value.start() > value.end()) {
            throw new IllegalArgumentException(
                    "Start value cannot be greater than end value");
        }

        if (value.start() < getMin() || value.end() > getMax()) {
            throw new IllegalArgumentException(
                    "The value must be between min and max");
        }

        if (value.start() % getStep() != 0 || value.end() % getStep() != 0) {
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
            if (getValue().start() < getMin()) {
                LoggerFactory.getLogger(RangeSlider.class).warn(
                        """
                                Start value {} is below the minimum of {}. \
                                This may happen when the minimum was changed but the value was not updated. \
                                Consider increasing the value or decreasing the minimum to avoid inconsistent behavior.
                                """,
                        getValue().start(), getMin());
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
            if (getValue().end() > getMax()) {
                LoggerFactory.getLogger(RangeSlider.class).warn(
                        """
                                End value {} exceeds the maximum of {}. \
                                This may happen when the maximum was changed but the value was not updated. \
                                Consider reducing the value or increasing the maximum to avoid inconsistent behavior.
                                """,
                        getValue().end(), getMax());
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
     * changes when the user moves a handle.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if the step is less than or equal to zero
     */
    public void setStep(double step) {
        super.setStepDouble(step);

        scheduleBeforeClientResponse("step", () -> {
            RangeSliderValue value = getValue();
            if (value.start() % getStep() != 0
                    || value.end() % getStep() != 0) {
                LoggerFactory.getLogger(RangeSlider.class).warn(
                        """
                                Value [{}, {}] is not aligned with the step {}. \
                                This may happen when the step was changed but the value was not updated. \
                                Consider adjusting the value to align with the step to avoid inconsistent behavior.
                                """,
                        value.start(), value.end(), getStep());
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
