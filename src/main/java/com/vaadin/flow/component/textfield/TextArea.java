/*
 * Copyright 2000-2017 Vaadin Ltd.
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

import java.util.Objects;

import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Server-side component for the {@code vaadin-text-area} element.
 *
 * @author Vaadin Ltd.
 */
public class TextArea extends GeneratedVaadinTextArea<TextArea>
        implements HasSize, HasValidation, HasValueChangeMode<TextArea, String>,
        HasPrefixAndSuffix, InputNotifier, KeyNotifier, CompositionNotifier {
    private ValueChangeMode currentMode;

    /**
     * Constructs an empty {@code TextArea}.
     */
    public TextArea() {
        super.setValue(getEmptyValue());
        setValueChangeMode(ValueChangeMode.ON_CHANGE);
    }

    /**
     * Constructs an empty {@code TextArea} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public TextArea(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code TextArea} with the given label and placeholder
     * text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public TextArea(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs a {@code TextArea} with the given label, an initial value and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param initialValue
     *            the initial value
     * @param placeholder
     *            the placeholder text to set
     *
     * @see #setValue(String)
     * @see #setPlaceholder(String)
     */
    public TextArea(String label, String initialValue, String placeholder) {
        this(label);
        setValue(initialValue);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code TextArea} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextArea(ValueChangeListener<TextArea, String> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code TextArea} with a label and a value change
     * listener.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     * @see #setLabel(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextArea(String label,
            ValueChangeListener<TextArea, String> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code TextArea} with a label, a value change
     * listener and an initial value.
     *
     * @param label
     *            the text to set as the label
     * @param initialValue
     *            the initial value
     * @param listener
     *            the value change listener
     *
     * @see #setLabel(String)
     * @see #setValue(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextArea(String label, String initialValue,
            ValueChangeListener<TextArea, String> listener) {
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
        HasValueChangeMode.super.setValueChangeMode(valueChangeMode);
    }

    @Override
    public String getValue() {
        String value = super.getValueString();
        return value == null ? getEmptyValue() : value;
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
    public void setValue(String value) {
        Objects.requireNonNull(value, "value cannot be null");
        if (!Objects.equals(value, getValue())) {
            super.setValue(value);
        }
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
     * If <code>false</code>, the user cannot interact with this element.
     *
     * @param enabled
     *            the boolean value to set
     */
    public void setEnabled(boolean enabled) {
        setDisabled(!enabled);
    }

    /**
     * If <code>false</code>, the user cannot interact with this element.
     *
     * @return the {@code disabled} property negation from the webcomponent
     */
    public boolean isEnabled() {
        return !isDisabledBoolean();
    }

    @Override
    public void setAutocomplete(String autocomplete) {
        super.setAutocomplete(autocomplete);
    }

    /**
     * Whether the value of the control can be automatically completed by the
     * browser. List of available options at:
     * https://developer.mozilla.org/en/docs
     * /Web/HTML/Element/input#attr-autocomplete
     *
     * @return the {@code autocomplete} property from the webcomponent
     */
    public String getAutocomplete() {
        return getAutocompleteString();
    }

    /**
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @param maxLength
     *            the maximum length
     */
    public void setMaxLength(int maxLength) {
        super.setMaxlength(maxLength);
    }

    /**
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @return the {@code maxlength} property from the webcomponent
     */
    public int getMaxLength() {
        return (int) getMaxlengthDouble();
    }

    /**
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @param minLength
     *            the minimum length
     */
    public void setMinLength(int minLength) {
        super.setMinlength(minLength);
    }

    /**
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @return the {@code minlength} property from the webcomponent
     */
    public int getMinLength() {
        return (int) getMinlengthDouble();
    }

    /**
     * Specifies that the user must fill in a value.
     *
     * @return the {@code required} property from the webcomponent
     */
    public boolean isRequired() {
        return isRequiredBoolean();
    }

    @Override
    public void setRequired(boolean required) {
        super.setRequired(required);
    }

    /**
     * When set to <code>true</code>, user is prevented from typing a value that
     * conflicts with the given {@code pattern}.
     * 
     * @return the {@code preventInvalidInput} property from the webcomponent
     */
    public boolean isPreventInvalidInput() {
        return isPreventInvalidInputBoolean();
    }

    @Override
    public void setPreventInvalidInput(boolean preventInvalidInput) {
        super.setPreventInvalidInput(preventInvalidInput);
    }

    @Override
    public String getEmptyValue() {
        return "";
    }
}
