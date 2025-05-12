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
package com.vaadin.flow.component.timepicker;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Stream;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasAriaLabel;
import com.vaadin.flow.component.HasPlaceholder;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasAutoOpen;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasOverlayClassName;
import com.vaadin.flow.component.shared.HasPrefix;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasValidationProperties;
import com.vaadin.flow.component.shared.InputField;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;

/**
 * Time Picker is an input field for entering or selecting a specific time. The
 * time can be entered directly using a keyboard or by choosing a value from a
 * set of predefined options presented in an overlay. The overlay opens when the
 * field is clicked or any input is entered when the field is focused.
 * <h2>Validation</h2>
 * <p>
 * Time Picker comes with a built-in validation mechanism based on constraints.
 * Validation is triggered whenever the user initiates a time change, for
 * example by selection from the dropdown or manual entry followed by Enter or
 * blur. Programmatic value changes trigger validation as well.
 * <p>
 * Validation verifies that the value is parsable into {@link LocalTime} and
 * satisfies the specified constraints. If validation fails, the component is
 * marked as invalid and an error message is displayed below the input.
 * <p>
 * The following constraints are supported:
 * <ul>
 * <li>{@link #setRequiredIndicatorVisible(boolean)}
 * <li>{@link #setMin(LocalTime)}
 * <li>{@link #setMax(LocalTime)}
 * </ul>
 * <p>
 * Error messages for unparsable input and constraints can be configured with
 * the {@link TimePickerI18n} object, using the respective properties. If you
 * want to provide a single catch-all error message, you can also use the
 * {@link #setErrorMessage(String)} method. Note that such an error message will
 * take priority over i18n error messages if both are set.
 * <p>
 * In addition to validation, constraints may also have a visual impact. For
 * example, times before the minimum time or after the maximum time are not
 * displayed in the dropdown to prevent their selection.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. By default, before running custom validators, Binder will
 * also check if the time is parsable and satisfies the component constraints,
 * displaying error messages from the {@link TimePickerI18n} object. The
 * exception is the required constraint, for which Binder provides its own API,
 * see {@link Binder.BindingBuilder#asRequired(String) asRequired()}.
 * <p>
 * However, if Binder doesn't fit your needs and you want to implement fully
 * custom validation logic, you can disable the constraint validation by setting
 * {@link #setManualValidation(boolean)} to true. This will allow you to control
 * the invalid state and the error message manually using
 * {@link #setInvalid(boolean)} and {@link #setErrorMessage(String)} API.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-time-picker")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/time-picker", version = "24.8.0-alpha18")
@JsModule("@vaadin/time-picker/src/vaadin-time-picker.js")
@JsModule("./vaadin-time-picker/timepickerConnector.js")
public class TimePicker
        extends AbstractSinglePropertyField<TimePicker, LocalTime>
        implements Focusable<TimePicker>, HasAllowedCharPattern, HasAriaLabel,
        HasAutoOpen, HasClearButton, HasClientValidation,
        InputField<AbstractField.ComponentValueChangeEvent<TimePicker, LocalTime>, LocalTime>,
        HasPrefix, HasOverlayClassName, HasThemeVariant<TimePickerVariant>,
        HasValidationProperties, HasValidator<LocalTime>, HasPlaceholder {

    private static final SerializableFunction<String, LocalTime> PARSER = valueFromClient -> {
        return valueFromClient == null || valueFromClient.isEmpty() ? null
                : LocalTime.parse(valueFromClient);
    };

    private static final SerializableFunction<LocalTime, String> FORMATTER = valueFromModel -> {
        return valueFromModel == null ? "" : valueFromModel.toString();
    };

    private TimePickerI18n i18n;

    private Locale locale;

    private LocalTime max;
    private LocalTime min;
    private StateTree.ExecutionRegistration pendingLocaleUpdate;

    private String unparsableValue;

    private final CopyOnWriteArrayList<ValidationStatusChangeListener<LocalTime>> validationStatusChangeListeners = new CopyOnWriteArrayList<>();

    private Validator<LocalTime> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        if (unparsableValue != null) {
            return ValidationResult.error(getI18nErrorMessage(
                    TimePickerI18n::getBadInputErrorMessage));
        }

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(
                            getI18nErrorMessage(
                                    TimePickerI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        ValidationResult maxResult = ValidationUtil.validateMaxConstraint(
                getI18nErrorMessage(TimePickerI18n::getMaxErrorMessage), value,
                max);
        if (maxResult.isError()) {
            return maxResult;
        }

        ValidationResult minResult = ValidationUtil.validateMinConstraint(
                getI18nErrorMessage(TimePickerI18n::getMinErrorMessage), value,
                min);
        if (minResult.isError()) {
            return minResult;
        }

        return ValidationResult.ok();
    };

    private ValidationController<TimePicker, LocalTime> validationController = new ValidationController<>(
            this);

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
        super("value", null, String.class, PARSER, FORMATTER);

        // Initialize property value unless it has already been set from a
        // template
        if ((getElement().getProperty("value") == null
                || !isInitialValueOptional)) {
            setPresentationValue(time);
        }

        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        addValueChangeListener(e -> validate());

        getElement().addEventListener("unparsable-change", event -> {
            // The unparsable-change event is fired in the following situations:
            // 1. User modifies input but it remains unparsable
            // 2. User enters unparsable input in empty field
            // 3. User clears unparsable input
            //
            // In all these cases, ValueChangeEvent isn't fired, so
            // we call setModelValue manually to trigger validation.
            setModelValue(getEmptyValue(), true);
        });

        getElement().addPropertyChangeListener("invalid", event -> fireEvent(
                new InvalidChangeEvent(this, event.isUserOriginated())));
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
     * {@inheritDoc}
     * <p>
     * Distinct error messages for unparsable input and different constraints
     * can be configured with the {@link TimePickerI18n} object, using the
     * respective properties. However, note that the error message set with
     * {@link #setErrorMessage(String)} will take priority and override any i18n
     * error messages if both are set.
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        HasValidationProperties.super.setErrorMessage(errorMessage);
    }

    /**
     * Sets the label for the time picker.
     *
     * @param label
     *            value for the {@code label} property in the time picker
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
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
        LocalTime oldValue = getValue();
        if (oldValue == null && value == null && unparsableValue != null) {
            // When the value is programmatically cleared while the field
            // contains an unparsable input, ValueChangeEvent isn't fired,
            // so we need to call setModelValue manually to clear the bad
            // input and trigger validation.
            setModelValue(getEmptyValue(), false);
            return;
        }

        // Truncate the value to millisecond precision, as the is the maximum
        // that the time picker web component supports.
        if (value != null) {
            value = value.truncatedTo(ChronoUnit.MILLIS);
        }

        super.setValue(value);
    }

    @Override
    protected void setModelValue(LocalTime newModelValue, boolean fromClient) {
        LocalTime oldModelValue = getValue();
        String oldUnparsableValue = unparsableValue;

        if (fromClient && newModelValue == null
                && !getInputElementValue().isEmpty()) {
            unparsableValue = getInputElementValue();
        } else {
            unparsableValue = null;
        }

        boolean isModelValueRemainedEmpty = newModelValue == null
                && oldModelValue == null;

        // Cases:
        // - User modifies input but it remains unparsable
        // - User enters unparsable input in empty field
        // - User clears unparsable input
        if (fromClient && isModelValueRemainedEmpty) {
            validate();
            fireValidationStatusChangeEvent();
            return;
        }

        // Case: setValue(null) is called on a field with unparsable input
        if (!fromClient && isModelValueRemainedEmpty
                && oldUnparsableValue != null) {
            setInputElementValue("");
            validate();
            fireValidationStatusChangeEvent();
            return;
        }

        super.setModelValue(newModelValue, fromClient);
    }

    /**
     * Gets the label of the time picker.
     *
     * @return the {@code label} property of the time picker
     */
    public String getLabel() {
        return getElement().getProperty("label");
    }

    @Override
    public void setAriaLabel(String ariaLabel) {
        getElement().setProperty("accessibleName", ariaLabel);
    }

    @Override
    public Optional<String> getAriaLabel() {
        return Optional.ofNullable(getElement().getProperty("accessibleName"));
    }

    @Override
    public void setAriaLabelledBy(String labelledBy) {
        getElement().setProperty("accessibleNameRef", labelledBy);
    }

    @Override
    public Optional<String> getAriaLabelledBy() {
        return Optional
                .ofNullable(getElement().getProperty("accessibleNameRef"));
    }

    @Override
    public Validator<LocalTime> getDefaultValidator() {
        return defaultValidator;
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<LocalTime> listener) {
        return Registration.addAndRemove(validationStatusChangeListeners,
                listener);
    }

    /**
     * Notifies Binder that it needs to revalidate the component since the
     * component's validity state may have changed. Note, there is no need to
     * notify Binder separately in the case of a ValueChangeEvent, as Binder
     * already listens to this event and revalidates automatically.
     */
    private void fireValidationStatusChangeEvent() {
        ValidationStatusChangeEvent<LocalTime> event = new ValidationStatusChangeEvent<>(
                this, !isInvalid());
        validationStatusChangeListeners
                .forEach(listener -> listener.validationStatusChanged(event));
    }

    /**
     * Returns whether the input element has a value or not.
     *
     * @return <code>true</code> if the input element's value is populated,
     *         <code>false</code> otherwise
     */
    protected boolean isInputValuePresent() {
        return !getInputElementValue().isEmpty();
    }

    /**
     * Gets the value of the input element. This value is updated on the server
     * when the web component dispatches a `change` or `unparsable-change`
     * event. Except when clearing the value, {@link #setValue(LocalTime)} does
     * not update the input element value on the server because it requires date
     * formatting, which is implemented on the web component's side.
     *
     * @return the value of the input element
     */
    @Synchronize(property = "_inputElementValue", value = { "change",
            "unparsable-change" })
    private String getInputElementValue() {
        return getElement().getProperty("_inputElementValue", "");
    }

    /**
     * Sets the value of the input element.
     *
     * @param value
     *            the value to set
     */
    private void setInputElementValue(String value) {
        getElement().setProperty("_inputElementValue", value);
    }

    /**
     * Sets whether the user is required to provide a value. When required, an
     * indicator appears next to the label and the field invalidates if the
     * value is cleared.
     * <p>
     * NOTE: The required indicator is only visible when the field has a label,
     * see {@link #setLabel(String)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     * @see TimePickerI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Gets whether the user is required to provide a value.
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     * @see #setRequiredIndicatorVisible(boolean)
     */
    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    /**
     * Alias for {@link #setRequiredIndicatorVisible(boolean)}.
     *
     * @param required
     *            {@code true} to make the field required, {@code false}
     *            otherwise
     */
    public void setRequired(boolean required) {
        setRequiredIndicatorVisible(required);
    }

    /**
     * Alias for {@link #isRequiredIndicatorVisible()}
     *
     * @return {@code true} if the field is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return isRequiredIndicatorVisible();
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
        double step = getElement().getProperty("step", 0.0);
        return StepsUtil.convertStepsValueToDuration(step);
    }

    /**
     * {@code invalid-changed} event is sent when the invalid state changes.
     */
    public static class InvalidChangeEvent extends ComponentEvent<TimePicker> {
        private final boolean invalid;

        public InvalidChangeEvent(TimePicker source, boolean fromClient) {
            super(source, fromClient);
            this.invalid = source.isInvalid();
        }

        public boolean isInvalid() {
            return invalid;
        }
    }

    /**
     * Adds a listener for {@code invalid-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addInvalidChangeListener(
            ComponentEventListener<InvalidChangeEvent> listener) {
        return addListener(InvalidChangeEvent.class, listener);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
        requestLocaleUpdate();
    }

    private void initConnector() {
        // can't run this with getElement().executeJs(...) since then
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
     * Sets the minimum time allowed to be selected for this field. Times before
     * that won't be displayed in the dropdown. Manual entry of such times will
     * cause the component to invalidate.
     * <p>
     * The minimum time is inclusive.
     *
     * @param min
     *            the minimum time, or {@code null} to remove this constraint
     * @see TimePickerI18n#setMinErrorMessage(String)
     */
    public void setMin(LocalTime min) {
        this.min = min;
        String minString = format(min);
        getElement().setProperty("min", minString == null ? "" : minString);
    }

    /**
     * Gets the minimum time allowed to be selected for this field.
     *
     * @return the minimum time, or {@code null} if no minimum is set
     * @see #setMax(LocalTime)
     */
    public LocalTime getMin() {
        return this.min;
    }

    /**
     * Sets the maximum time allowed to be selected for this field. Times after
     * that won't be displayed in the dropdown. Manual entry of such times will
     * cause the component to invalidate.
     * <p>
     * The maximum time is inclusive.
     *
     * @param max
     *            the maximum time, or {@code null} to remove this constraint
     * @see TimePickerI18n#setMaxErrorMessage(String)
     */
    public void setMax(LocalTime max) {
        this.max = max;
        String maxString = format(max);
        getElement().setProperty("max", maxString == null ? "" : maxString);
    }

    /**
     * Gets the maximum time allowed to be selected for this field.
     *
     * @return the maximum time, or {@code null} if no maximum is set
     * @see #setMin(LocalTime)
     */
    public LocalTime getMax() {
        return this.max;
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

    private static String format(LocalTime time) {
        return time != null ? time.toString() : null;
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(TimePickerI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public TimePickerI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(TimePickerI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<TimePickerI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link TimePicker}.
     */
    public static class TimePickerI18n implements Serializable {

        private String badInputErrorMessage;
        private String requiredErrorMessage;
        private String minErrorMessage;
        private String maxErrorMessage;

        /**
         * Gets the error message displayed when the field contains user input
         * that the server is unable to convert to type {@link LocalTime}.
         *
         * @return the error message or {@code null} if not set
         */
        public String getBadInputErrorMessage() {
            return badInputErrorMessage;
        }

        /**
         * Sets the error message to display when the field contains user input
         * that the server is unable to convert to type {@link LocalTime}.
         * <p>
         * Note, custom error messages set with
         * {@link TimePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public TimePickerI18n setBadInputErrorMessage(String errorMessage) {
            badInputErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see TimePicker#isRequiredIndicatorVisible()
         * @see TimePicker#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link TimePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see TimePicker#isRequiredIndicatorVisible()
         * @see TimePicker#setRequiredIndicatorVisible(boolean)
         */
        public TimePickerI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the selected time is earlier
         * than the minimum allowed time.
         *
         * @return the error message or {@code null} if not set
         * @see TimePicker#getMin()
         * @see TimePicker#setMin(LocalTime)
         */
        public String getMinErrorMessage() {
            return minErrorMessage;
        }

        /**
         * Sets the error message to display when the selected time is earlier
         * than the minimum allowed time.
         * <p>
         * Note, custom error messages set with
         * {@link TimePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see TimePicker#getMin()
         * @see TimePicker#setMin(LocalTime)
         */
        public TimePickerI18n setMinErrorMessage(String errorMessage) {
            minErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the selected time is later than
         * the maximum allowed time.
         *
         * @return the error message or {@code null} if not set
         * @see TimePicker#getMax()
         * @see TimePicker#setMax(LocalTime)
         */
        public String getMaxErrorMessage() {
            return maxErrorMessage;
        }

        /**
         * Sets the error message to display when the selected time is later
         * than the maximum allowed time.
         * <p>
         * Note, custom error messages set with
         * {@link TimePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see TimePicker#getMax()
         * @see TimePicker#setMax(LocalTime)
         */
        public TimePickerI18n setMaxErrorMessage(String errorMessage) {
            maxErrorMessage = errorMessage;
            return this;
        }
    }
}
