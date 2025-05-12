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
package com.vaadin.flow.component.textfield;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiFunction;
import com.vaadin.flow.shared.Registration;

/**
 * BigDecimalField is an input field for handling decimal numbers with high
 * precision. This field uses {@link BigDecimal} as the server-side value type,
 * and only allows numeric input.
 * <p>
 * When setting values from the server-side, the {@code scale} of the provided
 * {@link BigDecimal} is preserved in the presentation format shown to the user,
 * as described in {@link #setValue(BigDecimal)}.
 * <h2>Validation</h2>
 * <p>
 * BigDecimalField comes with a built-in validation mechanism that verifies that
 * the value is parsable into {@link BigDecimal} and is not empty when the
 * {@link #setRequiredIndicatorVisible(boolean) required constraint} is enabled.
 * If validation fails, the component is marked as invalid and an error message
 * is displayed below the input.
 * <p>
 * Validation is triggered whenever the user applies an input change, for
 * example by pressing Enter or blurring the field. Programmatic value changes
 * trigger validation as well. In eager and lazy value change modes, validation
 * is also triggered on every character press with a delay according to the
 * selected mode.
 * <p>
 * Error messages for unparsable input and required constraint can be configured
 * with the {@link BigDecimalFieldI18n} object, using the respective properties.
 * If you want to provide a single catch-all error message, you can also use the
 * {@link #setErrorMessage(String)} method. Note that such an error message will
 * take priority over i18n error messages if both are set.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. By default, before running custom validators, Binder will
 * also check if the value is parsable and display an error message from the
 * {@link BigDecimalFieldI18n} object. For the required constraint, Binder
 * provides its own API, see {@link Binder.BindingBuilder#asRequired(String)
 * asRequired()}.
 * <p>
 * However, if Binder doesn't fit your needs and you want to implement fully
 * custom validation logic, you can disable the constraint validation by setting
 * {@link #setManualValidation(boolean)} to true. This will allow you to control
 * the invalid state and the error message manually using
 * {@link #setInvalid(boolean)} and {@link #setErrorMessage(String)} API.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-big-decimal-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./vaadin-big-decimal-field.js")
@Uses(TextField.class)
public class BigDecimalField extends TextFieldBase<BigDecimalField, BigDecimal>
        implements HasThemeVariant<TextFieldVariant> {

    private BigDecimalFieldI18n i18n;

    private Locale locale;

    private static final SerializableBiFunction<BigDecimalField, String, BigDecimal> PARSER = (
            field, valueFromClient) -> {
        if (valueFromClient == null || valueFromClient.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(
                    valueFromClient.replace(field.getDecimalSeparator(), '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    };

    private static final SerializableBiFunction<BigDecimalField, BigDecimal, String> FORMATTER = (
            field, valueFromModel) -> valueFromModel == null ? ""
                    : valueFromModel.toPlainString().replace('.',
                            field.getDecimalSeparator());

    private final CopyOnWriteArrayList<ValidationStatusChangeListener<BigDecimal>> validationStatusChangeListeners = new CopyOnWriteArrayList<>();

    private Validator<BigDecimal> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        boolean hasBadInput = valueEquals(value, getEmptyValue())
                && !getInputElementValue().isEmpty();
        if (hasBadInput) {
            return ValidationResult.error(getI18nErrorMessage(
                    BigDecimalFieldI18n::getBadInputErrorMessage));
        }

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(getI18nErrorMessage(
                            BigDecimalFieldI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        return ValidationResult.ok();
    };

    private ValidationController<BigDecimalField, BigDecimal> validationController = new ValidationController<>(
            this);

    /**
     * Constructs an empty {@code BigDecimalField}.
     */
    public BigDecimalField() {
        super(null, null, String.class, PARSER, FORMATTER, true);

        setLocale(Optional.ofNullable(UI.getCurrent()).map(UI::getLocale)
                .orElse(Locale.ROOT));

        getElement().setProperty("manualValidation", true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());
    }

    /**
     * Constructs an empty {@code BigDecimalField} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public BigDecimalField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code BigDecimalField} with the given label and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public BigDecimalField(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs a {@code BigDecimalField} with the given label, an initial
     * value and placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param initialValue
     *            the initial value
     * @param placeholder
     *            the placeholder text to set
     * @see #setValue(Object)
     * @see #setPlaceholder(String)
     */
    public BigDecimalField(String label, BigDecimal initialValue,
            String placeholder) {
        this(label);
        setValue(initialValue);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code BigDecimalField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public BigDecimalField(
            ValueChangeListener<? super ComponentValueChangeEvent<BigDecimalField, BigDecimal>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code BigDecimalField} with a label and a value
     * change listener.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     * @see #setLabel(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public BigDecimalField(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<BigDecimalField, BigDecimal>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code BigDecimalField} with a label,a value change
     * listener and an initial value.
     *
     * @param label
     *            the text to set as the label
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener
     * @see #setLabel(String)
     * @see #setValue(Object)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public BigDecimalField(String label, BigDecimal initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<BigDecimalField, BigDecimal>> listener) {
        this(label);
        setValue(initialValue);
        addValueChangeListener(listener);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Distinct error messages for unparsable input and required constraint can
     * be configured with the {@link BigDecimalFieldI18n} object, using the
     * respective properties. However, note that the error message set with
     * {@link #setErrorMessage(String)} will take priority and override any i18n
     * error messages if both are set.
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * @see BigDecimalFieldI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    @Override
    public BigDecimal getEmptyValue() {
        return null;
    }

    /**
     * Sets the value of this field. If the new value is not equal to
     * {@code getValue()}, fires a value change event.
     * <p>
     * You can adjust how the value is presented in the field with the APIs
     * provided by the value type {@link BigDecimal}. For example, you can
     * change the number of decimal places with
     * {@link BigDecimal#setScale(int)}. This doesn't however restrict the user
     * from entering values with different number of decimals. Note that
     * BigDecimals are immutable, so their methods will return new instances
     * instead of editing the existing ones. Scientific notation (such as 1e9)
     * is turned into plain number format for the presentation.
     *
     * @param value
     *            the new value
     */
    @Override
    public void setValue(BigDecimal value) {
        BigDecimal oldValue = getValue();
        if (oldValue == null && value == null
                && !getInputElementValue().isEmpty()) {
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
    protected void setModelValue(BigDecimal newModelValue, boolean fromClient) {
        BigDecimal oldModelValue = getValue();
        boolean isModelValueRemainedEmpty = oldModelValue == null
                && newModelValue == null;

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
                && !getInputElementValue().isEmpty()) {
            setInputElementValue("");
            validate();
            fireValidationStatusChangeEvent();
            return;
        }

        super.setModelValue(newModelValue, fromClient);
    }

    /**
     * Returns the current value of the field. By default, the empty
     * BigDecimalField will return {@code null}.
     *
     * @return the current value.
     */
    @Override
    public BigDecimal getValue() {
        return super.getValue();
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
    public Validator<BigDecimal> getDefaultValidator() {
        return defaultValidator;
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<BigDecimal> listener) {
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
        ValidationStatusChangeEvent<BigDecimal> event = new ValidationStatusChangeEvent<>(
                this, !isInvalid());
        validationStatusChangeListeners
                .forEach(listener -> listener.validationStatusChanged(event));
    }

    private String getInputElementValue() {
        return getElement().getProperty("value", "");
    }

    private void setInputElementValue(String value) {
        getElement().setProperty("value", value);
    }

    /**
     * Sets the locale for this BigDecimalField. It is used to determine which
     * decimal separator (the radix point) should be used.
     *
     * @param locale
     *            the locale to set, not {@code null}
     */
    public void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale to set can't be null.");
        this.locale = locale;

        setDecimalSeparator(
                new DecimalFormatSymbols(locale).getDecimalSeparator());
    }

    /**
     * Gets the locale used by this BigDecimalField. It is used to determine
     * which decimal separator (the radix point) should be used.
     *
     * @return the locale of this field, never {@code null}
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Updates two things at client-side: changes the decimal separator in the
     * current input value, and updates the invalid input prevention to accept
     * the new decimal separator.
     */
    private void setDecimalSeparator(char decimalSeparator) {
        getElement().setProperty("_decimalSeparator", decimalSeparator + "");
    }

    private char getDecimalSeparator() {
        String prop = getElement().getProperty("_decimalSeparator");
        return prop == null || prop.isEmpty() ? '.'
                : getElement().getProperty("_decimalSeparator").charAt(0);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(BigDecimalFieldI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public BigDecimalFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(BigDecimalFieldI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<BigDecimalFieldI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link BigDecimalField}.
     */
    public static class BigDecimalFieldI18n implements Serializable {

        private String requiredErrorMessage;
        private String badInputErrorMessage;

        /**
         * Gets the error message displayed when the field contains user input
         * that the server is unable to convert to type {@link BigDecimal}.
         *
         * @return the error message or {@code null} if not set
         */
        public String getBadInputErrorMessage() {
            return badInputErrorMessage;
        }

        /**
         * Sets the error message to display when the field contains user input
         * that the server is unable to convert to type {@link BigDecimal}.
         * <p>
         * Note, custom error messages set with
         * {@link BigDecimalField#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message to set, or {@code null} to clear
         * @return this instance for method chaining
         */
        public BigDecimalFieldI18n setBadInputErrorMessage(
                String errorMessage) {
            badInputErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see BigDecimalField#isRequiredIndicatorVisible()
         * @see BigDecimalField#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link BigDecimalField#setErrorMessage(String)} take priority over
         * i18n error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see BigDecimalField#isRequiredIndicatorVisible()
         * @see BigDecimalField#setRequiredIndicatorVisible(boolean)
         */
        public BigDecimalFieldI18n setRequiredErrorMessage(
                String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }
    }
}
