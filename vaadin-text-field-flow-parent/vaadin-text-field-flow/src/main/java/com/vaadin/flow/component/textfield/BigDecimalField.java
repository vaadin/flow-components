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
package com.vaadin.flow.component.textfield;

import java.math.BigDecimal;
import java.util.Objects;

import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializableFunction;

/**
 * Server-side component for the {@code vaadin-big-decimal-field} element. This
 * field uses {@link BigDecimal} as the server-side value type, which allows
 * handling decimal numbers with high precision. The component also prevents
 * users from entering characters which can't be used in a decimal number, such
 * as alphabets.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-big-decimal-field")
@JavaScript("frontend://vaadin-big-decimal-field.js")
@JsModule("./vaadin-big-decimal-field.js")
public class BigDecimalField
        extends GeneratedVaadinTextField<BigDecimalField, BigDecimal>
        implements HasSize, HasValidation, HasValueChangeMode,
        HasPrefixAndSuffix, InputNotifier, KeyNotifier, CompositionNotifier,
        HasAutocomplete, HasAutocapitalize, HasAutocorrect {
    private ValueChangeMode currentMode;

    private boolean isConnectorAttached;

    private int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

    private boolean required;

    private static final SerializableFunction<String, BigDecimal> PARSER = valueFromClient -> {
        if (valueFromClient == null || valueFromClient.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(valueFromClient);
        } catch (NumberFormatException e) {
            return null;
        }
    };

    private static final SerializableFunction<BigDecimal, String> FORMATTER = valueFromModel -> valueFromModel == null
            ? ""
            : valueFromModel.toPlainString();

    /**
     * Constructs an empty {@code BigDecimalField}.
     */
    public BigDecimalField() {
        super(null, null, String.class, PARSER, FORMATTER);
        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addInvalidChangeListener(e -> {
            // If invalid is updated from client to false, check it
            if (e.isFromClient() && !e.isInvalid()) {
                validate();
            }
        });
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

    /**
     * Gets the visibility state of the button which clears the text field.
     *
     * @return <code>true</code> if the button is visible, <code>false</code>
     *         otherwise
     */
    public boolean isClearButtonVisible() {
        return isClearButtonVisibleBoolean();
    }

    /**
     * Set to <code>false</code> to hide the clear button which clears the text
     * field.
     *
     * @param clearButtonVisible
     *            <code>true</code> to set the button visible,
     *            <code>false</code> otherwise
     */
    @Override
    public void setClearButtonVisible(boolean clearButtonVisible) {
        super.setClearButtonVisible(clearButtonVisible);
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
     * Message to show to the user when validation fails.
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
     * Sets the value of this text field. If the new value is not equal to
     * {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link com.vaadin.flow.data.binder.Binder} will take care of the
     * {@code null} conversion when integrates with text field, as long as no
     * new converter is defined.
     *
     * @param value
     *            the new value, not {@code null}
     */
    @Override
    public void setValue(BigDecimal value) {
        super.setValue(value);
    }

    /**
     * Returns the current value of the text field. By default, the empty text
     * field will return an empty string.
     *
     * @return the current value.
     */
    @Override
    public BigDecimal getValue() {
        return super.getValue();
    }

    @Override
    protected void setModelValue(BigDecimal newModelValue, boolean fromClient) {
        super.setModelValue(newModelValue, fromClient);
        validate();
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
        if (!isConnectorAttached) {
            RequiredValidationUtil.attachConnector(this);
            isConnectorAttached = true;
        }
        RequiredValidationUtil.updateClientValidation(requiredIndicatorVisible,
                this);
        this.required = requiredIndicatorVisible;
    }
}
