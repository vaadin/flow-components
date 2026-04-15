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

import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JacksonUtils;

import tools.jackson.databind.node.ArrayNode;

/**
 * Base class for sliders that allow selecting a range of numeric values within
 * configured bounds.
 * 
 * @param <TComponent>
 *            the type of the slider component
 * @param <TValue>
 *            the type of the slider's value, which must extend {@link Range} of
 *            a numeric type
 * @param <TNumber>
 *            the numeric type of the slider's start value, end value, min, max,
 *            step properties, which must extend {@link Number}
 */
abstract class NumberRangeSlider<TComponent extends NumberRangeSlider<TComponent, TValue, TNumber>, TValue extends Range<TNumber>, TNumber extends Number>
        extends SliderBase<TComponent, TValue, TNumber, ArrayNode> {

    private final SerializableBiFunction<TNumber, TNumber, TValue> rangeFactory;

    /**
     * Constructs a NumberRangeSlider with the given min and max values, a
     * factory function for creating range values, and functions to convert
     * between the slider's numeric type and double.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param rangeFactory
     *            a function to create range values from start and end values
     * @param fromDouble
     *            a function to convert a double value to the slider's numeric
     *            type
     * @param toDouble
     *            a function to convert a value of the slider's numeric type to
     *            double
     */
    NumberRangeSlider(TNumber min, TNumber max,
            SerializableBiFunction<TNumber, TNumber, TValue> rangeFactory,
            SerializableFunction<Double, TNumber> fromDouble,
            SerializableFunction<TNumber, Double> toDouble) {
        super(min, max, ArrayNode.class,
                arrayValue -> rangeFactory.apply(
                        fromDouble.apply(arrayValue.get(0).asDouble()),
                        fromDouble.apply(arrayValue.get(1).asDouble())),
                value -> JacksonUtils
                        .listToJson(Arrays.asList(toDouble.apply(value.start()),
                                toDouble.apply(value.end()))),
                fromDouble, toDouble);
        this.rangeFactory = rangeFactory;

        setValue(rangeFactory.apply(min, max));
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
     * Clears the slider value, setting it to the full range from minimum to
     * maximum.
     *
     * @see #getMin()
     * @see #getMax()
     */
    @Override
    public void clear() {
        setValue(rangeFactory.apply(getMin(), getMax()));
    }

    @Override
    protected boolean isValueWithinMinMax(TValue value) {
        double min = toDouble.apply(getMin());
        double max = toDouble.apply(getMax());
        if (min > max) {
            return false;
        }

        double valueStart = toDouble.apply(value.start());
        double valueEnd = toDouble.apply(value.end());
        double clampedStart = SliderUtil.clampToMinMax(valueStart, min, max);
        double clampedEnd = SliderUtil.clampToMinMax(valueEnd, min, max);

        return Double.compare(valueStart, clampedStart) == 0
                && Double.compare(valueEnd, clampedEnd) == 0;
    }

    @Override
    protected boolean isValueAlignedWithStep(TValue value) {
        double min = toDouble.apply(getMin());
        double max = toDouble.apply(getMax());
        if (min > max) {
            return false;
        }

        double step = toDouble.apply(getStep());
        double valueStart = toDouble.apply(value.start());
        double valueEnd = toDouble.apply(value.end());
        double snappedStart = SliderUtil.snapToStep(valueStart, min, max, step);
        double snappedEnd = SliderUtil.snapToStep(valueEnd, min, max, step);

        return Double.compare(valueStart, snappedStart) == 0
                && Double.compare(valueEnd, snappedEnd) == 0;
    }
}
