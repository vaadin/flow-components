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
        extends SliderBase<TComponent, TValue, Double> implements HasAriaLabel {

    <TPresentation> NumberSlider(double min, double max,
            SerializableFunction<Double, TValue> fromDouble,
            SerializableFunction<TValue, Double> toDouble) {
        super(min, max, Double.class, fromDouble, toDouble);
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
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    public TValue getMin() {
        return presentationToModel.apply(super.getMinDouble());
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
        return presentationToModel.apply(super.getMaxDouble());
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
        return presentationToModel.apply(super.getStepDouble());
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
        TValue value = presentationToModel
                .apply(getElement().getProperty("value", 0.0));
        return isValueWithinMinMax(value) && isValueAlignedWithStep(value);
    }

    @Override
    protected boolean isValueWithinMinMax(TValue value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        if (min > max) {
            return false;
        }

        return value.equals(SliderUtil
                .clampToMinMax(modelToPresentation.apply(value), min, max));
    }

    @Override
    protected boolean isValueAlignedWithStep(TValue value) {
        double min = getMinDouble();
        double max = getMaxDouble();
        if (min > max) {
            return false;
        }

        return value.equals(SliderUtil.snapToStep(
                modelToPresentation.apply(value), min, max, getStepDouble()));
    }
}
