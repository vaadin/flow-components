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

import com.vaadin.experimental.FeatureFlags;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.function.SerializableBiFunction;

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
abstract class SliderBase<TComponent extends SliderBase<TComponent, TValue>, TValue>
        extends AbstractSinglePropertyField<TComponent, TValue> implements
        InputField<ComponentValueChangeEvent<TComponent, TValue>, TValue>,
        HasValidationProperties, Focusable<TComponent>, KeyNotifier {
    /**
     * Constructs a slider with the given min, max, step, initial value, and
     * custom converters for the value property.
     *
     * @param <TPresentation>
     *            the presentation type used by the element property
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @param value
     *            the initial value
     * @param presentationType
     *            the class of the presentation type
     * @param presentationToModel
     *            a function to convert from presentation to model
     * @param modelToPresentation
     *            a function to convert from model to presentation
     */
    <TPresentation> SliderBase(double min, double max, double step,
            TValue value, Class<TPresentation> presentationType,
            SerializableBiFunction<TComponent, TPresentation, TValue> presentationToModel,
            SerializableBiFunction<TComponent, TValue, TPresentation> modelToPresentation) {
        super("value", null, presentationType, presentationToModel,
                modelToPresentation);

        setSynchronizedEvent("change");
        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValue(value, min, max, step);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        checkFeatureFlag(attachEvent.getUI());
    }

    private void checkFeatureFlag(UI ui) {
        FeatureFlags featureFlags = FeatureFlags
                .get(ui.getSession().getService().getContext());
        boolean enabled = featureFlags
                .isEnabled(SliderFeatureFlagProvider.SLIDER_COMPONENT);

        if (!enabled) {
            throw new ExperimentalFeatureException();
        }
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    double getMinDouble() {
        return getElement().getProperty("min", 0.0);
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    double getMaxDouble() {
        return getElement().getProperty("max", 100.0);
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    double getStepDouble() {
        return getElement().getProperty("step", 1.0);
    }

    /**
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     */
    void setMinDouble(double min) {
        getElement().setProperty("min", min);
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     */
    void setMaxDouble(double max) {
        getElement().setProperty("max", max);
    }

    /**
     * Sets the step value of the slider.
     *
     * @param step
     *            the step value
     */
    void setStepDouble(double step) {
        getElement().setProperty("step", step);
    }

    /**
     * Sets the value of the slider.
     *
     * @param value
     *            the value
     * @throws IllegalArgumentException
     *             if value is not valid for the current min, max and step
     */
    @Override
    public void setValue(TValue value) {
        setValue(value, getMinDouble(), getMaxDouble(), getStepDouble());
    }

    /**
     * Sets the minimum, maximum, and value of the slider atomically.
     * <p>
     * The step remains unchanged.
     *
     * @param value
     *            the value
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @throws IllegalArgumentException
     *             if min is greater than max
     * @throws IllegalArgumentException
     *             if value is not valid for the given min, max and current step
     */
    public void setValue(TValue value, double min, double max) {
        setValue(value, min, max, getStepDouble());
    }

    /**
     * Sets the minimum, maximum, step, and value of the slider atomically.
     *
     * @param value
     *            the value
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if min is greater than max
     * @throws IllegalArgumentException
     *             if step is not positive
     * @throws IllegalArgumentException
     *             if value is not valid for the given min, max and step
     */
    public void setValue(TValue value, double min, double max, double step) {
        requireValidStep(step);
        requireValidMinMax(min, max);
        requireValidValue(min, max, step, value);

        setMinDouble(min);
        setMaxDouble(max);
        setStepDouble(step);
        super.setValue(value);
    }

    /**
     * Validates that the given min/max range is valid.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @throws IllegalArgumentException
     *             if min is greater than max
     */
    void requireValidMinMax(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "Max must be greater than or equal to min");
        }
    }

    /**
     * Validates that the given step value is valid.
     *
     * @param step
     *            the step value
     * @throws IllegalArgumentException
     *             if step is not positive
     */
    void requireValidStep(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be positive");
        }
    }

    /**
     * Validates that the given value is valid for the current min, max and step.
     *
     * @param value
     *            the value to validate
     * @throws IllegalArgumentException
     *             if value is not valid for the current min, max and step
     */
    void requireValidValue(TValue value) {
        requireValidValue(getMinDouble(), getMaxDouble(), getStepDouble(),
                value);
    }

    /**
     * Validates that the given value is valid for the given min, max and step.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @param value
     *            the value to validate
     * @throws IllegalArgumentException
     *             if value is not valid for the given min, max and step
     */
    abstract void requireValidValue(double min, double max, double step,
            TValue value);
}
