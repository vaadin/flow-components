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

abstract class NumberRangeSlider<TComponent extends NumberRangeSlider<TComponent, TRange, TValue>, TRange extends Range, TValue extends Number>
        extends SliderBase<TComponent, TRange, ArrayNode> {

    public NumberRangeSlider(double min, double max,
            SerializableFunction<ArrayNode, TRange> presentationToModel,
            SerializableFunction<TRange, ArrayNode> modelToPresentation) {
        super(min, max, ArrayNode.class, presentationToModel,
                modelToPresentation);
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
    public TValue getMin() {
        return fromDouble(super.getMinDouble());
    }

    /**
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     */
    public void setMin(TValue min) {
        setMinDouble(min.doubleValue());
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public TValue getMax() {
        return fromDouble(super.getMaxDouble());
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     */
    public void setMax(TValue max) {
        setMaxDouble(max.doubleValue());
    }

    /**
     * Gets the step value of the slider.
     * <p>
     * Valid slider values are calculated relative to the minimum value:
     * {@code min}, {@code min + step}, {@code min + 2*step}, etc.
     *
     * @return the step value
     */
    public TValue getStep() {
        return fromDouble(super.getStepDouble());
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
    public void setStep(TValue step) {
        setStepDouble(step.doubleValue());
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
            TRange value = presentationToModel.apply(arrayValue);
            return isValueWithinMinMax(value) && isValueAlignedWithStep(value);
        } catch (IllegalArgumentException | ClassCastException e) {
            return false;
        }
    }

    @Override
    protected boolean isValueWithinMinMax(TRange value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        if (min > max) {
            return false;
        }

        return value.equals(createRange(
                fromDouble(SliderUtil.clampToMinMax(value.start().doubleValue(),
                        min, max)),
                fromDouble(SliderUtil.clampToMinMax(value.end().doubleValue(),
                        min, max))));
    }

    @Override
    protected boolean isValueAlignedWithStep(TRange value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        double step = getStepDouble();
        if (min > max) {
            return false;
        }

        return value.equals(createRange(
                fromDouble(SliderUtil.snapToStep(value.start().doubleValue(),
                        min, max, step)),
                fromDouble(SliderUtil.snapToStep(value.end().doubleValue(), min,
                        max, step))));
    }

    protected abstract TRange createRange(TValue start, TValue end);

    protected abstract TValue fromDouble(Double value);
}
