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
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.shared.HasValidationProperties;
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
public abstract class SliderBase<TComponent extends SliderBase<TComponent, TValue>, TValue>
        extends AbstractSinglePropertyField<TComponent, TValue>
        implements HasLabel, HasHelper, HasValidationProperties, HasSize,
        Focusable<TComponent>, KeyNotifier {
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

        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValue(min, max, step, value);
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
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     */
    void setMin(double min) {
        getElement().setProperty("min", min);
    }

    /**
     * Gets the minimum value of the slider.
     *
     * @return the minimum value
     */
    double getMin() {
        return getElement().getProperty("min", 0);
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     */
    void setMax(double max) {
        getElement().setProperty("max", max);
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    double getMax() {
        return getElement().getProperty("max", 100);
    }

    /**
     * Sets the step value of the slider. The step is the amount the value
     * changes when the user moves the handle.
     *
     * @param step
     *            the step value
     */
    void setStep(double step) {
        getElement().setProperty("step", step);
    }

    /**
     * Gets the step value of the slider.
     *
     * @return the step value
     */
    double getStep() {
        return getElement().getProperty("step", 1);
    }

    /**
     * Sets the value of the slider.
     *
     * @param value
     *            the value
     * @throws IllegalArgumentException
     *             if value is not valid for the current range and step
     */
    public void setValue(TValue value) {
        requireValidValue(value);
        super.setValue(value);
    }

    /**
     * Sets the minimum, maximum, and value of the slider atomically.
     * <p>
     * The step remains unchanged.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param value
     *            the value
     * @throws IllegalArgumentException
     *             if min is greater than max
     * @throws IllegalArgumentException
     *             if value is not valid for the given range and current step
     */
    void setValue(double min, double max, TValue value) {
        setValue(min, max, getStep(), value);
    }

    /**
     * Sets the minimum, maximum, step, and value of the slider atomically.
     *
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param step
     *            the step value
     * @param value
     *            the value
     * @throws IllegalArgumentException
     *             if min is greater than max
     * @throws IllegalArgumentException
     *             if step is not positive
     * @throws IllegalArgumentException
     *             if value is not valid for the given range and step
     */
    void setValue(double min, double max, double step, TValue value) {
        if (min > max) {
            throw new IllegalArgumentException(
                    "The min value cannot be greater than the max value");
        }

        if (step <= 0) {
            throw new IllegalArgumentException(
                    "The step value must be a positive number");
        }

        requireValidValue(min, max, step, value);

        setMin(min);
        setMax(max);
        setStep(step);
        super.setValue(value);
    }

    TValue requireValidValue(TValue value) {
        return requireValidValue(getMin(), getMax(), getStep(), value);
    }

    abstract TValue requireValidValue(double min, double max, double step,
            TValue value);
}
