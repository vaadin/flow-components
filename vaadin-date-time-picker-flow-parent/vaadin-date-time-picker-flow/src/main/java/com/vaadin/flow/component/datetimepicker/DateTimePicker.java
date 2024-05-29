/*
 * Copyright 2000-2024 Vaadin Ltd.
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

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasAutoOpen;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.timepicker.StepsUtil;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

@Tag("vaadin-date-picker")
class DateTimePickerDatePicker
        extends com.vaadin.flow.component.datepicker.DatePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }

    void passThroughPresentationValue(LocalDate newPresentationValue) {
        super.setPresentationValue(newPresentationValue);

        if (valueEquals(newPresentationValue, getEmptyValue())
                && isInputValuePresent()) {
            // Clear the input element from possible bad input.
            getElement().executeJs("this.inputElement.value = ''");
            getElement().setProperty("_hasInputValue", false);
        }
    }

    @Override
    protected boolean isInputValuePresent() {
        return super.isInputValuePresent();
    }
}

@Tag("vaadin-time-picker")
class DateTimePickerTimePicker
        extends com.vaadin.flow.component.timepicker.TimePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }

    void passThroughPresentationValue(LocalTime newPresentationValue) {
        super.setPresentationValue(newPresentationValue);

        if (valueEquals(newPresentationValue, getEmptyValue())
                && isInputValuePresent()) {
            // Clear the input element from possible bad input.
            getElement().executeJs("this.inputElement.value = ''");
            getElement().setProperty("_hasInputValue", false);
        }
    }

    @Override
    protected boolean isInputValuePresent() {
        return super.isInputValuePresent();
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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/date-time-picker", version = "24.5.0-alpha1")
@JsModule("@vaadin/date-time-picker/src/vaadin-date-time-picker.js")
public class DateTimePicker
        extends AbstractSinglePropertyField<DateTimePicker, LocalDateTime>
        implements Focusable<DateTimePicker>, HasAutoOpen, HasClientValidation,
        InputField<AbstractField.ComponentValueChangeEvent<DateTimePicker, LocalDateTime>, LocalDateTime>,
        HasOverlayClassName, HasThemeVariant<DateTimePickerVariant>,
        HasValidationProperties, HasValidator<LocalDateTime> {

    private final DateTimePickerDatePicker datePicker = new DateTimePickerDatePicker();
    private final DateTimePickerTimePicker timePicker = new DateTimePickerTimePicker();
    private DatePickerI18n datePickerI18n;

    private DateTimePickerI18n i18n;
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

    private boolean manualValidationEnabled = false;

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

        SlotUtils.addToSlot(this, "date-picker", datePicker);
        SlotUtils.addToSlot(this, "time-picker", timePicker);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        addValueChangeListener(e -> validate());

        addClientValidatedEventListener(e -> validate());
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
        LocalDateTime oldValue = getValue();

        value = sanitizeValue(value);
        super.setValue(value);

        boolean isInputValuePresent = timePicker.isInputValuePresent()
                || datePicker.isInputValuePresent();
        boolean isValueRemainedEmpty = valueEquals(oldValue, getEmptyValue())
                && valueEquals(value, getEmptyValue());
        if (isValueRemainedEmpty && isInputValuePresent) {
            // Clear the input elements from possible bad input.
            synchronizeChildComponentValues(value);
            fireEvent(new ClientValidatedEvent(this, false));
        } else {
            synchronizeChildComponentValues(value);
        }

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
     * Sets the aria-label for the component.
     *
     * @param ariaLabel
     *            the value to set as aria-label
     */
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    /**
     * Gets the aria-label of the component.
     *
     * @return an optional aria-label or an empty optional if no aria-label has
     *         been set
     */
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    /**
     * Contains DateTimePicker internalization properties
     */
    public static class DateTimePickerI18n implements Serializable {
        private String dateLabel;
        private String timeLabel;

        public String getDateLabel() {
            return dateLabel;
        }

        public void setDateLabel(String dateLabel) {
            this.dateLabel = dateLabel;
        }

        public String getTimeLabel() {
            return timeLabel;
        }

        public void setTimeLabel(String timeLabel) {
            this.timeLabel = timeLabel;
        }
    }

    /**
     * Sets the accessible label for the date picker.
     * <p>
     * The final value is a concatenation of the accessible label from
     * DateTimePicker's {@link #getAriaLabel()} or {@link #getLabel()} and the
     * given accessible label.
     *
     * @param dateLabel
     *            the value to be used as part of date picker aria-label.
     */
    public void setDateAriaLabel(String dateLabel) {
        if (i18n == null) {
            i18n = new DateTimePickerI18n();
        }
        i18n.setDateLabel(dateLabel);
        getElement().setPropertyJson("i18n", JsonSerializer.toJson(i18n));
    }

    /**
     * Gets the accessible label of the date picker.
     * <p>
     * Note that this method will return the last value passed to
     * {@link #setDateAriaLabel(String)}, not the value currently set on the
     * `aria-label` attribute of the date picker input element.
     *
     * @return an optional label or an empty optional if no label has been set
     */
    public Optional<String> getDateAriaLabel() {
        if (i18n == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(i18n.getDateLabel());
    }

    /**
     * Sets the accessible label for the time picker.
     * <p>
     * The final value is a concatenation of the accessible label from
     * DateTimePicker's {@link #getAriaLabel()} or {@link #getLabel()} and the
     * given accessible label.
     *
     * @param timeLabel
     *            the value to be used as part of time picker aria-label.
     */
    public void setTimeAriaLabel(String timeLabel) {
        if (i18n == null) {
            i18n = new DateTimePickerI18n();
        }
        i18n.setTimeLabel(timeLabel);
        getElement().setPropertyJson("i18n", JsonSerializer.toJson(i18n));
    }

    /**
     * Gets the accessible label of the time picker.
     * <p>
     * Note that this method will return the last value passed to
     * {@link #setTimeAriaLabel(String)}, not the value currently set on the
     * `aria-label` attribute of the time picker input element.
     *
     * @return an optional label or an empty optional if no label has been set
     */
    public Optional<String> getTimeAriaLabel() {
        if (i18n == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(i18n.getTimeLabel());
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
     * datePickerI18n.firstDayOfWeek is 1 (Monday).
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
        HasThemeVariant.super.addThemeName(themeName);
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
        boolean result = HasThemeVariant.super.removeThemeName(themeName);
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
        HasThemeVariant.super.setThemeName(themeName);
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
        HasThemeVariant.super.setThemeName(themeName, set);
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
        HasThemeVariant.super.addThemeNames(themeNames);
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
        HasThemeVariant.super.removeThemeNames(themeNames);
        synchronizeTheme();
    }

    @Override
    public Validator<LocalDateTime> getDefaultValidator() {
        return (value, context) -> checkValidity(value);
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<LocalDateTime> listener) {
        return addClientValidatedEventListener(event -> listener
                .validationStatusChanged(new ValidationStatusChangeEvent<>(this,
                        event.isValid())));
    }

    private ValidationResult checkValidity(LocalDateTime value) {
        boolean hasNonParsableDatePickerValue = datePicker
                .getValue() == datePicker.getEmptyValue()
                && datePicker.isInputValuePresent();

        boolean hasNonParsableTimePickerValue = timePicker
                .getValue() == timePicker.getEmptyValue()
                && timePicker.isInputValuePresent();

        if (hasNonParsableDatePickerValue || hasNonParsableTimePickerValue) {
            return ValidationResult.error("");
        }

        ValidationResult greaterThanMax = ValidationUtil
                .checkGreaterThanMax(value, max);
        if (greaterThanMax.isError()) {
            return greaterThanMax;
        }

        ValidationResult smallerThanMin = ValidationUtil
                .checkSmallerThanMin(value, min);
        if (smallerThanMin.isError()) {
            return smallerThanMin;
        }

        return ValidationResult.ok();
    }

    /**
     * Gets the validity of the date time picker value.
     *
     * @return the current validity of the value.
     */
    private boolean isInvalid(LocalDateTime value) {
        var requiredValidation = ValidationUtil.checkRequired(required, value,
                getEmptyValue());

        return requiredValidation.isError() || checkValidity(value).isError();
    }

    @Override
    public void setManualValidation(boolean enabled) {
        this.manualValidationEnabled = enabled;
    }

    /**
     * Performs server-side validation of the current value. This is needed
     * because it is possible to circumvent the client-side validation
     * constraints using browser development tools.
     */
    protected void validate() {
        if (!this.manualValidationEnabled) {
            setInvalid(isInvalid(getValue()));
        }
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
        return datePickerI18n;
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
        this.datePickerI18n = i18n;
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
        getElement().setProperty("autoOpenDisabled", !autoOpen);
        datePicker.setAutoOpen(autoOpen);
        timePicker.setAutoOpen(autoOpen);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }
}
