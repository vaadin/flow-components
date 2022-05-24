/*
 * Copyright 2000-2022 Vaadin Ltd.
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasClearButton;
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;

/**
 * Time Picker is an input field for entering or selecting a specific time. The
 * time can be entered directly using a keyboard or by choosing a value from a
 * set of predefined options presented in an overlay. The overlay opens when the
 * field is clicked or any input is entered when the field is focused.
 *
 * @author Vaadin Ltd
 */
@JsModule("./vaadin-time-picker/timepickerConnector.js")
public class TimePicker extends GeneratedVaadinTimePicker<TimePicker, LocalTime>
        implements HasSize, HasValidation, HasEnabled, HasHelper, HasLabel,
        HasTheme, HasClearButton {

    private static final SerializableFunction<String, LocalTime> PARSER = valueFromClient -> {
        return valueFromClient == null || valueFromClient.isEmpty() ? null
                : LocalTime.parse(valueFromClient);
    };

    private static final SerializableFunction<LocalTime, String> FORMATTER = valueFromModel -> {
        return valueFromModel == null ? "" : valueFromModel.toString();
    };

    private static final String PROP_AUTO_OPEN_DISABLED = "autoOpenDisabled";

    private Locale locale;

    private LocalTime max;
    private LocalTime min;
    private boolean required;
    private StateTree.ExecutionRegistration pendingLocaleUpdate;

    /**
     * Default constructor.
     */
    public TimePicker() {
        this((LocalTime) null, true);
    }

    /**
     * Convenience constructor to create a time picker with a pre-selected time.
     *
     * @param time
     *            the pre-selected time in the picker
     */
    public TimePicker(LocalTime time) {
        this(time, false);
    }

    /**
     * Convenience constructor to create a time picker with a pre-selected time.
     *
     * @param time
     *            the pre-selected time in the picker
     * @param isInitialValueOptional
     *            If {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     */
    private TimePicker(LocalTime time, boolean isInitialValueOptional) {
        super(time, null, String.class, PARSER, FORMATTER,
                isInitialValueOptional);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        addValueChangeListener(e -> validate());
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

    /**
     * Convenience constructor to create a time picker with a pre-selected time
     * and {@link ValueChangeListener}.
     *
     * @param time
     *            the pre-selected time in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public TimePicker(LocalTime time,
            ValueChangeListener<ComponentValueChangeEvent<TimePicker, LocalTime>> listener) {
        this(time);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a time picker with a label, a
     * pre-selected time and a {@link ValueChangeListener}.
     *
     * @param label
     *            the label describing the time picker
     * @param time
     *            the pre-selected time in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setLabel(String)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public TimePicker(String label, LocalTime time,
            ValueChangeListener<ComponentValueChangeEvent<TimePicker, LocalTime>> listener) {
        this(time);
        setLabel(label);
        addValueChangeListener(listener);
    }

    /**
     * Sets the label for the time picker.
     *
     * @param label
     *            value for the {@code label} property in the time picker
     */
    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * Sets the selected time value of the component. The value can be cleared
     * by setting null.
     *
     * <p>
     * The value will be truncated to millisecond precision, as that is the
     * maximum that the time picker supports. This means that
     * {@link #getValue()} might return a different value than what was passed
     * in.
     *
     * @param value
     *            the LocalTime instance representing the selected time, or null
     */
    @Override
    public void setValue(LocalTime value) {
        // Truncate the value to millisecond precision, as the is the maximum
        // that the time picker web component supports.
        if (value != null) {
            value = value.truncatedTo(ChronoUnit.MILLIS);
        }
        super.setValue(value);
    }

    /**
     * Gets the label of the time picker.
     *
     * @return the {@code label} property of the time picker
     */
    @Override
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

    /**
     * Performs a server-side validation of the given value. This is needed
     * because it is possible to circumvent the client side validation
     * constraints using browser development tools.
     */
    private boolean isInvalid(LocalTime value) {
        final boolean isRequiredButEmpty = required
                && Objects.equals(getEmptyValue(), value);
        final boolean isGreaterThanMax = value != null && max != null
                && value.isAfter(max);
        final boolean isSmallerThenMin = value != null && min != null
                && value.isBefore(min);
        return isRequiredButEmpty || isGreaterThanMax || isSmallerThenMin;
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
        this.required = required;
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        this.required = requiredIndicatorVisible;
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

        super.setStep(StepsUtil.convertDurationToStepsValue(step));
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
        // if step was not set by the user, then assume default value of the
        // time picker web component
        if (!getElement().hasProperty("step")) {
            return StepsUtil.DEFAULT_WEB_COMPONENT_STEP;
        }
        return StepsUtil.convertStepsValueToDuration(getStepDouble());
    }

    @Override
    public Registration addInvalidChangeListener(
            ComponentEventListener<InvalidChangeEvent<TimePicker>> listener) {
        return super.addInvalidChangeListener(listener);
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(TimePickerVariant... variants) {
        getThemeNames().addAll(
                Stream.of(variants).map(TimePickerVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(TimePickerVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(TimePickerVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Performs server-side validation of the current value. This is needed
     * because it is possible to circumvent the client-side validation
     * constraints using browser development tools.
     */
    @Override
    protected void validate() {
        setInvalid(isInvalid(getValue()));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
        requestLocaleUpdate();
        FieldValidationUtil.disableClientValidation(this);
    }

    private void initConnector() {
        // can't run this with getElement().executeJavaScript(...) since then
        // setLocale might be called before this causing client side error
        runBeforeClientResponse(ui -> ui.getPage().executeJs(
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
        requestLocaleUpdate();
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
        if (locale != null) {
            return locale;
        } else {
            return super.getLocale();
        }
    }

    private void requestLocaleUpdate() {
        getUI().ifPresent(ui -> {
            if (pendingLocaleUpdate != null) {
                pendingLocaleUpdate.remove();
            }
            pendingLocaleUpdate = ui.beforeClientResponse(this, context -> {
                pendingLocaleUpdate = null;
                executeLocaleUpdate();
            });
        });
    }

    private void executeLocaleUpdate() {
        Locale appliedLocale = getLocale();
        // we could support script & variant, but that requires more work on
        // client side to detect the different
        // number characters for other scripts (current only Arabic there)
        StringBuilder bcp47LanguageTag = new StringBuilder(
                appliedLocale.getLanguage());
        if (!appliedLocale.getCountry().isEmpty()) {
            bcp47LanguageTag.append("-").append(appliedLocale.getCountry());
        }
        runBeforeClientResponse(ui -> getElement().callJsFunction(
                "$connector.setLocale", bcp47LanguageTag.toString()));
    }

    /**
     * Sets the minimum time in the time picker. Times before that will be
     * disabled in the popup.
     *
     * @param min
     *            the minimum time that is allowed to be selected, or
     *            <code>null</code> to remove any minimum constraints
     */
    public void setMin(LocalTime min) {
        this.min = min;
        super.setMin(format(min));
    }

    /**
     * Sets the minimum time in the time picker. Times before that will be
     * disabled in the popup.
     *
     * @deprecated Since 22.0, this API is deprecated in favor of
     *             {@link TimePicker#setMin(LocalTime)}
     * @param min
     *            the minimum time that is allowed to be selected, or
     *            <code>null</code> to remove any minimum constraints
     */
    @Deprecated
    public void setMinTime(LocalTime min) {
        this.setMin(min);
    }

    /**
     * Gets the minimum time in the time picker. Time before that will be
     * disabled in the popup.
     *
     * @return the minimum time that is allowed to be selected, or
     *         <code>null</code> if there's no minimum
     */
    public LocalTime getMin() {
        return this.min;
    }

    /**
     * Gets the minimum time in the time picker. Time before that will be
     * disabled in the popup.
     *
     * @deprecated Since 22.0, this API is deprecated in favor of
     *             {@link TimePicker#getMin()}
     *
     * @return the minimum time that is allowed to be selected, or
     *         <code>null</code> if there's no minimum
     */
    @Deprecated
    public LocalTime getMinTime() {
        return this.getMin();
    }

    /**
     * Sets the maximum time in the time picker. Times after that will be
     * disabled in the popup.
     *
     * @param max
     *            the maximum time that is allowed to be selected, or
     *            <code>null</code> to remove any maximum constraints
     */
    public void setMax(LocalTime max) {
        this.max = max;
        super.setMax(format(max));
    }

    /**
     * Sets the maximum time in the time picker. Times after that will be
     * disabled in the popup.
     *
     * @deprecated Since 22.0, this API is deprecated in favor of
     *             {@link TimePicker#setMax(LocalTime)}
     *
     * @param max
     *            the maximum time that is allowed to be selected, or
     *            <code>null</code> to remove any maximum constraints
     */
    @Deprecated
    public void setMaxTime(LocalTime max) {
        this.setMax(max);
    }

    /**
     * Gets the maximum time in the time picker. Times after that will be
     * disabled in the popup.
     *
     * @return the maximum time that is allowed to be selected, or
     *         <code>null</code> if there's no maximum
     */
    public LocalTime getMax() {
        return this.max;
    }

    /**
     * Gets the maximum time in the time picker. Times after that will be
     * disabled in the popup.
     *
     * @deprecated Since 22.0, this API is deprecated in favor of
     *             {@link TimePicker#getMax()}
     *
     * @return the maximum time that is allowed to be selected, or
     *         <code>null</code> if there's no maximum
     */
    @Deprecated
    public LocalTime getMaxTime() {
        return this.getMax();
    }

    private void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    /**
     * Enables or disables the dropdown opening automatically. If {@code false}
     * the dropdown is only opened when clicking the toggle button or pressing
     * Up or Down arrow keys.
     *
     * @param autoOpen
     *            {@code false} to prevent the dropdown from opening
     *            automatically
     */
    public void setAutoOpen(boolean autoOpen) {
        getElement().setProperty(PROP_AUTO_OPEN_DISABLED, !autoOpen);
    }

    /**
     * Gets whether dropdown will open automatically or not.
     *
     * @return @{code true} if enabled, {@code false} otherwise
     */
    public boolean isAutoOpen() {
        return !getElement().getProperty(PROP_AUTO_OPEN_DISABLED, false);
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

    private static String format(LocalTime time) {
        return time != null ? time.toString() : null;
    }

}
