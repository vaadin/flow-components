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
package com.vaadin.flow.component.textfield;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.HasClearButton;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableBiFunction;

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
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.0-beta1")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./vaadin-big-decimal-field.js")
public class BigDecimalField
        extends GeneratedVaadinTextField<BigDecimalField, BigDecimal> implements
        HasSize, HasValidation, HasValueChangeMode, HasPrefixAndSuffix,
        InputNotifier, KeyNotifier, CompositionNotifier, HasAutocomplete,
        HasAutocapitalize, HasAutocorrect, HasHelper, HasLabel, HasClearButton {
    private ValueChangeMode currentMode;

    private boolean isConnectorAttached;

    private int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

    private boolean required;

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
     * The default value is {@link ValueChangeMode#ON_CHANGE}.
     */
    @Override
    public ValueChangeMode getValueChangeMode() {
        return currentMode;
    }

    @Override
    public void setValueChangeMode(ValueChangeMode valueChangeMode) {
        currentMode = valueChangeMode;
        setSynchronizedEvent(
                ValueChangeMode.eventForMode(valueChangeMode, "value-changed"));
        applyChangeTimeout();
    }

    @Override
    public void setValueChangeTimeout(int valueChangeTimeout) {
        this.valueChangeTimeout = valueChangeTimeout;
        applyChangeTimeout();
    }

    @Override
    public int getValueChangeTimeout() {
        return valueChangeTimeout;
    }

    private void applyChangeTimeout() {
        ValueChangeMode.applyChangeTimeout(getValueChangeMode(),
                getValueChangeTimeout(), getSynchronizationRegistration());
    }

    @Override
    public String getErrorMessage() {
        return super.getErrorMessageString();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    @Override
    public boolean isInvalid() {
        return isInvalidBoolean();
    }

    @Override
    public void setInvalid(boolean invalid) {
        super.setInvalid(invalid);
    }

    @Override
    public void setLabel(String label) {
        super.setLabel(label);
    }

    /**
     * String used for the label element.
     *
     * @return the {@code label} property from the webcomponent
     */
    @Override
    public String getLabel() {
        return getLabelString();
    }

    @Override
    public void setPlaceholder(String placeholder) {
        super.setPlaceholder(placeholder);
    }

    /**
     * A hint to the user of what can be entered in the component.
     *
     * @return the {@code placeholder} property from the webcomponent
     */
    public String getPlaceholder() {
        return getPlaceholderString();
    }

    /**
     * Specifies if the field value gets automatically selected when the field
     * gains focus.
     *
     * @return <code>true</code> if autoselect is active, <code>false</code>
     *         otherwise
     */
    public boolean isAutoselect() {
        return super.isAutoselectBoolean();
    }

    /**
     * Set to <code>true</code> to always have the field value automatically
     * selected when the field gains focus, <code>false</code> otherwise.
     *
     * @param autoselect
     *            <code>true</code> to set auto select on, <code>false</code>
     *            otherwise
     */
    @Override
    public void setAutoselect(boolean autoselect) {
        super.setAutoselect(autoselect);
    }

    @Override
    public void setAutofocus(boolean autofocus) {
        super.setAutofocus(autofocus);
    }

    /**
     * Specify that this control should have input focus when the page loads.
     *
     * @return the {@code autofocus} property from the webcomponent
     */
    public boolean isAutofocus() {
        return isAutofocusBoolean();
    }

    /**
     * The text usually displayed in a tooltip popup when the mouse is over the
     * field.
     *
     * @return the {@code title} property from the webcomponent
     */
    public String getTitle() {
        return super.getTitleString();
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
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
        super.setValue(value);
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
    @Override
    protected void validate() {
        final boolean isRequiredButEmpty = required
                && Objects.equals(getEmptyValue(), getValue());
        setInvalid(isRequiredButEmpty);
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        this.required = requiredIndicatorVisible;
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
        FieldValidationUtil.disableClientValidation(this);
    }
}
