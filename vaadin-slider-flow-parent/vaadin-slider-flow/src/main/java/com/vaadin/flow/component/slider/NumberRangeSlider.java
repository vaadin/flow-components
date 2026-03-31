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

import java.util.Optional;

import com.vaadin.flow.function.SerializableFunction;

import tools.jackson.databind.node.ArrayNode;

abstract class NumberRangeSlider<TComponent extends NumberRangeSlider<TComponent, TValue, TNumber>, TValue extends Range<TNumber>, TNumber extends Number>
        extends SliderBase<TComponent, TValue, TNumber, ArrayNode> {

    public NumberRangeSlider(TNumber min, TNumber max,
            SerializableFunction<ArrayNode, TValue> presentationToModel,
            SerializableFunction<TValue, ArrayNode> modelToPresentation,
            SerializableFunction<Double, TNumber> fromDouble,
            SerializableFunction<TNumber, Double> toDouble) {
        super(min, max, ArrayNode.class, presentationToModel,
                modelToPresentation, fromDouble, toDouble);
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
        setValue(createRange(getMin(), getMax()));
    }

    @Override
    protected boolean hasValidValue() {
        try {
            ArrayNode arrayValue = (ArrayNode) getElement()
                    .getPropertyRaw("value");
            TValue value = presentationToModel.apply(arrayValue);
            return isValueWithinMinMax(value) && isValueAlignedWithStep(value);
        } catch (IllegalArgumentException | ClassCastException e) {
            return false;
        }
    }

    @Override
    protected boolean isValueWithinMinMax(TValue value) {
        double min = toDouble.apply(getMin());
        double max = toDouble.apply(getMax());
        if (min > max) {
            return false;
        }

        double clampedStart = SliderUtil
                .clampToMinMax(toDouble.apply(value.start()), min, max);
        double clampedEnd = SliderUtil
                .clampToMinMax(toDouble.apply(value.end()), min, max);
        TValue clampedRange = createRange(fromDouble.apply(clampedStart),
                fromDouble.apply(clampedEnd));

        return value.equals(clampedRange);
    }

    @Override
    protected boolean isValueAlignedWithStep(TValue value) {
        double min = toDouble.apply(getMin());
        double max = toDouble.apply(getMax());
        double step = toDouble.apply(getStep());
        if (min > max) {
            return false;
        }

        double snappedStart = SliderUtil
                .snapToStep(toDouble.apply(value.start()), min, max, step);
        double snappedEnd = SliderUtil.snapToStep(toDouble.apply(value.end()),
                min, max, step);
        TValue snappedRange = createRange(fromDouble.apply(snappedStart),
                fromDouble.apply(snappedEnd));

        return value.equals(snappedRange);
    }

    protected abstract TValue createRange(TNumber start, TNumber end);
}
