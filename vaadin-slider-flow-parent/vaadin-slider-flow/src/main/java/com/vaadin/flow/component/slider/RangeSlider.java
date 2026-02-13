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
import java.util.Optional;

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
@NpmPackage(value = "@vaadin/slider", version = "25.1.0-alpha7")
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

    /**
     * Constructs a {@code RangeSlider} with min 0 and max 100. The initial
     * value is [0, 100].
     * <p>
     * The step defaults to 1.
     */
    public RangeSlider() {
        this(DEFAULT_MIN, DEFAULT_MAX);
    }

    /**
     * Constructs a {@code RangeSlider} with the given min and max. The initial
     * value is set to [min, max].
     * <p>
     * The step defaults to 1.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     */
    public RangeSlider(double min, double max) {
        super(min, max, ArrayNode.class, PARSER, FORMATTER);
    }

    /**
     * Constructs a {@code RangeSlider} with the given label, min 0, and max
     * 100. The initial value is [0, 100].
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
     * Constructs a {@code RangeSlider} with the given label, min and max. The
     * initial value is set to [min, max].
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
    public RangeSlider(String label, double min, double max) {
        this(min, max);
        setLabel(label);
    }

    /**
     * Sets an accessible name for the start range input element of the slider.
     *
     * @param accessibleName
     *            the accessible name to set, or {@code null} to remove it
     */
    public void setAccessibleNameStart(String accessibleName) {
        getElement().setProperty("accessibleNameStart", accessibleName);
    }

    /**
     * Gets the accessible name for the start range input element of the slider.
     *
     * @return an optional accessible name, or an empty optional if no
     *         accessible name has been set
     */
    public Optional<String> getAccessibleNameStart() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameStart"));
    }

    /**
     * Sets an accessible name for the end range input element of the slider.
     *
     * @param accessibleName
     *            the accessible name to set, or {@code null} to remove it
     */
    public void setAccessibleNameEnd(String accessibleName) {
        getElement().setProperty("accessibleNameEnd", accessibleName);
    }

    /**
     * Gets the accessible name for the end range input element of the slider.
     *
     * @return an optional accessible name, or an empty optional if no
     *         accessible name has been set
     */
    public Optional<String> getAccessibleNameEnd() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameEnd"));
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
     *
     * @param min
     *            the minimum value
     */
    public void setMin(double min) {
        setMinDouble(min);
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
     *
     * @param max
     *            the maximum value
     */
    public void setMax(double max) {
        setMaxDouble(max);
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
     * Valid slider values are calculated relative to the minimum value:
     * {@code min}, {@code min + step}, {@code min + 2*step}, etc.
     *
     * @param step
     *            the step value
     */
    public void setStep(double step) {
        setStepDouble(step);
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
            RangeSliderValue value = PARSER.apply(arrayValue);
            return isValueWithinMinMax(value) && isValueAlignedWithStep(value);
        } catch (IllegalArgumentException | ClassCastException e) {
            return false;
        }
    }

    @Override
    boolean isValueWithinMinMax(RangeSliderValue value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        if (min > max) {
            return false;
        }

        return value.equals(new RangeSliderValue(
                SliderUtil.clampToMinMax(value.start(), min, max),
                SliderUtil.clampToMinMax(value.end(), min, max)));
    }

    @Override
    boolean isValueAlignedWithStep(RangeSliderValue value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        double step = getStepDouble();
        if (min > max) {
            return false;
        }

        return value.equals(new RangeSliderValue(
                SliderUtil.snapToStep(value.start(), min, max, step),
                SliderUtil.snapToStep(value.end(), min, max, step)));
    }
}
