/*
 * Copyright 2000-2023 Vaadin Ltd.
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

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.ValidationUtil;
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
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-big-decimal-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.1.14")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./vaadin-big-decimal-field.js")
@Uses(TextField.class)
public class BigDecimalField extends TextFieldBase<BigDecimalField, BigDecimal>
        implements HasThemeVariant<TextFieldVariant> {

    private boolean isConnectorAttached;

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

    /**
     * Constructs an empty {@code BigDecimalField}.
     */
    public BigDecimalField() {
        super(null, null, String.class, PARSER, FORMATTER, true);

        setLocale(Optional.ofNullable(UI.getCurrent()).map(UI::getLocale)
                .orElse(Locale.ROOT));

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());

        addClientValidatedEventListener(e -> validate());
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

        super.setValue(value);

        if (Objects.equals(oldValue, getEmptyValue())
                && Objects.equals(value, getEmptyValue())
                && isInputValuePresent()) {
            // Clear the input element from possible bad input.
            getElement().executeJs("this._inputElementValue = ''");
            getElement().setProperty("_hasInputValue", false);
            fireEvent(new ClientValidatedEvent(this, false));
        } else {
            // Restore the input element's value in case it was cleared
            // in the above branch. That can happen when setValue(null)
            // and setValue(...) are subsequently called within one round-trip
            // and there was bad input.
            getElement().executeJs("this._inputElementValue = this.value");
        }
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

    /**
     * Performs server-side validation of the current value. This is needed
     * because it is possible to circumvent the client-side validation
     * constraints using browser development tools.
     */
    protected void validate() {
        BigDecimal value = getValue();

        boolean isRequired = isRequiredIndicatorVisible();
        ValidationResult requiredValidation = ValidationUtil
                .checkRequired(isRequired, value, getEmptyValue());

        setInvalid(
                requiredValidation.isError() || checkValidity(value).isError());
    }

    private ValidationResult checkValidity(BigDecimal value) {
        boolean hasNonParsableValue = Objects.equals(value, getEmptyValue())
                && isInputValuePresent();
        if (hasNonParsableValue) {
            return ValidationResult.error("");
        }

        return ValidationResult.ok();
    }

    @Override
    public Validator<BigDecimal> getDefaultValidator() {
        return (value, context) -> checkValidity(value);
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<BigDecimal> listener) {
        return addClientValidatedEventListener(
                event -> listener.validationStatusChanged(
                        new ValidationStatusChangeEvent<BigDecimal>(this,
                                !isInvalid())));
    }

    /**
     * Returns whether the input element has a value or not.
     *
     * @return <code>true</code> if the input element's value is populated,
     *         <code>false</code> otherwise
     */
    @Synchronize(property = "_hasInputValue", value = "has-input-value-changed")
    private boolean isInputValuePresent() {
        return getElement().getProperty("_hasInputValue", false);
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

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }
}
