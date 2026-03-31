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

import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.function.SerializableFunction;

abstract class NumberSlider<TComponent extends NumberSlider<TComponent, TValue>, TValue extends Number>
        extends SliderBase<TComponent, TValue, TValue, Double>
        implements HasAriaLabel {

    <TPresentation> NumberSlider(TValue min, TValue max,
            SerializableFunction<Double, TValue> fromDouble,
            SerializableFunction<TValue, Double> toDouble) {
        super(min, max, Double.class, fromDouble, toDouble, fromDouble,
                toDouble);
    }

    /**
     * Sets an accessible name for the range input element of the slider.
     *
     * @param ariaLabel
     *            the accessible name to set, or {@code null} to remove it
     */
    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    /**
     * Gets the accessible name for the range input element of the slider.
     *
     * @return an optional accessible name, or an empty optional if no
     *         accessible name has been set
     */
    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    /**
     * Sets the id of an element to be used as the accessible name for the range
     * input element of the slider.
     *
     * @param ariaLabelledBy
     *            the id of the element to be used as the label, or {@code null}
     *            to remove it
     */
    @Override
    public void setAriaLabelledBy(String ariaLabelledBy) {
        getElement().setProperty("accessibleNameRef", ariaLabelledBy);
    }

    /**
     * Gets the id of the element used as the accessible name for the range
     * input element of the slider.
     *
     * @return an optional id of the element used as the label, or an empty
     *         optional if no id has been set
     */
    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
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
    protected boolean hasValidValue() {
        TValue value = fromDouble.apply(getElement().getProperty("value", 0.0));
        return isValueWithinMinMax(value) && isValueAlignedWithStep(value);
    }

    @Override
    protected boolean isValueWithinMinMax(TValue value) {
        double min = toDouble.apply(getMin());
        double max = toDouble.apply(getMax());
        if (min > max) {
            return false;
        }

        double doubleValue = toDouble.apply(value);
        double clampedValue = SliderUtil.clampToMinMax(doubleValue, min, max);

        return Double.compare(doubleValue, clampedValue) == 0;
    }

    @Override
    protected boolean isValueAlignedWithStep(TValue value) {
        double min = toDouble.apply(getMin());
        double max = toDouble.apply(getMax());
        if (min > max) {
            return false;
        }

        double step = toDouble.apply(getStep());
        double doubleValue = toDouble.apply(value);
        double snappedValue = SliderUtil.snapToStep(doubleValue, min, max,
                step);

        return Double.compare(doubleValue, snappedValue) == 0;
    }
}
