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

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.shared.HasValidationProperties;

/**
 * Abstract base class for slider components.
 *
 * @param <TComponent>
 *            the component type
 * @param <TValue>
 *            the value type
 *
 * @author Vaadin Ltd
 */
public abstract class SliderBase<TComponent extends SliderBase<TComponent, TValue>, TValue>
        extends AbstractSinglePropertyField<TComponent, TValue>
        implements HasLabel, HasHelper, HasValidationProperties {

    /**
     * Constructs a slider with the given min, max, and initial value.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the initial value
     */
    public SliderBase(double min, double max, TValue value) {
        super("value", null, false);

        getElement().setProperty("manualValidation", true);

        setMin(min);
        setMax(max);
        setValue(value);

        // workaround for // https://github.com/vaadin/flow/issues/3496
        setInvalid(false);
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
        if (min > getMax()) {
            throw new IllegalArgumentException(
                    "The min value cannot be greater than the max value");
        }

        getElement().setProperty("min", min);
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    public double getMin() {
        return getElement().getProperty("min", 0.0);
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
        if (max < getMin()) {
            throw new IllegalArgumentException(
                    "The max value cannot be less than the min value");
        }

        getElement().setProperty("max", max);
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public double getMax() {
        return getElement().getProperty("max", 100.0);
    }

    /**
     * Sets the step value of the slider. The step is the amount the value
     * changes when the user moves the handle.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if the step is less than or equal to zero
     */
    public void setStep(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException(
                    "The step value must be a positive number");
        }

        getElement().setProperty("step", step);
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    public double getStep() {
        return getElement().getProperty("step", 1.0);
    }
}
