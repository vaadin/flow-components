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
package com.vaadin.flow.component.datetimepicker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.timepicker.StepsUtil;
import com.vaadin.flow.function.SerializableFunction;

@Tag("vaadin-date-time-picker-date-picker")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
class DateTimePickerDatePicker
        extends com.vaadin.flow.component.datepicker.DatePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }

    void passThroughPresentationValue(LocalDate newPresentationValue) {
        super.setPresentationValue(newPresentationValue);
    }
}

@Tag("vaadin-date-time-picker-time-picker")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
class DateTimePickerTimePicker
        extends com.vaadin.flow.component.timepicker.TimePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }

    void passThroughPresentationValue(LocalTime newPresentationValue) {
        super.setPresentationValue(newPresentationValue);
    }
}

/**
 * Date Time Picker is an input field for selecting both a date and a time. The
 * date and time can be entered directly using a keyboard in the format of the
 * current locale or through the Date Time Picker’s two overlays. The overlays
 * open when their respective fields are clicked or any input is entered when
 * the fields are focused.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-date-time-picker")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/date-time-picker", version = "23.1.0-beta1")
@NpmPackage(value = "@vaadin/vaadin-date-time-picker", version = "23.1.0-beta1")
@JsModule("@vaadin/date-time-picker/src/vaadin-date-time-picker.js")
public class DateTimePicker
        extends AbstractSinglePropertyField<DateTimePicker, LocalDateTime>
        implements HasStyle, HasSize, HasTheme, HasValidation,
        Focusable<DateTimePicker>, HasHelper, HasLabel {

    private static final String PROP_AUTO_OPEN_DISABLED = "autoOpenDisabled";

    private final DateTimePickerDatePicker datePicker = new DateTimePickerDatePicker();
    private final DateTimePickerTimePicker timePicker = new DateTimePickerTimePicker();
    private DatePickerI18n i18n;
    private Locale locale;

    private final static SerializableFunction<String, LocalDateTime> PARSER = s -> {
        return s == null || s.isEmpty() ? null : LocalDateTime.parse(s);
    };

    private final static SerializableFunction<LocalDateTime, String> FORMATTER = d -> {
        return d == null ? "" : d.truncatedTo(ChronoUnit.MILLIS).toString();
    };

    private LocalDateTime max;
    private LocalDateTime min;
    private boolean required;

    /**
     * Default constructor.
     */
    public DateTimePicker() {
        this((LocalDateTime) null);
    }

    /**
     * Convenience constructor to create a date time picker with a label.
     *
     * @param label
     *            the label describing the date time picker
     * @see #setLabel(String)
     */
    public DateTimePicker(String label) {
        this();
        setLabel(label);
    }

    /**
     * Convenience constructor to create a date time picker with a pre-selected
     * date and time in current UI locale format and a label.
     *
     * @param label
     *            the label describing the date time picker
     * @param initialDateTime
     *            the pre-selected date time in the picker
     * @see #setValue(LocalDateTime)
     * @see #setLabel(String)
     */
    public DateTimePicker(String label, LocalDateTime initialDateTime) {
        this(initialDateTime);
        setLabel(label);
    }

    /**
     * Convenience constructor to create a date time picker with a pre-selected
     * date time in current UI locale format.
     *
     * @param initialDateTime
     *            the pre-selected date time in the picker
     */
    public DateTimePicker(LocalDateTime initialDateTime) {
        super("value", null, String.class, PARSER, FORMATTER);
        if (initialDateTime != null) {
            initialDateTime = sanitizeValue(initialDateTime);
            setPresentationValue(initialDateTime);
            synchronizeChildComponentValues(initialDateTime);
        } else if (this.getElement().getProperty("value") == null) {
            // Apply `null` as a value to force the client side `value` property
            // to be initialized with an empty string. Having an empty string
            // will prevent `ValueChangeEvent` which otherwise can be triggered
            // in response to Polymer converting `null` to an empty string by
            // itself.
            // Only apply `null` if the element does not already have a value,
            // which can be the case when binding to an existing element from a
            // Lit template.
            setPresentationValue(null);
        }

        addToSlot(datePicker, "date-picker");
        addToSlot(timePicker, "time-picker");

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        addValueChangeListener(e -> validate());
    }

    /**
     * Convenience constructor to create a date time picker with a
     * {@link ValueChangeListener}.
     *
     * @param listener
     *            the listener to receive value change events
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DateTimePicker(
            ValueChangeListener<ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date time picker with a
     * {@link ValueChangeListener} and a label.
     *
     *
     * @param label
     *            the label describing the date time picker
     * @param listener
     *            the listener to receive value change events
     * @see #setLabel(String)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DateTimePicker(String label,
            ValueChangeListener<ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date time picker with a pre-selected
     * date time in current UI locale format and a {@link ValueChangeListener}.
     *
     * @param initialDateTime
     *            the pre-selected date time in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setValue(LocalDateTime)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DateTimePicker(LocalDateTime initialDateTime,
            ValueChangeListener<ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listener) {
        this(initialDateTime);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date time picker with a pre-selected
     * date and time in current UI locale format, a {@link ValueChangeListener}
     * and a label.
     *
     * @param label
     *            the label describing the date time picker
     * @param initialDateTime
     *            the pre-selected date time in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setLabel(String)
     * @see #setValue(LocalDateTime)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DateTimePicker(String label, LocalDateTime initialDateTime,
            ValueChangeListener<ComponentValueChangeEvent<DateTimePicker, LocalDateTime>> listener) {
        this(initialDateTime);
        setLabel(label);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date time picker with pre-selected
     * date time and locale setup.
     *
     * @param initialDateTime
     *            the pre-selected date time in the picker
     * @param locale
     *            the locale for the date time picker
     */
    public DateTimePicker(LocalDateTime initialDateTime, Locale locale) {
        this(initialDateTime);
        setLocale(locale);
    }

    /**
     * Sets the selected date and time value of the component. The value can be
     * cleared by setting null.
     *
     * <p>
     * The value will be truncated to millisecond precision, as that is the
     * maximum that the time picker supports. This means that
     * {@link #getValue()} might return a different value than what was passed
     * in.
     *
     * @param value
     *            the LocalDateTime instance representing the selected date and
     *            time, or null
     */
    @Override
    public void setValue(LocalDateTime value) {
        value = sanitizeValue(value);
        super.setValue(value);
        synchronizeChildComponentValues(value);
    }

    /**
     * Sanitizes a LocalDateTime instance for to be used as internal value.
     *
     * <p>
     * Truncates value to millisecond precision, as that is the maximum that the
     * time picker supports. This is also necessary to synchronize with the
     * internal value of the Flow TimePicker, which truncates the value as well.
     *
     * @param value
     *            the LocalDateTime instance to sanitize, can be null
     * @return sanitized LocalDateTime instance
     */
    private LocalDateTime sanitizeValue(LocalDateTime value) {
        if (value == null)
            return null;

        return value.truncatedTo(ChronoUnit.MILLIS);
    }

    private void synchronizeChildComponentValues(LocalDateTime value) {
        if (value != null) {
            datePicker.passThroughPresentationValue(value.toLocalDate());
            timePicker.passThroughPresentationValue(value.toLocalTime());
        } else {
            datePicker.passThroughPresentationValue(null);
            timePicker.passThroughPresentationValue(null);
        }
    }

    /**
     * Adds the given Component to the specified slot of this component.
     */
    private void addToSlot(Component component, String slot) {
        Objects.requireNonNull(component, "Component to add cannot be null");
        component.getElement().setAttribute("slot", slot);
        getElement().appendChild(component.getElement());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        // fixme(haprog) This override can probably be removed after we use a
        // version which includes this fix:
        // https://github.com/vaadin/vaadin-date-time-picker/pull/30
        datePicker.setReadOnly(readOnly);
        timePicker.setReadOnly(readOnly);
    }

    /**
     * Sets the label for this field.
     *
     * @param label
     *            the String value to set
     */
    @Override
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Gets the label of this field.
     *
     * @return the {@code label} property of the date time picker
     */
    @Override
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Sets a placeholder string for the date field.
     *
     * @param placeholder
     *            the String value to set
     */
    public void setDatePlaceholder(String placeholder) {
        datePicker.setPlaceholder(placeholder);
    }

    /**
     * Gets the placeholder string of the date field.
     *
     * @return the {@code placeholder} property of the date picker
     */
    public String getDatePlaceholder() {
        return datePicker.getPlaceholder();
    }

    /**
     * Set a placeholder string for the time field.
     *
     * @param placeholder
     *            the String value to set
     */
    public void setTimePlaceholder(String placeholder) {
        timePicker.setPlaceholder(placeholder);
    }

    /**
     * Gets the placeholder string of the time field.
     *
     * @return the {@code placeholder} property of the time picker
     */
    public String getTimePlaceholder() {
        return timePicker.getPlaceholder();
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

        getElement().setProperty("step",
                StepsUtil.convertDurationToStepsValue(step));
        timePicker.setStep(step);
    }

    /**
     * Gets the step of the time picker.
     *
     * @return the {@code step} property from the picker, unit seconds
     */
    public Duration getStep() {
        // if step was not set by the user, then assume default value of the
        // time picker web component
        if (!getElement().hasProperty("step")) {
            return StepsUtil.DEFAULT_WEB_COMPONENT_STEP;
        }

        double stepsValue = getElement().getProperty("step", 0.0);

        return StepsUtil.convertStepsValueToDuration(stepsValue);
    }

    /**
     * Show or hide the week numbers in the date picker. By default the week
     * numbers are not shown.
     * <p>
     * Set true to display ISO-8601 week numbers in the calendar.
     * <p>
     * Note that displaying of week numbers is only supported when
     * i18n.firstDayOfWeek is 1 (Monday).
     *
     * @param weekNumbersVisible
     *            the boolean value to set
     * @see #setDatePickerI18n(DatePickerI18n)
     * @see DatePickerI18n#setFirstDayOfWeek(int)
     */
    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        datePicker.setWeekNumbersVisible(weekNumbersVisible);
    }

    /**
     * Get the state of {@code showWeekNumbers} property of the date picker.
     *
     * @return the {@code showWeekNumbers} property from the date picker
     */
    public boolean isWeekNumbersVisible() {
        return datePicker.isWeekNumbersVisible();
    }

    /**
     * Set the Locale for the DateTimePicker. The displayed date and time will
     * be matched to the format used in that locale.
     *
     * @param locale
     *            the locale to set to the DateTimePicker, cannot be null
     */
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null.");
        this.locale = locale;
        datePicker.setLocale(locale);
        timePicker.setLocale(locale);
    }

    /**
     * Gets the Locale for this DateTimePicker
     *
     * @return the locale used for this DateTimePicker
     */
    @Override
    public Locale getLocale() {
        if (locale != null) {
            return locale;
        } else {
            return super.getLocale();
        }
    }

    /**
     * Synchronizes the theme attribute to the internal DatePicker and
     * TimePicker components.
     */
    private void synchronizeTheme() {
        String theme = getThemeName();
        theme = theme != null ? theme : "";
        datePicker.getElement().setAttribute("theme", theme);
        timePicker.getElement().setAttribute("theme", theme);
    }

    /**
     * Adds a theme name to this component.
     *
     * @param themeName
     *            the theme name to add, not <code>null</code>
     */
    @Override
    public void addThemeName(String themeName) {
        HasTheme.super.addThemeName(themeName);
        synchronizeTheme();
    }

    /**
     * Removes a theme name from this component.
     *
     * @param themeName
     *            the theme name to remove, not <code>null</code>
     * @return <code>true</code> if the theme name was removed,
     *         <code>false</code> if the theme list didn't contain the theme
     *         name
     */
    @Override
    public boolean removeThemeName(String themeName) {
        boolean result = HasTheme.super.removeThemeName(themeName);
        synchronizeTheme();
        return result;
    }

    /**
     * Sets the theme names of this component. This method overwrites any
     * previous set theme names.
     *
     * @param themeName
     *            a space-separated string of theme names to set, or empty
     *            string to remove all theme names
     */
    @Override
    public void setThemeName(String themeName) {
        HasTheme.super.setThemeName(themeName);
        synchronizeTheme();
    }

    /**
     * Sets or removes the given theme name for this component.
     *
     * @param themeName
     *            the theme name to set or remove, not <code>null</code>
     * @param set
     *            <code>true</code> to set the theme name, <code>false</code> to
     *            remove it
     */
    @Override
    public void setThemeName(String themeName, boolean set) {
        HasTheme.super.setThemeName(themeName, set);
        synchronizeTheme();
    }

    /**
     * Adds one or more theme names to this component. Multiple theme names can
     * be specified by using multiple parameters.
     *
     * @param themeNames
     *            the theme name or theme names to be added to the component
     */
    @Override
    public void addThemeNames(String... themeNames) {
        HasTheme.super.addThemeNames(themeNames);
        synchronizeTheme();
    }

    /**
     * Removes one or more theme names from component. Multiple theme names can
     * be specified by using multiple parameters.
     *
     * @param themeNames
     *            the theme name or theme names to be removed from the component
     */
    @Override
    public void removeThemeNames(String... themeNames) {
        HasTheme.super.removeThemeNames(themeNames);
        synchronizeTheme();
    }

    /**
     * Sets the error message to display when the input is invalid.
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * Gets the error message to display when the input is invalid.
     *
     * @return the current error message
     */
    @Override
    public String getErrorMessage() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * Sets the validity indication of the date time picker output.
     */
    @Override
    public void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    /**
     * Gets the validity indication of the date time picker output.
     *
     * @return the current validity indication.
     */
    @Override
    public boolean isInvalid() {
        return getElement().getProperty("invalid", false);
    }

    /**
     * Gets the validity of the date time picker value.
     *
     * @return the current validity of the value.
     */
    private boolean isInvalid(LocalDateTime value) {
        final boolean isRequiredButEmpty = required
                && Objects.equals(getEmptyValue(), value);
        final boolean isGreaterThanMax = value != null && max != null
                && value.isAfter(max);
        final boolean isSmallerThanMin = value != null && min != null
                && value.isBefore(min);
        return isRequiredButEmpty || isGreaterThanMax || isSmallerThanMin;
    }

    /**
     * Performs server-side validation of the current value. This is needed
     * because it is possible to circumvent the client-side validation
     * constraints using browser development tools.
     */
    protected void validate() {
        setInvalid(isInvalid(getValue()));
    }

    /**
     * Sets the minimum date and time in the date time picker. Dates and times
     * before that will be disabled in the popups.
     *
     * @param min
     *            the minimum date and time that is allowed to be set, or
     *            <code>null</code> to remove any minimum constraints
     */
    public void setMin(LocalDateTime min) {
        getElement().setProperty("min", FORMATTER.apply(min));
        this.min = min;
    }

    /**
     * Gets the minimum date and time in the date time picker. Dates and times
     * before that will be disabled in the popups.
     *
     * @return the minimum date and time that is allowed to be set, or
     *         <code>null</code> if there's no minimum
     */
    public LocalDateTime getMin() {
        return PARSER.apply(getElement().getProperty("min"));
    }

    /**
     * Sets the maximum date and time in the date time picker. Dates and times
     * above that will be disabled in the popups.
     *
     * @param max
     *            the maximum date and time that is allowed to be set, or
     *            <code>null</code> to remove any minimum constraints
     */
    public void setMax(LocalDateTime max) {
        getElement().setProperty("max", FORMATTER.apply(max));
        this.max = max;
    }

    /**
     * Gets the maximum date and time in the date time picker. Dates and times
     * above that will be disabled in the popups.
     *
     * @return the maximum date and time that is allowed to be set, or
     *         <code>null</code> if there's no minimum
     */
    public LocalDateTime getMax() {
        return PARSER.apply(getElement().getProperty("max"));
    }

    /**
     * Gets the internationalization object previously set for this component.
     *
     * Note: updating the object content that is gotten from this method will
     * not update the lang on the component if not set back using
     * {@link DateTimePicker#setDatePickerI18n(DatePickerI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, If the i18n
     *         properties weren't set.
     */
    public DatePickerI18n getDatePickerI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for the date picker inside this
     * component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setDatePickerI18n(DatePickerI18n i18n) {
        Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        this.i18n = i18n;
        datePicker.setI18n(i18n);
    }

    /**
     * Sets whether the date time picker is marked as input required.
     *
     * @param requiredIndicatorVisible
     *            the value of the requiredIndicatorVisible to be set
     */
    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        this.required = requiredIndicatorVisible;
    }

    /**
     * When auto open is enabled, the dropdown will open when the field is
     * clicked.
     *
     * @param autoOpen
     *            Value for the auto open property,
     */
    public void setAutoOpen(boolean autoOpen) {
        getElement().setProperty(PROP_AUTO_OPEN_DISABLED, !autoOpen);
        datePicker.setAutoOpen(autoOpen);
        timePicker.setAutoOpen(autoOpen);
    }

    /**
     * When auto open is enabled, the dropdown will open when the field is
     * clicked.
     *
     * @return {@code true} if auto open is enabled. {@code false} otherwise.
     *         Default is {@code true}
     */
    public boolean isAutoOpen() {
        return !getElement().getProperty(PROP_AUTO_OPEN_DISABLED, false);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        FieldValidationUtil.disableClientValidation(this);

    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(DateTimePickerVariant... variants) {
        getThemeNames().addAll(
                Stream.of(variants).map(DateTimePickerVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(DateTimePickerVariant... variants) {
        getThemeNames().removeAll(
                Stream.of(variants).map(DateTimePickerVariant::getVariantName)
                        .collect(Collectors.toList()));
    }
}
