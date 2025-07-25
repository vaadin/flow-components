/*
 * Copyright 2000-2025 Vaadin Ltd.
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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasAutoOpen;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.component.timepicker.StepsUtil;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;

@Tag("vaadin-date-picker")
class DateTimePickerDatePicker
        extends com.vaadin.flow.component.datepicker.DatePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }

    boolean isPickerInputUnparsable() {
        return super.isInputUnparsable();
    }
}

@Tag("vaadin-time-picker")
class DateTimePickerTimePicker
        extends com.vaadin.flow.component.timepicker.TimePicker {
    @Override
    protected void validate() {
        // Should not change invalid state
    }

    boolean isPickerInputUnparsable() {
        return super.isInputUnparsable();
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
@NpmPackage(value = "@vaadin/date-time-picker", version = "25.0.0-alpha8")
@JsModule("@vaadin/date-time-picker/src/vaadin-date-time-picker.js")
public class DateTimePicker
        extends AbstractSinglePropertyField<DateTimePicker, LocalDateTime>
        implements Focusable<DateTimePicker>, HasAutoOpen,
        InputField<AbstractField.ComponentValueChangeEvent<DateTimePicker, LocalDateTime>, LocalDateTime>,
        HasOverlayClassName, HasThemeVariant<DateTimePickerVariant>,
        HasValidationProperties, HasValidator<LocalDateTime> {

    private final DateTimePickerDatePicker datePicker = new DateTimePickerDatePicker();
    private final DateTimePickerTimePicker timePicker = new DateTimePickerTimePicker();
    private DatePickerI18n datePickerI18n;

    private DateTimePickerI18n i18n;
    private Locale locale;

    private String dateAriaLabel;
    private String timeAriaLabel;

    private final static SerializableFunction<String, LocalDateTime> PARSER = s -> {
        return s == null || s.isEmpty() ? null : LocalDateTime.parse(s);
    };

    private final static SerializableFunction<LocalDateTime, String> FORMATTER = d -> {
        return d == null ? "" : d.truncatedTo(ChronoUnit.MILLIS).toString();
    };

    private LocalDateTime max;
    private LocalDateTime min;

    private final CopyOnWriteArrayList<ValidationStatusChangeListener<LocalDateTime>> validationStatusChangeListeners = new CopyOnWriteArrayList<>();

    private final Validator<LocalDateTime> defaultValidator = (value,
            context) -> {
        var fromComponent = context == null;

        // Report error if any of the pickers has bad input
        if (isInputUnparsable()) {
            return ValidationResult.error(getI18nErrorMessage(
                    DateTimePickerI18n::getBadInputErrorMessage));
        }

        // Report error if only date picker has a value, and it's outside the
        // range.
        if (Objects.equals(value, getEmptyValue()) && !datePicker.isEmpty()) {
            var maxDate = max != null ? max.toLocalDate() : null;
            var minDate = min != null ? min.toLocalDate() : null;

            var maxResult = ValidationUtil.validateMaxConstraint(
                    getI18nErrorMessage(DateTimePickerI18n::getMaxErrorMessage),
                    datePicker.getValue(), maxDate);
            if (maxResult.isError()) {
                return maxResult;
            }

            var minResult = ValidationUtil.validateMinConstraint(
                    getI18nErrorMessage(DateTimePickerI18n::getMinErrorMessage),
                    datePicker.getValue(), minDate);
            if (minResult.isError()) {
                return minResult;
            }
        }

        // Report error if only one of the pickers has a value
        if (isInputIncomplete()) {
            return ValidationResult.error(getI18nErrorMessage(
                    DateTimePickerI18n::getIncompleteInputErrorMessage));
        }

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(getI18nErrorMessage(
                            DateTimePickerI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        var maxResult = ValidationUtil.validateMaxConstraint(
                getI18nErrorMessage(DateTimePickerI18n::getMaxErrorMessage),
                value, max);
        if (maxResult.isError()) {
            return maxResult;
        }

        var minResult = ValidationUtil.validateMinConstraint(
                getI18nErrorMessage(DateTimePickerI18n::getMinErrorMessage),
                value, min);
        if (minResult.isError()) {
            return minResult;
        }

        return ValidationResult.ok();
    };

    private ValidationController<DateTimePicker, LocalDateTime> validationController = new ValidationController<>(
            this);

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

        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        addValidationListeners();
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

    private void addValidationListeners() {
        addValueChangeListener(e -> validate());
        getElement().addEventListener("unparsable-change", e -> validate(true));
        // Add listeners to invalidate a required DateTimePicker when:
        // 1. It's initially empty
        // 2. The user selects a value in one picker (date or time)
        // 3. The user then removes that value
        // 4. The user leaves the field without proceeding to the other picker
        datePicker.addValueChangeListener(event -> handlePickerValueChange());
        timePicker.addValueChangeListener(event -> handlePickerValueChange());
    }

    private void handlePickerValueChange() {
        // Only handle picker value change if required.
        if (!isRequiredIndicatorVisible()) {
            return;
        }
        // If the component is already invalid, any picker value change will
        // trigger the listener for either unparsable-change event or value
        // change event. Therefore, the component will be validated and there is
        // no need to validate here.
        if (isInvalid()) {
            return;
        }
        if (isEmpty() && timePicker.isEmpty() && datePicker.isEmpty()
                && !isInputUnparsable()) {
            validate(true);
        }
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
        var oldValue = getValue();
        value = sanitizeValue(value);
        var shouldFireValidationStatusChangeEvent = oldValue == null
                && value == null
                && (isInputUnparsable() || isInputIncomplete());
        super.setValue(value);
        synchronizeChildComponentValues(value);
        if (shouldFireValidationStatusChangeEvent) {
            validate(true);
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
            datePicker.setValue(value.toLocalDate());
            timePicker.setValue(value.toLocalTime());
        } else {
            datePicker.setValue(null);
            timePicker.setValue(null);
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

    @Override
    public void setInvalid(boolean invalid) {
        HasValidationProperties.super.setInvalid(invalid);
        datePicker.setInvalid(invalid);
        timePicker.setInvalid(invalid);
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
     * Sets the aria-label suffix for the date picker.
     * <p>
     * The suffix set with this method takes precedence over the suffix set with
     * {@link DateTimePickerI18n#setDateLabel(String)}.
     * <p>
     * The date picker's final aria-label is a concatenation of DateTimePicker's
     * {@link #getAriaLabel()} or {@link #getLabel()} and this suffix.
     *
     * @param dateLabel
     *            the value to be used as a suffix in the date picker
     *            aria-label.
     */
    public void setDateAriaLabel(String dateLabel) {
        dateAriaLabel = dateLabel;
        updateI18n();
    }

    /**
     * Gets the aria-label suffix for the date picker.
     * <p>
     * Note: this method will return the last value passed to
     * {@link #setDateAriaLabel(String)}, not the value currently set on the
     * `aria-label` attribute of the date picker input element.
     *
     * @return an optional label or an empty optional if no label has been set
     *         with this method before.
     */
    public Optional<String> getDateAriaLabel() {
        return Optional.ofNullable(dateAriaLabel);
    }

    /**
     * Sets the aria-label suffix for the time picker.
     * <p>
     * The suffix set with this method takes precedence over the suffix set with
     * {@link DateTimePickerI18n#setTimeLabel(String)}.
     * <p>
     * The time picker's final aria-label is a concatenation of DateTimePicker's
     * {@link #getAriaLabel()} or {@link #getLabel()} and this suffix.
     *
     * @param timeLabel
     *            the value to be used as a suffix in the time picker
     *            aria-label.
     */
    public void setTimeAriaLabel(String timeLabel) {
        timeAriaLabel = timeLabel;
        updateI18n();
    }

    /**
     * Gets the aria-label suffix for the time picker.
     * <p>
     * Note: this method will return the last value passed to
     * {@link #setTimeAriaLabel(String)}, not the value currently set on the
     * `aria-label` attribute of the time picker input element.
     *
     * @return an optional label or an empty optional if no label has been set
     *         with this method before.
     */
    public Optional<String> getTimeAriaLabel() {
        return Optional.ofNullable(timeAriaLabel);
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

    private boolean isInputUnparsable() {
        return datePicker.isPickerInputUnparsable()
                || timePicker.isPickerInputUnparsable();
    }

    private boolean isInputIncomplete() {
        return datePicker.isEmpty() != timePicker.isEmpty();
    }

    @Override
    public Validator<LocalDateTime> getDefaultValidator() {
        return defaultValidator;
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<LocalDateTime> listener) {
        return Registration.addAndRemove(validationStatusChangeListeners,
                listener);
    }

    @Override
    public void setManualValidation(boolean enabled) {
        validationController.setManualValidation(enabled);
    }

    /**
     * Validates the current value against the constraints and sets the
     * {@code invalid} property and the {@code errorMessage} property based on
     * the result. If a custom error message is provided with
     * {@link #setErrorMessage(String)}, it is used. Otherwise, the error
     * message defined in the i18n object is used.
     * <p>
     * The method does nothing if the manual validation mode is enabled.
     */
    protected void validate() {
        validationController.validate(getValue());
    }

    /**
     * Delegates the call to {@link #validate()} and additionally fires
     * {@link ValidationStatusChangeEvent} to notify Binder that it needs to
     * revalidate since the component's own validity state may have changed.
     * <p>
     * NOTE: There is no need to notify Binder separately when running
     * validation on {@link ValueChangeEvent}, as Binder already listens to this
     * event and revalidates automatically.
     */
    private void validate(boolean shouldFireValidationStatusChangeEvent) {
        validate();

        if (shouldFireValidationStatusChangeEvent) {
            ValidationStatusChangeEvent<LocalDateTime> event = new ValidationStatusChangeEvent<>(
                    this, !isInvalid());
            validationStatusChangeListeners.forEach(
                    listener -> listener.validationStatusChanged(event));
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
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link DateTimePicker#setDatePickerI18n(DatePickerI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public DatePickerI18n getDatePickerI18n() {
        return datePickerI18n;
    }

    /**
     * Sets the internationalization properties for the date picker inside this
     * component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setDatePickerI18n(DatePickerI18n i18n) {
        this.datePickerI18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        datePicker.setI18n(i18n);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * Note: updating the instance that is returned from this method will not
     * update the component if not set back using
     * {@link DateTimePicker#setI18n(DateTimePickerI18n)}
     *
     * @return the i18n object. It will be <code>null</code>, If the i18n
     *         properties weren't set.
     */
    public DateTimePickerI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not <code>null</code>
     */
    public void setI18n(DateTimePickerI18n i18n) {
        Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        this.i18n = i18n;
        updateI18n();
    }

    private void updateI18n() {
        DateTimePickerI18n i18nObject = i18n != null ? i18n
                : new DateTimePickerI18n();
        JsonObject i18nJson = (JsonObject) JsonSerializer.toJson(i18nObject);

        if (dateAriaLabel != null) {
            i18nJson.put("dateLabel", dateAriaLabel);
        }

        if (timeAriaLabel != null) {
            i18nJson.put("timeLabel", timeAriaLabel);
        }

        // Remove the error message properties because they aren't used on
        // the client-side.
        i18nJson.remove("badInputErrorMessage");
        i18nJson.remove("requiredErrorMessage");
        i18nJson.remove("minErrorMessage");
        i18nJson.remove("maxErrorMessage");

        getElement().setPropertyJson("i18n", i18nJson);
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

    private String getI18nErrorMessage(
            Function<DateTimePickerI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link DateTimePicker}.
     */
    public static class DateTimePickerI18n implements Serializable {
        private String dateLabel;
        private String timeLabel;
        private String badInputErrorMessage;
        private String incompleteInputErrorMessage;
        private String requiredErrorMessage;
        private String minErrorMessage;
        private String maxErrorMessage;

        /**
         * Gets the aria-label suffix for the date picker.
         * <p>
         * The date picker's final aria-label is a concatenation of the
         * DateTimePicker's {@link #getAriaLabel()} or {@link #getLabel()}
         * methods and this suffix.
         *
         * @return the value used as a suffix in the date picker aria-label.
         */
        public String getDateLabel() {
            return dateLabel;
        }

        /**
         * Sets the aria-label suffix for the date picker.
         * <p>
         * The date picker's final aria-label is a concatenation of the
         * DateTimePicker's {@link #getAriaLabel()} or {@link #getLabel()}
         * methods and this suffix.
         *
         * @param dateLabel
         *            the value to be used as a suffix in the date picker
         *            aria-label.
         * @return this instance for method chaining
         */
        public DateTimePickerI18n setDateLabel(String dateLabel) {
            this.dateLabel = dateLabel;
            return this;
        }

        /**
         * Gets the aria-label suffix for the time picker.
         * <p>
         * The time picker's aria-label is a concatenation of the
         * DateTimePicker's {@link #getAriaLabel()} or {@link #getLabel()}
         * methods and this suffix.
         *
         * @return the value used as a suffix in the time picker aria-label.
         */
        public String getTimeLabel() {
            return timeLabel;
        }

        /**
         * Sets the aria-label suffix for the time picker.
         * <p>
         * The time picker's aria-label is a concatenation of the
         * DateTimePicker's {@link #getAriaLabel()} or {@link #getLabel()}
         * methods and this suffix.
         *
         * @param timeLabel
         *            the value to be used as a suffix in the time picker
         *            aria-label.
         * @return this instance for method chaining
         */
        public DateTimePickerI18n setTimeLabel(String timeLabel) {
            this.timeLabel = timeLabel;
            return this;
        }

        /**
         * Gets the error message displayed when the field contains user input
         * that the server is unable to convert to type {@link LocalDateTime}.
         *
         * @return the error message or {@code null} if not set
         */
        public String getBadInputErrorMessage() {
            return badInputErrorMessage;
        }

        /**
         * Sets the error message to display when the field contains user input
         * that the server is unable to convert to type {@link LocalDateTime}.
         * <p>
         * Note, custom error messages set with
         * {@link DateTimePicker#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public DateTimePickerI18n setBadInputErrorMessage(String errorMessage) {
            badInputErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when either the date or time is
         * empty.
         *
         * @return the error message or {@code null} if not set
         */
        public String getIncompleteInputErrorMessage() {
            return incompleteInputErrorMessage;
        }

        /**
         * Sets the error message to display when either the date or time is
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link DateTimePicker#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public DateTimePickerI18n setIncompleteInputErrorMessage(
                String errorMessage) {
            incompleteInputErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see DateTimePicker#isRequiredIndicatorVisible()
         * @see DateTimePicker#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link DateTimePicker#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see DateTimePicker#isRequiredIndicatorVisible()
         * @see DateTimePicker#setRequiredIndicatorVisible(boolean)
         */
        public DateTimePickerI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the selected date and time are
         * earlier than the minimum allowed date and time.
         *
         * @return the error message or {@code null} if not set
         * @see DateTimePicker#getMin()
         * @see DateTimePicker#setMin(LocalDateTime)
         */
        public String getMinErrorMessage() {
            return minErrorMessage;
        }

        /**
         * Sets the error message to display when the selected date and time are
         * earlier than the minimum allowed time.
         * <p>
         * Note, custom error messages set with
         * {@link DateTimePicker#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see DateTimePicker#getMin()
         * @see DateTimePicker#setMin(LocalDateTime)
         */
        public DateTimePickerI18n setMinErrorMessage(String errorMessage) {
            minErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the selected date and time are
         * later than the maximum allowed date and time.
         *
         * @return the error message or {@code null} if not set
         * @see DateTimePicker#getMax()
         * @see DateTimePicker#setMax(LocalDateTime)
         */
        public String getMaxErrorMessage() {
            return maxErrorMessage;
        }

        /**
         * Sets the error message to display when the selected date and time are
         * later than the maximum allowed date and time.
         * <p>
         * Note, custom error messages set with
         * {@link DateTimePicker#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see DateTimePicker#getMax()
         * @see DateTimePicker#setMax(LocalDateTime)
         */
        public DateTimePickerI18n setMaxErrorMessage(String errorMessage) {
            maxErrorMessage = errorMessage;
            return this;
        }
    }
}
