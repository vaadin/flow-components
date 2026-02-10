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
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableFunction;

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
        HasValidationProperties, HasValueChangeMode, Focusable<TComponent>,
        KeyNotifier {

    private ValueChangeMode currentMode;

    private int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

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
            SerializableFunction<TPresentation, TValue> presentationToModel,
            SerializableFunction<TValue, TPresentation> modelToPresentation) {
        super("value", null, presentationType, presentationToModel,
                modelToPresentation);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);
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
     * Sets whether the value bubble is always visible, regardless of focus or
     * hover state. By default, bubble is hidden and only shown on interaction.
     *
     * @param valueAlwaysVisible
     *            {@code true} to always show the value bubble, {@code false}
     *            otherwise
     */
    public void setValueAlwaysVisible(boolean valueAlwaysVisible) {
        getElement().setProperty("valueAlwaysVisible", valueAlwaysVisible);
    }

    /**
     * Gets whether the value bubble is always visible, regardless of focus or
     * hover state. By default, bubble is hidden and only shown on interaction.
     *
     * @return {@code true} if the value bubble is always visible, {@code false}
     *         otherwise
     */
    public boolean isValueAlwaysVisible() {
        return getElement().getProperty("valueAlwaysVisible", false);
    }

    /**
     * Sets whether the min and max values are displayed below the slider track.
     * By default, min and max values are hidden.
     *
     * @param minMaxVisible
     *            {@code true} to display min and max values, {@code false}
     *            otherwise
     */
    public void setMinMaxVisible(boolean minMaxVisible) {
        getElement().setProperty("minMaxVisible", minMaxVisible);
    }

    /**
     * Gets whether the min and max values are displayed below the slider track.
     * By default, min and max values are hidden.
     *
     * @return {@code true} if the min and max values are displayed,
     *         {@code false} otherwise
     */
    public boolean isMinMaxVisible() {
        return getElement().getProperty("minMaxVisible", false);
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
        SliderUtil.requireValidStep(step);
        SliderUtil.requireValidMinMax(min, max);
        requireValidValue(min, max, step, value);

        setMinDouble(min);
        setMaxDouble(max);
        setStepDouble(step);
        super.setValue(value);
    }

    /**
     * Validates that the given value is valid for the current min, max and
     * step.
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

    /**
     * {@inheritDoc}
     * <p>
     * The default value is {@link ValueChangeMode#ON_CHANGE}.
     */
    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        setSynchronizedEvent(
                ValueChangeMode.eventForMode(valueChangeMode, "value-changed"));
        applyChangeTimeout();
    }

    @Override
    public void setValueChangeTimeout(int valueChangeTimeout) {
        this.valueChangeTimeout = valueChangeTimeout;
        applyChangeTimeout();
    }

    @Override
    public int getValueChangeTimeout() {
        return valueChangeTimeout;
    }

    private void applyChangeTimeout() {
        ValueChangeMode.applyChangeTimeout(getValueChangeMode(),
                getValueChangeTimeout(), getSynchronizationRegistration());
    }
}
