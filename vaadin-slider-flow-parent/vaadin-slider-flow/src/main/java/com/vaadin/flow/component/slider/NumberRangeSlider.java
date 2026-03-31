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

    protected abstract TValue createRange(TNumber start, TNumber end);
}
