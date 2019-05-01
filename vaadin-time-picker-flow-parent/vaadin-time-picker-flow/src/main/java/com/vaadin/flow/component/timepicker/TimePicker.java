/*
 * Copyright 2000-2019 Vaadin Ltd.
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
package com.vaadin.flow.component.timepicker;

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.shared.Registration;

/**
 * An input component for selecting time of day, based on
 * {@code vaadin-time-picker} web component.
 *
 * @author Vaadin Ltd
 */
@JavaScript("frontend://timepickerConnector.js")
@JsModule("frontend://timepickerConnector.js")
public class TimePicker extends GeneratedVaadinTimePicker<TimePicker, LocalTime>
        implements HasSize, HasValidation, HasEnabled {

    private static final SerializableFunction<String, LocalTime> PARSER = valueFromClient -> {
        return valueFromClient == null || valueFromClient.isEmpty() ? null
                : LocalTime.parse(valueFromClient);
    };

    private static final SerializableFunction<LocalTime, String> FORMATTER = valueFromModel -> {
        return valueFromModel == null ? "" : valueFromModel.toString();
    };

    private static final long MILLISECONDS_IN_A_DAY = 86400000L;
    private static final long MILLISECONDS_IN_AN_HOUR = 3600000L;

    private Locale locale;

    /**
     * Default constructor.
     */
    public TimePicker() {
        this((LocalTime) null);
    }

    /**
     * Convenience constructor to create a time picker with a pre-selected time.
     *
     * @param time
     *            the pre-selected time in the picker
     */
    public TimePicker(LocalTime time) {
        super(time, null, String.class, PARSER, FORMATTER);
    }

    /**
     * Convenience constructor to create a time picker with a label.
     *
     * @param label
     *            the label describing the time picker
     * @see #setLabel(String)
     */
    public TimePicker(String label) {
        this();
        setLabel(label);
    }

    /**
     * Convenience constructor to create a time picker with a pre-selected time
     * and a label.
     *
     * @param label
     *            the label describing the time picker
     * @param time
     *            the pre-selected time in the picker
     */
    public TimePicker(String label, LocalTime time) {
        this(time);
        setLabel(label);
    }

    /**
     * Convenience constructor to create a time picker with a
     * {@link ValueChangeListener}.
     *
     * @param listener
     *            the listener to receive value change events
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public TimePicker(
            ValueChangeListener<ComponentValueChangeEvent<TimePicker, LocalTime>> listener) {
        this();
        addValueChangeListener(listener);
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    // This is needed because the LocalTime format is not the same depending on
    // the platform.
    @Override
    public void setValue(LocalTime value) {
        if (value == null) {
            super.setValue(null);
        } else {
            LocalTime truncatedValue = value.truncatedTo(ChronoUnit.MILLIS);
            super.setValue(truncatedValue);
        }
    }

    /**
     * Gets the label of the time picker.
     *
     * @return the {@code label} property of the time picker
     */
    public String getLabel() {
        return getLabelString();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * Gets the current error message from the time picker.
     *
     * @return the current error message
     */
    @Override
    public String getErrorMessage() {
        return getErrorMessageString();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    /**
     * Gets the validity of the time picker output.
     * <p>
     * return true, if the value is invalid.
     *
     * @return the {@code validity} property from the time picker
     */
    @Override
    public boolean isInvalid() {
        return isInvalidBoolean();
    }

    @Override
    public void setPlaceholder(String placeholder) {
        super.setPlaceholder(placeholder);
    }

    /**
     * Gets the placeholder of the time picker.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code placeholder} property of the time picker
     */
    public String getPlaceholder() {
        return getPlaceholderString();
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
    }

    /**
     * Determines whether the time picker is marked as input required.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return {@code true} if the input is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return isRequiredBoolean();
    }

    /**
     * Sets the {@code step} property of the time picker using duration. It
     * specifies the intervals for the displayed items in the time picker
     * dropdown and also the displayed time format.
     * <p>
     * The set step needs to evenly divide a day or an hour and has to be larger
     * than 0 milliseconds. By default, the format is {@code hh:mm} (same as *
     * {@code Duration.ofHours(1)}
     * <p>
     * If the step is less than 60 seconds, the format will be changed to
     * {@code hh:mm:ss} and it can be in {@code hh:mm:ss.fff} format, when the
     * step is less than 1 second.
     * <p>
     * <em>NOTE:</em> If the step is less than 900 seconds, the dropdown is
     * hidden.
     * <p>
     * <em>NOTE: changing the step to a larger duration can cause a new
     * {@link com.vaadin.flow.component.HasValue.ValueChangeEvent} to be fired
     * if some parts (eg. seconds) is discarded from the value.</em>
     *
     * @param step
     *            the step to set, not {@code null} and should divide a day or
     *            an hour evenly
     */
    public void setStep(Duration step) {
        Objects.requireNonNull(step, "Step cannot be null");
        long stepAsMilliseconds = step.getSeconds() * 1000
                + (long) (step.getNano() / 1E6);
        if (step.isNegative() || stepAsMilliseconds == 0) {
            throw new IllegalArgumentException(
                    "Step cannot be negative and must be larger than 0 milliseconds");
        }

        if (MILLISECONDS_IN_A_DAY % stepAsMilliseconds != 0
                && MILLISECONDS_IN_AN_HOUR % stepAsMilliseconds != 0) {
            throw new IllegalArgumentException("Given step " + step.toString()
                    + " does not divide evenly a day or an hour.");
        }

        super.setStep(step.getSeconds() + (step.getNano() / 1E9));
    }

    /**
     * Gets the step of the time picker.
     *
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     *
     * @return the {@code step} property from the picker, unit seconds
     */
    public Duration getStep() {
        // the web component doesn't have a default value defined, but it is an
        // hour, not 0.0 like in the generated class
        if (!getElement().hasProperty("step")) {
            return Duration.ofHours(1);
        }
        return Duration.ofNanos((long) (getStepDouble() * 1E9));
    }

    @Override
    public Registration addInvalidChangeListener(
            ComponentEventListener<InvalidChangeEvent<TimePicker>> listener) {
        return super.addInvalidChangeListener(listener);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (getLocale() == null) {
            setLocale(attachEvent.getUI().getLocale());
        }
        initConnector();
    }

    private void initConnector() {
        // can't run this with getElement().executeJavaScript(...) since then
        // setLocale might be called before this causing client side error
        runBeforeClientResponse(ui -> ui.getPage().executeJavaScript(
                "window.Vaadin.Flow.timepickerConnector.initLazy($0)",
                getElement()));
    }

    /**
     * Set the Locale for the Time Picker. The displayed time will be formatted
     * by the browser using the given locale.
     * <p>
     * By default, the locale is {@code null} until the component is attached to
     * an UI, and then locale is set to {@link UI#getLocale()}, unless a locale
     * has been explicitly set before that.
     * <p>
     * The time formatting is done in the browser using the <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleTimeString">Date.toLocaleTimeString()</a>
     * function.
     * <p>
     * If for some reason the browser doesn't support the given locale, the
     * en-US locale is used.
     * <p>
     * <em>NOTE: only the language + country/region codes are used</em>. This
     * means that the script and variant information is not used and supported.
     * <em>NOTE: timezone related data is not supported.</em> <em>NOTE: changing
     * the locale does not cause a new
     * {@link com.vaadin.flow.component.HasValue.ValueChangeEvent} to be
     * fired.</em>
     *
     * @param locale
     *            the locale set to the time picker, cannot be [@code null}
     */
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null.");
        if (locale.getLanguage().isEmpty()) {
            throw new UnsupportedOperationException("Given Locale "
                    + locale.getDisplayName()
                    + " is not supported by time picker because it is missing the language information.");
        }

        this.locale = locale;
        // we could support script & variant, but that requires more work on
        // client side to detect the different
        // number characters for other scripts (current only Arabic there)
        StringBuilder bcp47LanguageTag = new StringBuilder(
                locale.getLanguage());
        if (!locale.getCountry().isEmpty()) {
            bcp47LanguageTag.append("-").append(locale.getCountry());
        }
        runBeforeClientResponse(ui -> getElement().callFunction(
                "$connector.setLocale", bcp47LanguageTag.toString()));
    }

    /**
     * Gets the Locale for this time picker.
     * <p>
     * By default, the locale is {@code null} until the component is attached to
     * an UI, and then locale is set to {@link UI#getLocale()}, unless
     * {@link #setLocale(Locale)} has been explicitly called before that.
     *
     * @return the locale used for this time picker
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the minimum time in the time picker. Times before that will be
     * disabled in the popup.
     *
     * @param min
     *            the minimum time that is allowed to be selected, or
     *            <code>null</code> to remove any minimum constraints
     */
    @Override
    public void setMin(String min) {
        super.setMin(min);
    }

    /**
     * Gets the minimum time in the time picker. Time before that will be
     * disabled in the popup.
     *
     * @return the minimum time that is allowed to be selected, or
     *         <code>null</code> if there's no minimum
     */
    public String getMin() {
        return super.getMinString();
    }

    /**
     * Sets the maximum time in the time picker. Times after that will be
     * disabled in the popup.
     *
     * @param max
     *            the maximum time that is allowed to be selected, or
     *            <code>null</code> to remove any maximum constraints
     */
    @Override
    public void setMax(String max) {
        super.setMax(max);
    }

    /**
     * Gets the maximum time in the time picker. Times after that will be
     * disabled in the popup.
     *
     * @return the maximum time that is allowed to be selected, or
     *         <code>null</code> if there's no maximum
     */
    public String getMax() {
        return super.getMaxString();
    }

    /**
     * Sets displaying a clear button in the time picker when it has value.
     * <p>
     * The clear button is an icon, which can be clicked to set the time picker
     * value to {@code null}.
     *
     * @param clearButtonVisible
     *            {@code true} to display the clear button, {@code false} to
     *            hide it
     */
    @Override
    public void setClearButtonVisible(boolean clearButtonVisible) {
        super.setClearButtonVisible(clearButtonVisible);
    }

    /**
     * Gets whether this time picker displays a clear button when it has value.
     *
     * @return {@code true} if this time picker displays a clear button,
     *         {@code false} otherwise
     * @see #setClearButtonVisible(boolean)
     */
    public boolean isClearButtonVisible() {
        return super.isClearButtonVisibleBoolean();
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * Returns a stream of all the available locales that are supported by the
     * time picker component.
     * <p>
     * This is a shorthand for {@link Locale#getAvailableLocales()} where all
     * locales without the {@link Locale#getLanguage()} have been filtered out,
     * as the browser cannot localize the time for those.
     *
     * @return a stream of the available locales that are supported by the time
     *         picker component
     * @see #setLocale(Locale)
     * @see Locale#getAvailableLocales()
     * @see Locale#getLanguage()
     */
    public static Stream<Locale> getSupportedAvailableLocales() {
        return Stream.of(Locale.getAvailableLocales())
                .filter(locale -> !locale.getLanguage().isEmpty());
    }
}
