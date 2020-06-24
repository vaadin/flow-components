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
package com.vaadin.flow.component.datetimepicker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableFunction;

@Tag("vaadin-date-time-picker-date-picker")
class DateTimePickerDatePicker
        extends com.vaadin.flow.component.datepicker.DatePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }
}

@Tag("vaadin-date-time-picker-time-picker")
class DateTimePickerTimePicker
        extends com.vaadin.flow.component.timepicker.TimePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }
}

/**
 * Server-side component that encapsulates the functionality of the
 * {@code vaadin-date-time-picker} web component.
 *
 */
@Tag("vaadin-date-time-picker")
@NpmPackage(value = "@vaadin/vaadin-date-time-picker", version = "1.3.0-alpha3")
@JsModule("@vaadin/vaadin-date-time-picker/src/vaadin-date-time-picker.js")
public class DateTimePicker extends AbstractField<DateTimePicker, LocalDateTime>
        implements HasStyle, HasSize, HasTheme, HasValidation,
        Focusable<DateTimePicker> {

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
        super(null);
        if (initialDateTime != null) {
            setPresentationValue(initialDateTime);
            updateValue();
        }

        addToSlot(datePicker, "date-picker");
        addToSlot(timePicker, "time-picker");

        setLocale(UI.getCurrent().getLocale());

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        getElement().addEventListener("value-changed", e -> this.updateValue());
        addValueChangeListener(e -> validate());

        FieldValidationUtil.disableClientValidation(this);
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

    @Override
    public void setValue(LocalDateTime value) {
        super.setValue(value);
        // Get the value from client after possible adjustment.
        // This is a workaround for infinite value change loop which can be
        // triggered when the field is set as read-only.
        // fixme(haprog) This workaround should probably be removed after this
        // is fixed: https://github.com/vaadin/vaadin-time-picker-flow/issues/77
        getElement().executeJs("return this.value;")
                .then(jsonValue -> super.setValue(
                        jsonValue.asString().isEmpty() ? null :
                                LocalDateTime.parse(jsonValue.asString())));
    }

    @Override
    protected void setPresentationValue(LocalDateTime newPresentationValue) {
        datePicker.setValue(newPresentationValue != null
                ? newPresentationValue.toLocalDate()
                : null);
        timePicker.setValue(newPresentationValue != null
                ? newPresentationValue.toLocalTime()
                : null);
        // Make custom field detect and propagate the value change by triggering
        // "change" event on one of the fields.
        datePicker.getElement().executeJs(
                "this.dispatchEvent(new CustomEvent('change', { bubbles: true }));");
    }

    private void updateValue() {
        final LocalDate date = datePicker.getValue();
        final LocalTime time = timePicker.getValue();
        final LocalDateTime newValue = date != null && time != null
                ? LocalDateTime.of(date, time)
                : null;

        setModelValue(newValue, true);
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
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Gets the label of this field.
     *
     * @return the {@code label} property of the date time picker
     */
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
        timePicker.setStep(step);
    }

    /**
     * Gets the step of the time picker.
     *
     * @return the {@code step} property from the picker, unit seconds
     */
    public Duration getStep() {
        return timePicker.getStep();
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
        return locale;
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
        return getElement().getProperty("error-message");
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
}
