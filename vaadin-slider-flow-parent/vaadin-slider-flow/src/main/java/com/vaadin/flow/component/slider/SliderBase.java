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

import org.slf4j.LoggerFactory;

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
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.signals.Signal;

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

    private static final double DEFAULT_STEP = 1.0;

    private ValueChangeMode currentMode;

    private int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

    private boolean consistencyCheckPending = false;

    /**
     * Constructs a slider with the given min, max, and custom converters for
     * the value property.
     *
     * @param <TPresentation>
     *            the presentation type used by the element property
     * @param min
     *            the minimum value
     * @param max
     *            the maximum value
     * @param presentationType
     *            the class of the presentation type
     * @param presentationToModel
     *            a function to convert from presentation to model
     * @param modelToPresentation
     *            a function to convert from model to presentation
     */
    protected <TPresentation> SliderBase(double min, double max,
            Class<TPresentation> presentationType,
            SerializableFunction<TPresentation, TValue> presentationToModel,
            SerializableFunction<TValue, TPresentation> modelToPresentation) {
        super("value", null, presentationType, presentationToModel,
                modelToPresentation);

        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setMinDouble(min);
        setMaxDouble(max);
        setStepDouble(DEFAULT_STEP);
        clear();

        setValueChangeMode(ValueChangeMode.ON_CHANGE);
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
        schedulePropertyConsistencyCheck();
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     */
    void setMaxDouble(double max) {
        getElement().setProperty("max", max);
        schedulePropertyConsistencyCheck();
    }

    /**
     * Sets the step value of the slider.
     *
     * @param step
     *            the step value
     */
    void setStepDouble(double step) {
        if (step <= 0) {
            throw new IllegalArgumentException(
                    "The step must be greater than 0.");
        }
        getElement().setProperty("step", step);
        schedulePropertyConsistencyCheck();
    }

    /**
     * Binds the given signal to the minimum value of the slider as a one-way
     * binding so that the property is updated when the signal's value is
     * updated.
     * <p>
     * When a signal is bound, the minimum value is kept synchronized with the
     * signal value while the component is attached. When the component is
     * detached, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the minimum value manually
     * through the setter throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the minimum value to, not {@code null}
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal,
     *      SerializableConsumer)
     * @since 25.1
     */
    public void bindMin(Signal<Double> signal) {
        getElement().bindProperty("min", signal, null);
    }

    /**
     * Binds the given signal to the maximum value of the slider as a one-way
     * binding so that the property is updated when the signal's value is
     * updated.
     * <p>
     * When a signal is bound, the maximum value is kept synchronized with the
     * signal value while the component is attached. When the component is
     * detached, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the maximum value manually
     * through the setter throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the maximum value to, not {@code null}
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal,
     *      SerializableConsumer)
     * @since 25.1
     */
    public void bindMax(Signal<Double> signal) {
        getElement().bindProperty("max", signal, null);
    }

    /**
     * Binds the given signal to the step value of the slider as a one-way
     * binding so that the property is updated when the signal's value is
     * updated.
     * <p>
     * When a signal is bound, the step value is kept synchronized with the
     * signal value while the component is attached. When the component is
     * detached, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the step value manually
     * through the setter throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the step value to, not {@code null}
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal,
     *      SerializableConsumer)
     * @since 25.1
     */
    public void bindStep(Signal<Double> signal) {
        getElement().bindProperty("step", signal, null);
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

    @Override
    public void setValue(TValue value) {
        super.setValue(value);
        schedulePropertyConsistencyCheck();
    }

    private void schedulePropertyConsistencyCheck() {
        if (consistencyCheckPending) {
            return;
        }

        consistencyCheckPending = true;
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> {
                    consistencyCheckPending = false;
                    warnIfPropertiesInconsistent();
                }));
    }

    private void warnIfPropertiesInconsistent() {
        double min = getMinDouble();
        double max = getMaxDouble();
        double step = getStepDouble();
        TValue value = getValue();

        if (min > max) {
            LoggerFactory.getLogger(getClass()).warn(
                    """
                            Invalid configuration: min ({}) is greater than max ({}). \
                            Swap min and max, or adjust them so that min <= max to avoid a broken UI state.
                            """,
                    min, max);
        }

        if (min <= max && !isValueWithinMinMax(value)) {
            LoggerFactory.getLogger(getClass()).warn(
                    """
                            Invalid configuration: value ({}) is outside the configured range (min={}, max={}). \
                            Set only values between min and max to avoid an inconsistent UI state.
                            """,
                    value, min, max);
        }

        if (min <= max && !isValueAlignedWithStep(value)) {
            LoggerFactory.getLogger(getClass()).warn(
                    """
                            Invalid configuration: value ({}) is not aligned with step (min={}, max={}, step={}). \
                            Set only values of the form (min + N * step) to avoid an inconsistent UI state.
                            """,
                    value, min, max, step);
        }
    }

    abstract protected boolean isValueAlignedWithStep(TValue value);

    abstract protected boolean isValueWithinMinMax(TValue value);
}
