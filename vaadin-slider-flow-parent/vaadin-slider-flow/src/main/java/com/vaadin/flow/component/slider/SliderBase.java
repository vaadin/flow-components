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

import java.util.Objects;

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
import com.vaadin.flow.dom.SignalBinding;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.signals.Signal;

/**
 * Abstract base class for slider components.
 *
 * @param <TComponent>
 *            the component type
 * @param <TValue>
 *            the model value type
 * @param <TNumber>
 *            the number type used for min, max, and step properties
 * @param <TPresentation>
 *            the presentation type used by the element property
 *
 * @author Vaadin Ltd
 */
abstract class SliderBase<TComponent extends SliderBase<TComponent, TValue, TNumber, TPresentation>, TValue, TNumber extends Number, TPresentation>
        extends AbstractSinglePropertyField<TComponent, TValue> implements
        InputField<ComponentValueChangeEvent<TComponent, TValue>, TValue>,
        HasValidationProperties, HasValueChangeMode, Focusable<TComponent>,
        KeyNotifier {

    static final double DEFAULT_MIN = 0;
    static final double DEFAULT_MAX = 100;
    static final double DEFAULT_STEP = 1;

    final SerializableFunction<TPresentation, TValue> presentationToModel;
    final SerializableFunction<TValue, TPresentation> modelToPresentation;
    final SerializableFunction<Double, TNumber> fromDouble;
    final SerializableFunction<TNumber, Double> toDouble;

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
     *            the class of the presentation type used by the element
     *            property
     * @param presentationToModel
     *            a function to convert a client-side presentation value to the
     *            slider's value type
     * @param modelToPresentation
     *            a function to convert a value of the slider's value type to
     *            client-side presentation
     * @param fromDouble
     *            a function to convert from double to the slider's number type
     * @param toDouble
     *            a function to convert from the slider's number type to double
     */
    SliderBase(TNumber min, TNumber max, Class<TPresentation> presentationType,
            SerializableFunction<TPresentation, TValue> presentationToModel,
            SerializableFunction<TValue, TPresentation> modelToPresentation,
            SerializableFunction<Double, TNumber> fromDouble,
            SerializableFunction<TNumber, Double> toDouble) {
        super("value", null, presentationType, presentationToModel,
                modelToPresentation);
        this.presentationToModel = presentationToModel;
        this.modelToPresentation = modelToPresentation;
        this.fromDouble = fromDouble;
        this.toDouble = toDouble;

        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setMin(min);
        setMax(max);
        setStep(fromDouble.apply(DEFAULT_STEP));
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
    public TNumber getMin() {
        return fromDouble.apply(getElement().getProperty("min", DEFAULT_MIN));
    }

    /**
     * Sets the minimum value of the slider.
     *
     * @param min
     *            the minimum value
     */
    public void setMin(TNumber min) {
        Objects.requireNonNull(min, "Min value cannot be null");
        getElement().setProperty("min", toDouble.apply(min));
        schedulePropertyConsistencyCheck();
    }

    /**
     * Gets the maximum value of the slider.
     *
     * @return the maximum value
     */
    public TNumber getMax() {
        return fromDouble.apply(getElement().getProperty("max", DEFAULT_MAX));
    }

    /**
     * Sets the maximum value of the slider.
     *
     * @param max
     *            the maximum value
     */
    public void setMax(TNumber max) {
        Objects.requireNonNull(max, "Max value cannot be null");
        getElement().setProperty("max", toDouble.apply(max));
        schedulePropertyConsistencyCheck();
    }

    /**
     * Gets the step value of the slider.
     * <p>
     * Valid slider values are calculated relative to the minimum value:
     * {@code min}, {@code min + step}, {@code min + 2*step}, etc.
     *
     * @return the step value
     */
    public TNumber getStep() {
        return fromDouble.apply(getElement().getProperty("step", DEFAULT_STEP));
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
    public void setStep(TNumber step) {
        Objects.requireNonNull(step, "Step value cannot be null");
        double stepDouble = toDouble.apply(step);
        if (stepDouble <= 0) {
            throw new IllegalArgumentException(
                    "The step must be greater than 0.");
        }
        getElement().setProperty("step", stepDouble);
        schedulePropertyConsistencyCheck();
    }

    /**
     * Binds the given signal to the minimum value of the slider as a one-way
     * binding so that the property is updated when the signal's value is
     * updated.
     * <p>
     * The minimum value is set immediately with the current signal value when
     * the binding is created, and is kept synchronized with any subsequent
     * signal value changes while the component is in attached state. When the
     * component is in detached state, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the minimum value manually
     * through the setter throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the minimum value to, not {@code null}
     * @return a {@link SignalBinding} that can be used to register
     *         {@link SignalBinding#onChange(com.vaadin.flow.function.SerializableConsumer)
     *         onChange} callbacks
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal,
     *      SerializableConsumer)
     * @since 25.1
     */
    public SignalBinding<TNumber> bindMin(Signal<TNumber> signal) {
        return getElement().bindProperty("min", signal, null);
    }

    /**
     * Binds the given signal to the maximum value of the slider as a one-way
     * binding so that the property is updated when the signal's value is
     * updated.
     * <p>
     * The maximum value is set immediately with the current signal value when
     * the binding is created, and is kept synchronized with any subsequent
     * signal value changes while the component is in attached state. When the
     * component is in detached state, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the maximum value manually
     * through the setter throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the maximum value to, not {@code null}
     * @return a {@link SignalBinding} that can be used to register
     *         {@link SignalBinding#onChange(com.vaadin.flow.function.SerializableConsumer)
     *         onChange} callbacks
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal,
     *      SerializableConsumer)
     * @since 25.1
     */
    public SignalBinding<TNumber> bindMax(Signal<TNumber> signal) {
        return getElement().bindProperty("max", signal, null);
    }

    /**
     * Binds the given signal to the step value of the slider as a one-way
     * binding so that the property is updated when the signal's value is
     * updated.
     * <p>
     * The step value is set immediately with the current signal value when the
     * binding is created, and is kept synchronized with any subsequent signal
     * value changes while the component is in attached state. When the
     * component is in detached state, signal value changes have no effect.
     * <p>
     * While a signal is bound, any attempt to set the step value manually
     * through the setter throws a
     * {@link com.vaadin.flow.signals.BindingActiveException}.
     *
     * @param signal
     *            the signal to bind the step value to, not {@code null}
     * @return a {@link SignalBinding} that can be used to register
     *         {@link SignalBinding#onChange(com.vaadin.flow.function.SerializableConsumer)
     *         onChange} callbacks
     * @see com.vaadin.flow.dom.Element#bindProperty(String, Signal,
     *      SerializableConsumer)
     * @since 25.1
     */
    public SignalBinding<TNumber> bindStep(Signal<TNumber> signal) {
        return getElement().bindProperty("step", signal, null);
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
        Objects.requireNonNull(value, "Value cannot be null");
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
        double min = toDouble.apply(getMin());
        double max = toDouble.apply(getMax());
        double step = toDouble.apply(getStep());
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

    @Override
    protected boolean hasValidValue() {
        try {
            TPresentation arrayValue = (TPresentation) getElement()
                    .getPropertyRaw("value");
            TValue value = presentationToModel.apply(arrayValue);
            return isValueWithinMinMax(value) && isValueAlignedWithStep(value);
        } catch (IllegalArgumentException | ClassCastException e) {
            return false;
        }
    }

    abstract protected boolean isValueAlignedWithStep(TValue value);

    abstract protected boolean isValueWithinMinMax(TValue value);
}
