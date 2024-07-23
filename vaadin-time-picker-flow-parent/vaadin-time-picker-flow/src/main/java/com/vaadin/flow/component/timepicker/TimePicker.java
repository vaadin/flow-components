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
import com.vaadin.flow.component.shared.ClientValidationUtil;
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
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-time-picker")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha5")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/time-picker", version = "24.5.0-alpha5")
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

    private boolean manualValidationEnabled = false;

    private String customErrorMessage;
    private String constraintErrorMessage;

    private final CopyOnWriteArrayList<ValidationStatusChangeListener<LocalTime>> validationStatusChangeListeners = new CopyOnWriteArrayList<>();

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
        super("value", time, String.class, PARSER, FORMATTER);

        // Initialize property value unless it has already been set from a
        // template
        if ((getElement().getProperty("value") == null
                || !isInitialValueOptional)) {
            setPresentationValue(time);
        }

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        addValueChangeListener(e -> validate());

        getElement().addEventListener("unparsable-change", event -> {
            validate();
            fireValidationStatusChangeEvent();
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
     * Sets an error message to display for all constraint violations.
     * <p>
     * This error message takes priority over i18n error messages when both are
     * set.
     *
     * @param errorMessage
     *            the error message to set, or {@code null} to clear
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        customErrorMessage = errorMessage;
        updateErrorMessage();
    }

    /**
     * Gets the error message displayed for all constraint violations.
     *
     * @return the error message
     */
    @Override
    public String getErrorMessage() {
        return customErrorMessage;
    }

    private void setConstraintErrorMessage(String errorMessage) {
        constraintErrorMessage = errorMessage;
        updateErrorMessage();
    }

    private void updateErrorMessage() {
        String errorMessage = constraintErrorMessage;
        if (customErrorMessage != null && !customErrorMessage.isEmpty()) {
            errorMessage = customErrorMessage;
        }
        getElement().setProperty("errorMessage", errorMessage);
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
        // Truncate the value to millisecond precision, as the is the maximum
        // that the time picker web component supports.
        if (value != null) {
            value = value.truncatedTo(ChronoUnit.MILLIS);
        }

        LocalTime oldValue = getValue();
        boolean isOldValueEmpty = valueEquals(oldValue, getEmptyValue());
        boolean isNewValueEmpty = valueEquals(value, getEmptyValue());
        boolean isValueRemainedEmpty = isOldValueEmpty && isNewValueEmpty;
        boolean isInputValuePresent = isInputValuePresent();

        // When the value is cleared programmatically, reset hasInputValue
        // so that the following validation doesn't treat this as bad input.
        if (isNewValueEmpty) {
            getElement().setProperty("_hasInputValue", false);
        }

        super.setValue(value);

        // Clear the input element from possible bad input.
        if (isValueRemainedEmpty && isInputValuePresent) {
            // The check for value presence guarantees that a non-empty value
            // won't get cleared when setValue(null) and setValue(...) are
            // subsequently called within one round-trip.
            // Flow only sends the final component value to the client
            // when you update the value multiple times during a round-trip
            // and the final value is sent in place of the first one, so
            // `executeJs` can end up invoked after a non-empty value is set.
            getElement()
                    .executeJs("if (!this.value) this._inputElementValue = ''");
            validate();
            fireValidationStatusChangeEvent();
        }
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
        return (value, context) -> checkValidity(value, false);
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

    private ValidationResult checkValidity(LocalTime value,
            boolean withRequiredValidator) {
        boolean hasBadInput = Objects.equals(value, getEmptyValue())
                && isInputValuePresent();
        if (hasBadInput) {
            return ValidationResult.error(getI18nErrorMessage(
                    TimePickerI18n::getBadInputErrorMessage));
        }

        if (withRequiredValidator) {
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
    }

    /**
     * Returns whether the input element has a value or not.
     *
     * @return <code>true</code> if the input element's value is populated,
     *         <code>false</code> otherwise
     */
    @Synchronize(property = "_hasInputValue", value = "has-input-value-changed")
    protected boolean isInputValuePresent() {
        return getElement().getProperty("_hasInputValue", false);
    }

    /**
     * Sets whether the time picker is marked as input required.
     *
     * @param required
     *            the boolean value to set
     */
    public void setRequired(boolean required) {
        setRequiredIndicatorVisible(required);
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
        this.manualValidationEnabled = enabled;
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
        if (this.manualValidationEnabled) {
            return;
        }

        ValidationResult result = checkValidity(getValue(), true);
        if (result.isError()) {
            setInvalid(true);
            setConstraintErrorMessage(result.getErrorMessage());
        } else {
            setInvalid(false);
            setConstraintErrorMessage("");
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
        requestLocaleUpdate();
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
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
     * Sets the minimum time in the time picker. Times before that will be
     * disabled in the popup.
     *
     * @param min
     *            the minimum time that is allowed to be selected, or
     *            <code>null</code> to remove any minimum constraints
     */
    public void setMin(LocalTime min) {
        this.min = min;
        String minString = format(min);
        getElement().setProperty("min", minString == null ? "" : minString);
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
     * Sets the maximum time in the time picker. Times after that will be
     * disabled in the popup.
     *
     * @param max
     *            the maximum time that is allowed to be selected, or
     *            <code>null</code> to remove any maximum constraints
     */
    public void setMax(LocalTime max) {
        this.max = max;
        String maxString = format(max);
        getElement().setProperty("max", maxString == null ? "" : maxString);
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
