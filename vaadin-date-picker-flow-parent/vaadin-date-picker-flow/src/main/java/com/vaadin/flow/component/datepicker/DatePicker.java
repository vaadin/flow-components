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
package com.vaadin.flow.component.datepicker;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

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
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.internal.StateTree;
import com.vaadin.flow.shared.Registration;

import elemental.json.JsonObject;
import elemental.json.JsonType;

/**
 * Date Picker is an input field that allows the user to enter a date by typing
 * or by selecting from a calendar overlay.
 * <p>
 * DatePicker allows setting and getting {@link LocalDate} objects, setting
 * minimum and maximum date ranges and has internationalization support by using
 * the {@link DatePickerI18n} object.
 * <p>
 * This component allows the date to be entered directly using the keyboard in
 * the format of the current locale or through the date picker overlay. The
 * overlay opens when the field is clicked and/or any input is entered when the
 * field is focused.
 * <h2>Validation</h2>
 * <p>
 * Date Picker comes with a built-in validation mechanism based on constraints.
 * Validation is triggered whenever the user initiates a date change, for
 * example by selection from the overlay or manual entry followed by Enter or
 * blur. Programmatic value changes trigger validation as well.
 * <p>
 * Validation verifies that the value is parsable into {@link LocalDate} and
 * satisfies the specified constraints. If validation fails, the component is
 * marked as invalid and an error message is displayed below the input.
 * <p>
 * The following constraints are supported:
 * <ul>
 * <li>{@link #setRequiredIndicatorVisible(boolean)}
 * <li>{@link #setMin(LocalDate)}
 * <li>{@link #setMax(LocalDate)}
 * </ul>
 * <p>
 * Error messages for unparsable input and constraints can be configured with
 * the {@link DatePickerI18n} object, using the respective properties. If you
 * want to provide a single catch-all error message, you can also use the
 * {@link #setErrorMessage(String)} method. Note that such an error message will
 * take priority over i18n error messages if both are set.
 * <p>
 * In addition to validation, constraints may also have a visual aspect. For
 * example, dates before the minimum date are displayed as disabled in the
 * overlay to prevent their selection.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. By default, before running custom validators, Binder will
 * also check if the date is parsable and satisfies the component constraints,
 * displaying error messages from the {@link DatePickerI18n} object. The
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
@Tag("vaadin-date-picker")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/date-picker", version = "24.8.0-alpha18")
@JsModule("@vaadin/date-picker/src/vaadin-date-picker.js")
@JsModule("./datepickerConnector.js")
@NpmPackage(value = "date-fns", version = "2.29.3")
public class DatePicker
        extends AbstractSinglePropertyField<DatePicker, LocalDate>
        implements Focusable<DatePicker>, HasAllowedCharPattern, HasAriaLabel,
        HasAutoOpen, HasClearButton, HasClientValidation,
        InputField<AbstractField.ComponentValueChangeEvent<DatePicker, LocalDate>, LocalDate>,
        HasOverlayClassName, HasPrefix, HasThemeVariant<DatePickerVariant>,
        HasValidationProperties, HasValidator<LocalDate>, HasPlaceholder {

    private DatePickerI18n i18n;

    private final static SerializableFunction<String, LocalDate> PARSER = s -> {
        return s == null || s.isEmpty() ? null : LocalDate.parse(s);
    };

    private final static SerializableFunction<LocalDate, String> FORMATTER = d -> {
        return d == null ? "" : d.toString();
    };

    private Locale locale;

    private LocalDate max;
    private LocalDate min;

    private StateTree.ExecutionRegistration pendingI18nUpdate;

    private String unparsableValue;

    private SerializableFunction<String, Result<LocalDate>> fallbackParser;
    private String fallbackParserErrorMessage = null;
    private boolean isFallbackParserRunning = false;

    private final CopyOnWriteArrayList<ValidationStatusChangeListener<LocalDate>> validationStatusChangeListeners = new CopyOnWriteArrayList<>();

    private Validator<LocalDate> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        if (unparsableValue != null && fallbackParserErrorMessage != null) {
            return ValidationResult.error(fallbackParserErrorMessage);
        } else if (unparsableValue != null) {
            return ValidationResult.error(getI18nErrorMessage(
                    DatePickerI18n::getBadInputErrorMessage));
        }

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(
                            getI18nErrorMessage(
                                    DatePickerI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        ValidationResult maxResult = ValidationUtil.validateMaxConstraint(
                getI18nErrorMessage(DatePickerI18n::getMaxErrorMessage), value,
                max);
        if (maxResult.isError()) {
            return maxResult;
        }

        ValidationResult minResult = ValidationUtil.validateMinConstraint(
                getI18nErrorMessage(DatePickerI18n::getMinErrorMessage), value,
                min);
        if (minResult.isError()) {
            return minResult;
        }

        return ValidationResult.ok();
    };

    private ValidationController<DatePicker, LocalDate> validationController = new ValidationController<>(
            this);

    /**
     * Default constructor.
     */
    public DatePicker() {
        this((LocalDate) null, true);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date
     * in current UI locale format.
     *
     * @param initialDate
     *            the pre-selected date in the picker
     * @see #setValue(Object)
     */
    public DatePicker(LocalDate initialDate) {
        this(initialDate, false);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date
     * in current UI locale format.
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
     *
     * @param initialDate
     *            the pre-selected date in the picker
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     * @see #setValue(Object)
     */
    private DatePicker(LocalDate initialDate, boolean isInitialValueOptional) {
        super("value", null, String.class, PARSER, FORMATTER);

        getElement().setProperty("manualValidation", true);

        // Initialize property value unless it has already been set from a
        // template
        if ((getElement().getProperty("value") == null
                || !isInitialValueOptional)) {
            setPresentationValue(initialDate);
        }

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
            // we call setModelValue manually to run fallback parser
            // and trigger validation.
            setModelValue(getEmptyValue(), true);
        });

        getElement().addPropertyChangeListener("opened", event -> fireEvent(
                new OpenedChangeEvent(this, event.isUserOriginated())));

        getElement().addPropertyChangeListener("invalid", event -> fireEvent(
                new InvalidChangeEvent(this, event.isUserOriginated())));
    }

    /**
     * Convenience constructor to create a date picker with a label.
     *
     * @param label
     *            the label describing the date picker
     * @see #setLabel(String)
     */
    public DatePicker(String label) {
        this();
        setLabel(label);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date
     * in current UI locale format and a label.
     *
     * @param label
     *            the label describing the date picker
     * @param initialDate
     *            the pre-selected date in the picker
     * @see #setValue(Object)
     * @see #setLabel(String)
     */
    public DatePicker(String label, LocalDate initialDate) {
        this(initialDate);
        setLabel(label);
    }

    /**
     * Convenience constructor to create a date picker with a
     * {@link ValueChangeListener}.
     *
     * @param listener
     *            the listener to receive value change events
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(
            ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date picker with a
     * {@link ValueChangeListener} and a label.
     *
     *
     * @param label
     *            the label describing the date picker
     * @param listener
     *            the listener to receive value change events
     * @see #setLabel(String)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(String label,
            ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date
     * in current UI locale format and a {@link ValueChangeListener}.
     *
     * @param initialDate
     *            the pre-selected date in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setValue(Object)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(LocalDate initialDate,
            ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> listener) {
        this(initialDate);
        addValueChangeListener(listener);
    }

    /**
     * Convenience constructor to create a date picker with a pre-selected date
     * in current UI locale format, a {@link ValueChangeListener} and a label.
     *
     * @param label
     *            the label describing the date picker
     * @param initialDate
     *            the pre-selected date in the picker
     * @param listener
     *            the listener to receive value change events
     * @see #setLabel(String)
     * @see #setValue(Object)
     * @see #addValueChangeListener(HasValue.ValueChangeListener)
     */
    public DatePicker(String label, LocalDate initialDate,
            ValueChangeListener<ComponentValueChangeEvent<DatePicker, LocalDate>> listener) {
        this(initialDate);
        setLabel(label);
        addValueChangeListener(listener);
    }

    /**
     * Convenience Constructor to create a date picker with pre-selected date
     * and locale setup.
     *
     * @param initialDate
     *            the pre-selected date in the picker
     * @param locale
     *            the locale for the date picker
     */
    public DatePicker(LocalDate initialDate, Locale locale) {
        this(initialDate);
        setLocale(locale);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Distinct error messages for unparsable input and different constraints
     * can be configured with the {@link DatePickerI18n} object, using the
     * respective properties. However, note that the error message set with
     * {@link #setErrorMessage(String)} will take priority and override any i18n
     * error messages if both are set.
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        HasValidationProperties.super.setErrorMessage(errorMessage);
    }

    /**
     * Sets the minimum date allowed to be selected for this field. Dates before
     * that will be disabled in the calendar overlay. Manual entry of such dates
     * will cause the component to invalidate.
     * <p>
     * The minimum date is inclusive.
     *
     * @param min
     *            the minimum date, or {@code null} to remove this constraint
     * @see DatePickerI18n#setMinErrorMessage(String)
     */
    public void setMin(LocalDate min) {
        String minAsString = FORMATTER.apply(min);
        getElement().setProperty("min", minAsString == null ? "" : minAsString);
        this.min = min;
    }

    /**
     * Gets the minimum date allowed to be selected for this field.
     *
     * @return the minimum date, or {@code null} if no minimum is set
     * @see #setMax(LocalDate)
     */
    public LocalDate getMin() {
        return PARSER.apply(getElement().getProperty("min"));
    }

    /**
     * Sets the maximum date allowed to be selected for this field. Dates after
     * that will be disabled in the calendar overlay. Manual entry of such dates
     * will cause the component to invalidate.
     * <p>
     * The maximum date is inclusive.
     *
     * @param max
     *            the maximum date, or {@code null} to remove this constraint
     * @see DatePickerI18n#setMaxErrorMessage(String)
     */
    public void setMax(LocalDate max) {
        String maxAsString = FORMATTER.apply(max);
        getElement().setProperty("max", maxAsString == null ? "" : maxAsString);
        this.max = max;
    }

    /**
     * Gets the maximum date allowed to be selected for this field.
     *
     * @return the maximum date, or {@code null} if no maximum is set
     * @see #setMax(LocalDate)
     */
    public LocalDate getMax() {
        return PARSER.apply(getElement().getProperty("max"));
    }

    /**
     * Set the Locale for the Date Picker. The displayed date will be matched to
     * the format used in that locale.
     * <p>
     * NOTE:Supported formats are MM/DD/YYYY, DD/MM/YYYY and YYYY/MM/DD. Browser
     * compatibility can be different based on the browser and mobile devices,
     * you can check here for more details: <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString">https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date/toLocaleDateString</a>
     * <p>
     * When using custom date formats through
     * {@link DatePicker#setI18n(DatePickerI18n)}, setting a locale has no
     * effect, and dates will always be parsed and displayed using the custom
     * date format.
     *
     * @param locale
     *            the locale set to the date picker, cannot be null
     */
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null.");
        this.locale = locale;
        requestI18nUpdate();
    }

    /**
     * Gets the Locale for this date picker
     *
     * @return the locale used for this picker
     */
    @Override
    public Locale getLocale() {
        if (locale != null) {
            return locale;
        } else {
            return super.getLocale();
        }
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
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initConnector();
        requestI18nUpdate();
    }

    private void initConnector() {
        runBeforeClientResponse(ui -> ui.getPage().executeJs(
                "window.Vaadin.Flow.datepickerConnector.initLazy($0)",
                getElement()));
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(DatePickerI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public DatePickerI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(DatePickerI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
        requestI18nUpdate();
    }

    private void requestI18nUpdate() {
        getUI().ifPresent(ui -> {
            if (pendingI18nUpdate != null) {
                pendingI18nUpdate.remove();
            }
            pendingI18nUpdate = ui.beforeClientResponse(this, context -> {
                pendingI18nUpdate = null;
                executeI18nUpdate();
            });
        });
    }

    /**
     * Update I18N settings in the web component. Merges the DatePickerI18N
     * settings with the current settings of the web component, and configures
     * formatting and parsing functions based on either the locale, or the
     * custom date formats specified in DatePickerI18N.
     */
    private void executeI18nUpdate() {
        JsonObject i18nObject = getI18nAsJsonObject();

        // For ill-formed locales, Locale.toLanguageTag() will append subtag
        // "lvariant" to it, which will cause the client side
        // Date().toLocaleDateString()
        // fallback to the system default locale silently.
        // This has been caught by DatePickerValidationPage::invalidLocale test
        // when running on
        // Chrome(73+)/FireFox(66)/Edge(42.17134).
        Locale appliedLocale = getLocale();
        String languageTag;
        if (!appliedLocale.toLanguageTag().contains("lvariant")) {
            languageTag = appliedLocale.toLanguageTag();
        } else if (appliedLocale.getCountry().isEmpty()) {
            languageTag = appliedLocale.getLanguage();
        } else {
            languageTag = appliedLocale.getLanguage() + "-"
                    + appliedLocale.getCountry();
        }

        // Call update function in connector with locale and I18N settings
        // The connector is expected to handle that either of those can be null
        getElement().callJsFunction("$connector.updateI18n", languageTag,
                i18nObject);
    }

    private JsonObject getI18nAsJsonObject() {
        if (i18n == null) {
            return null;
        }
        JsonObject i18nObject = (JsonObject) JsonSerializer.toJson(i18n);
        // LocalDate objects have to be explicitly added to the serialized i18n
        // object in order to be formatted correctly
        if (i18n.getReferenceDate() != null) {
            i18nObject.put("referenceDate",
                    i18n.getReferenceDate().format(DateTimeFormatter.ISO_DATE));
        }

        // Remove the error message properties because they aren't used on
        // the client-side.
        i18nObject.remove("badInputErrorMessage");
        i18nObject.remove("requiredErrorMessage");
        i18nObject.remove("minErrorMessage");
        i18nObject.remove("maxErrorMessage");

        // Remove properties with null values to prevent errors in web component
        removeNullValuesFromJsonObject(i18nObject);
        return i18nObject;
    }

    private void removeNullValuesFromJsonObject(JsonObject jsonObject) {
        for (String key : jsonObject.keys()) {
            if (jsonObject.get(key).getType() == JsonType.NULL) {
                jsonObject.remove(key);
            }
        }
    }

    void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }

    @Override
    public Validator<LocalDate> getDefaultValidator() {
        return defaultValidator;
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<LocalDate> listener) {
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
        ValidationStatusChangeEvent<LocalDate> event = new ValidationStatusChangeEvent<>(
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
     * event. Except when clearing the value, {@link #setValue(LocalDate)} does
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
     * Sets a parser to handle user input that cannot be parsed using the i18n
     * date formats.
     * <p>
     * The parser is a function that receives the user-entered string and
     * returns a {@link Result} with the parsed date or an error message. If the
     * parser returns an error message, the field will be marked as invalid,
     * displaying that message as a validation error.
     * <p>
     * Example:
     *
     * <pre>
     * datePicker.setFallbackParser(s -> {
     *     if (s.equals("tomorrow")) {
     *         return Result.ok(LocalDate.now().plusDays(1));
     *     } else {
     *         return Result.error("Invalid date format");
     *     }
     * });
     * </pre>
     * <p>
     * NOTE: When a fallback parser is set, the i18n error message from
     * {@link DatePickerI18n#getBadInputErrorMessage()} is not used.
     *
     * @param fallbackParser
     *            the parser function
     */
    public void setFallbackParser(
            SerializableFunction<String, Result<LocalDate>> fallbackParser) {
        this.fallbackParser = fallbackParser;
        this.fallbackParserErrorMessage = null;
    }

    /**
     * Gets the parser that is used as a fallback when user input cannot be
     * parsed using the i18n date formats.
     *
     * @return the parser function
     */
    public SerializableFunction<String, Result<LocalDate>> getFallbackParser() {
        return fallbackParser;
    }

    private Result<LocalDate> runFallbackParser(String s) {
        Result<LocalDate> result = null;

        try {
            result = fallbackParser.apply(s);
        } catch (Exception e) {
            LoggerFactory.getLogger(DatePicker.class)
                    .error("Fallback parser threw an exception", e);
            result = Result.error(getI18nErrorMessage(
                    DatePickerI18n::getBadInputErrorMessage));
        }

        return Objects.requireNonNull(result, "Result cannot be null");
    }

    @Override
    public void setValue(LocalDate value) {
        LocalDate oldValue = getValue();
        if (oldValue == null && value == null && unparsableValue != null) {
            // When the value is programmatically cleared while the field
            // contains an unparsable input, ValueChangeEvent isn't fired,
            // so we need to call setModelValue manually to clear the bad
            // input and trigger validation.
            setModelValue(getEmptyValue(), false);
            return;
        }

        super.setValue(value);
    }

    @Override
    protected void setModelValue(LocalDate newModelValue, boolean fromClient) {
        // Ignore setModelValue calls triggered by setPresentationValue
        // when the fallback parser applies a parsed value (see below).
        // This ensures that the ValueChangeEvent fires from the original
        // setModelValue call with `fromClient` value: `true`.
        if (isFallbackParserRunning) {
            return;
        }

        LocalDate oldModelValue = getValue();
        String oldUnparsableValue = unparsableValue;

        if (fromClient && newModelValue == null
                && !getInputElementValue().isEmpty()) {
            unparsableValue = getInputElementValue();
        } else {
            unparsableValue = null;
        }

        try {
            isFallbackParserRunning = true;

            if (fallbackParser != null && unparsableValue != null) {
                Result<LocalDate> result = runFallbackParser(unparsableValue);
                if (result.isError()) {
                    fallbackParserErrorMessage = result.getMessage()
                            .orElse(null);
                } else {
                    unparsableValue = null;
                    fallbackParserErrorMessage = null;
                    newModelValue = result
                            .getOrThrow(IllegalStateException::new);
                    setPresentationValue(newModelValue);
                }
            }
        } finally {
            isFallbackParserRunning = false;
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
     * Sets the label for the datepicker.
     *
     * @param label
     *            value for the {@code label} property in the datepicker
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * Gets the label of the datepicker.
     *
     * @return the {@code label} property of the datePicker
     */
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Date which should be visible when there is no value selected.
     * <p>
     * The same date formats as for the {@code value} property are supported.
     * </p>
     *
     * @param initialPosition
     *            the LocalDate value to set
     */
    public void setInitialPosition(LocalDate initialPosition) {
        String initialPositionString = FORMATTER.apply(initialPosition);
        getElement().setProperty("initialPosition",
                initialPositionString == null ? "" : initialPositionString);
    }

    /**
     * Get the visible date when there is no value selected.
     * <p>
     * The same date formats as for the {@code value} property are supported.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code initialPosition} property from the datepicker
     */
    public LocalDate getInitialPosition() {
        return PARSER.apply(getElement().getProperty("initialPosition"));
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
     * @see DatePickerI18n#setRequiredErrorMessage(String)
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
     * Set the week number visible in the DatePicker.
     * <p>
     * Set true to display ISO-8601 week numbers in the calendar.
     * <p>
     * Notice that displaying week numbers is only supported when
     * i18n.firstDayOfWeek is 1 (Monday).
     *
     * @param weekNumbersVisible
     *            the boolean value to set
     */
    public void setWeekNumbersVisible(boolean weekNumbersVisible) {
        getElement().setProperty("showWeekNumbers", weekNumbersVisible);
    }

    /**
     * Get the state of {@code showWeekNumbers} property of the datepicker
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code showWeekNumbers} property from the datepicker
     */
    public boolean isWeekNumbersVisible() {
        return getElement().getProperty("showWeekNumbers", false);
    }

    /**
     * Sets the opened property of the datepicker to open or close its overlay.
     *
     * @param opened
     *            {@code true} to open the datepicker overlay, {@code false} to
     *            close it
     */
    public void setOpened(boolean opened) {
        getElement().setProperty("opened", opened);
    }

    /**
     * Opens the datepicker overlay.
     */
    public void open() {
        setOpened(true);
    }

    /**
     * Closes the datepicker overlay.
     */
    protected void close() {
        setOpened(false);
    }

    /**
     * Gets the states of the drop-down for the datepicker
     * <p>
     * This property is synchronized automatically from client side when an
     * {@code opened-changed} event happens.
     *
     * @return {@code true} if the drop-down is opened, {@code false} otherwise
     */
    @Synchronize(property = "opened", value = "opened-changed")
    public boolean isOpened() {
        return getElement().getProperty("opened", false);
    }

    /**
     * Sets the name of the DatePicker.
     *
     * @param name
     *            the string value to set
     */
    public void setName(String name) {
        getElement().setProperty("name", name == null ? "" : name);
    }

    /**
     * Gets the name of the DatePicker.
     *
     * @return the {@code name} property from the DatePicker
     */
    public String getName() {
        return getElement().getProperty("name");
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
     * {@code opened-changed} event is sent when the overlay opened state
     * changes.
     */
    public static class OpenedChangeEvent extends ComponentEvent<DatePicker> {
        private final boolean opened;

        public OpenedChangeEvent(DatePicker source, boolean fromClient) {
            super(source, fromClient);
            this.opened = source.isOpened();
        }

        public boolean isOpened() {
            return opened;
        }
    }

    /**
     * Adds a listener for {@code opened-changed} events fired by the
     * webcomponent.
     *
     * @param listener
     *            the listener
     * @return a {@link Registration} for removing the event listener
     */
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent> listener) {
        return addListener(OpenedChangeEvent.class, listener);
    }

    /**
     * {@code invalid-changed} event is sent when the invalid state changes.
     */
    public static class InvalidChangeEvent extends ComponentEvent<DatePicker> {
        private final boolean invalid;

        public InvalidChangeEvent(DatePicker source, boolean fromClient) {
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

    private String getI18nErrorMessage(
            Function<DatePickerI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link DatePicker}.
     */
    public static class DatePickerI18n implements Serializable {
        private List<String> monthNames;
        private List<String> weekdays;
        private List<String> weekdaysShort;
        private List<String> dateFormats;
        private int firstDayOfWeek;
        private String today;
        private String cancel;
        private LocalDate referenceDate;
        private String badInputErrorMessage;
        private String requiredErrorMessage;
        private String minErrorMessage;
        private String maxErrorMessage;

        /**
         * Gets the name of the months.
         *
         * @return the month names
         */
        public List<String> getMonthNames() {
            return monthNames;
        }

        /**
         * Sets the name of the months, starting from January and ending on
         * December.
         *
         * @param monthNames
         *            the month names
         * @return this instance for method chaining
         */
        public DatePickerI18n setMonthNames(List<String> monthNames) {
            this.monthNames = monthNames;
            return this;
        }

        /**
         * Gets the name of the week days.
         *
         * @return the week days
         */
        public List<String> getWeekdays() {
            return weekdays;
        }

        /**
         * Sets the name of the week days, starting from {@code Sunday} and
         * ending on {@code Saturday}.
         *
         * @param weekdays
         *            the week days names
         * @return this instance for method chaining
         */
        public DatePickerI18n setWeekdays(List<String> weekdays) {
            if (weekdays != null && weekdays.size() != 7) {
                LoggerFactory.getLogger(getClass()).warn(String.format(
                        "setWeekdays parameter list should have exactly 7 elements. Instead got %d",
                        weekdays.size()));
            }

            this.weekdays = weekdays;
            return this;
        }

        /**
         * Gets the short names of the week days.
         *
         * @return the short names of the week days
         */
        public List<String> getWeekdaysShort() {
            return weekdaysShort;
        }

        /**
         * Sets the short names of the week days, starting from {@code sun} and
         * ending on {@code sat}.
         *
         * @param weekdaysShort
         *            the short names of the week days
         * @return this instance for method chaining
         */
        public DatePickerI18n setWeekdaysShort(List<String> weekdaysShort) {
            if (weekdaysShort != null && weekdaysShort.size() != 7) {
                LoggerFactory.getLogger(getClass()).warn(String.format(
                        "setWeekdaysShort parameter list should have exactly 7 elements. Instead got %d",
                        weekdaysShort.size()));
            }

            this.weekdaysShort = weekdaysShort;
            return this;
        }

        /**
         * Get the list of custom date formats that are used for formatting the
         * date displayed in the text field, and for parsing the user input
         *
         * @return list of date patterns or null
         */
        public List<String> getDateFormats() {
            return dateFormats;
        }

        /**
         * Sets a custom date format to be used by the date picker. The format
         * is used for formatting the date displayed in the text field, and for
         * parsing the user input.
         * <p>
         * The format is a string pattern using specific symbols to specify how
         * and where the day, month and year should be displayed. The following
         * symbols can be used in the pattern:
         * <ul>
         * <li>{@code yy} - 2 digit year
         * <li>{@code yyyy} - 4 digit year
         * <li>{@code M} - Month, as 1 or 2 digits
         * <li>{@code MM} - Month, padded to 2 digits
         * <li>{@code d} - Day-of-month, as 1 or 2 digits
         * <li>{@code dd} - Day-of-month, padded to 2 digits
         * </ul>
         * <p>
         * For example {@code dd/MM/yyyy}, will format the 20th of June 2021 as
         * {@code 20/06/2021}.
         * <p>
         * Using a custom date format overrides the locale set in the date
         * picker.
         * <p>
         * Setting the format to null will revert the date picker to use the
         * locale for formatting and parsing dates.
         *
         * @param dateFormat
         *            A string with a date format pattern, or null to remove the
         *            previous custom format
         * @return this instance for method chaining
         */
        public DatePickerI18n setDateFormat(String dateFormat) {
            this.setDateFormats(dateFormat);
            return this;
        }

        /**
         * Sets custom date formats to be used by the date picker. The primary
         * format is used for formatting the date displayed in the text field,
         * and for parsing the user input. Additional parsing formats can be
         * specified to support entering dates in multiple formats. The date
         * picker will first attempt to parse the user input using the primary
         * format. If parsing with the primary format fails, it will attempt to
         * parse the input using the additional formats in the order that they
         * were specified. The additional parsing formats are never used for
         * formatting the date. After entering a date using one of the
         * additional parsing formats, it will still be displayed using the
         * primary format.
         * <p>
         * See {@link DatePickerI18n#setDateFormat(String)} on how date patterns
         * are structured.
         * <p>
         * Using custom date formats overrides the locale set in the date
         * picker.
         * <p>
         * Setting the primary format to null will revert the date picker to use
         * the locale for formatting and parsing dates.
         *
         * @param primaryFormat
         *            A string with a date format pattern, or null to remove the
         *            previous custom format
         * @param additionalParsingFormats
         *            Additional date format patterns to be used for parsing
         * @return this instance for method chaining
         */
        public DatePickerI18n setDateFormats(String primaryFormat,
                String... additionalParsingFormats) {
            Objects.requireNonNull(additionalParsingFormats,
                    "Additional parsing formats must not be null");

            if (primaryFormat == null) {
                this.dateFormats = null;
            } else {
                this.dateFormats = new ArrayList<>();
                this.dateFormats.add(primaryFormat);
                this.dateFormats.addAll(Stream.of(additionalParsingFormats)
                        .filter(Objects::nonNull).collect(Collectors.toList()));
            }

            return this;
        }

        /**
         * Gets the first day of the week.
         * <p>
         * 0 for Sunday, 1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for
         * Thursday, 5 for Friday, 6 for Saturday.
         *
         * @return the index of the first day of the week
         */
        public int getFirstDayOfWeek() {
            return firstDayOfWeek;
        }

        /**
         * Sets the first day of the week.
         * <p>
         * 0 for Sunday, 1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for
         * Thursday, 5 for Friday, 6 for Saturday.
         *
         * @param firstDayOfWeek
         *            the index of the first day of the week
         * @return this instance for method chaining
         * @throws IllegalArgumentException
         *             if firstDayOfWeek is invalid
         */
        public DatePickerI18n setFirstDayOfWeek(int firstDayOfWeek) {
            if (firstDayOfWeek < 0 || firstDayOfWeek > 6) {
                throw new IllegalArgumentException(
                        "First day of the week needs to be in range of 0 to 6.");
            }
            this.firstDayOfWeek = firstDayOfWeek;
            return this;
        }

        /**
         * Gets the translated word for {@code today}.
         *
         * @return the translated word for today
         */
        public String getToday() {
            return today;
        }

        /**
         * Sets the translated word for {@code today}.
         *
         * @param today
         *            the translated word for today
         * @return this instance for method chaining
         */
        public DatePickerI18n setToday(String today) {
            this.today = today;
            return this;
        }

        /**
         * Gets the translated word for {@code cancel}.
         *
         * @return the translated word for cancel
         */
        public String getCancel() {
            return cancel;
        }

        /**
         * Sets the translated word for {@code cancel}.
         *
         * @param cancel
         *            the translated word for cancel
         * @return this instance for method chaining
         */
        public DatePickerI18n setCancel(String cancel) {
            this.cancel = cancel;
            return this;
        }

        /**
         * Gets the {@code referenceDate}.
         *
         * @return the reference date
         */
        public LocalDate getReferenceDate() {
            return referenceDate;
        }

        /**
         * Sets the {@code referenceDate}.
         *
         * The reference date is used to determine the century when parsing
         * two-digit years. The century that makes the date closest to the
         * reference date is applied. The default value is the current date.
         *
         * Example: for a reference date of 1970-10-30; years {10, 40, 80}
         * become {2010, 1940, 1980}.
         *
         * @param referenceDate
         *            the date used to base relative dates on
         * @return this instance for method chaining
         */
        public DatePickerI18n setReferenceDate(LocalDate referenceDate) {
            this.referenceDate = referenceDate;
            return this;
        }

        /**
         * Gets the error message displayed when the field contains user input
         * that the server is unable to convert to type {@link LocalDate}.
         *
         * @return the error message or {@code null} if not set
         */
        public String getBadInputErrorMessage() {
            return badInputErrorMessage;
        }

        /**
         * Sets the error message to display when the field contains user input
         * that the server is unable to convert to type {@link LocalDate}.
         * <p>
         * Note, custom error messages set with
         * {@link DatePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public DatePickerI18n setBadInputErrorMessage(String errorMessage) {
            badInputErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see DatePicker#isRequiredIndicatorVisible()
         * @see DatePicker#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link DatePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see DatePicker#isRequiredIndicatorVisible()
         * @see DatePicker#setRequiredIndicatorVisible(boolean)
         */
        public DatePickerI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the selected date is earlier
         * than the minimum allowed date.
         *
         * @return the error message or {@code null} if not set
         * @see DatePicker#getMin()
         * @see DatePicker#setMin(LocalDate)
         */
        public String getMinErrorMessage() {
            return minErrorMessage;
        }

        /**
         * Sets the error message to display when the selected date is earlier
         * than the minimum allowed date.
         * <p>
         * Note, custom error messages set with
         * {@link DatePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see DatePicker#getMin()
         * @see DatePicker#setMin(LocalDate)
         */
        public DatePickerI18n setMinErrorMessage(String errorMessage) {
            minErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the selected date is later than
         * the maximum allowed date.
         *
         * @return the error message or {@code null} if not set
         * @see DatePicker#getMax()
         * @see DatePicker#setMax(LocalDate)
         */
        public String getMaxErrorMessage() {
            return maxErrorMessage;
        }

        /**
         * Sets the error message to display when the selected date is later
         * than the maximum allowed date.
         * <p>
         * Note, custom error messages set with
         * {@link DatePicker#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see DatePicker#getMax()
         * @see DatePicker#setMax(LocalDate)
         */
        public DatePickerI18n setMaxErrorMessage(String errorMessage) {
            maxErrorMessage = errorMessage;
            return this;
        }
    }
}
