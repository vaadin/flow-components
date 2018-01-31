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

import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Server-side component for the {@code vaadin-password-field} element.
 *
 * @author Vaadin Ltd.
 */
public class PasswordField extends GeneratedVaadinPasswordField<PasswordField>
        implements HasSize, HasValidation,
        HasValueChangeMode<PasswordField, String> {
    private ValueChangeMode currentMode;

    /**
     * Constructs an empty {@code PasswordField}.
     */
    public PasswordField() {
        super.setValue(getEmptyValue());
        setValueChangeMode(ValueChangeMode.ON_BLUR);
    }

    /**
     * Constructs an empty {@code PasswordField} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public PasswordField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code PasswordField} with the given label and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public PasswordField(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code PasswordField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public PasswordField(ValueChangeListener<PasswordField, String> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code PasswordField} with a value change listener
     * and a label.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     *
     * @see #setLabel(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public PasswordField(String label,
            ValueChangeListener<PasswordField, String> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a {@code PasswordField} with a value change listener, a label
     * and an initial value.
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
    public PasswordField(String label, String initialValue,
            ValueChangeListener<PasswordField, String> listener) {
        this(label);
        setValue(initialValue);
        addValueChangeListener(listener);
    }

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
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * String used for the label element.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
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
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * A hint to the user of what can be entered in the control.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
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
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specify that this control should have input focus when the page loads.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code autofocus} property from the webcomponent
     */
    public boolean isAutofocus() {
        return isAutofocusBoolean();
    }

    /**
     * <p>
     * If false, the user cannot interact with this element.
     * </p>
     *
     * @param enabled
     *            the boolean value to set
     */
    public void setEnabled(boolean enabled) {
        setDisabled(!enabled);
    }

    /**
     * <p>
     * If false, the user cannot interact with this element.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
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
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Whether the value of the control can be automatically completed by the
     * browser. List of available options at:
     * https://developer.mozilla.org/en/docs
     * /Web/HTML/Element/input#attr-autocomplete
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code autocomplete} property from the webcomponent
     */
    public String getAutocomplete() {
        return getAutocompleteString();
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     * </p>
     *
     * @param maxlength
     *            the double value to set
     */
    public void setMaxlength(int maxlength) {
        super.setMaxlength(maxlength);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code maxlength} property from the webcomponent
     */
    public int getMaxlength() {
        return (int) getMaxlengthDouble();
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     * </p>
     *
     * @param minlength
     *            the double value to set
     */
    public void setMinlength(int minlength) {
        super.setMinlength(minlength);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code minlength} property from the webcomponent
     */
    public int getMinlength() {
        return (int) getMinlengthDouble();
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Specifies that the user must fill in a value.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
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
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * When set to true, user is prevented from typing a value that conflicts
     * with the given {@code pattern}.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
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
    public void setAutocorrect(String autocorrect) {
        super.setAutocorrect(autocorrect);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * This is a property supported by Safari that is used to control whether
     * autocorrection should be enabled when the user is entering/editing the
     * text. Possible values are: on: Enable autocorrection. off: Disable
     * autocorrection.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code autocorrect} property from the webcomponent
     */
    public String getAutocorrect() {
        return getAutocorrectString();
    }

    @Override
    public void setPattern(String pattern) {
        super.setPattern(pattern);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * A regular expression that the value is checked against. The pattern must
     * match the entire value, not just some subset.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code pattern} property from the webcomponent
     */
    public String getPattern() {
        return getPatternString();
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Message to show to the user when validation fails.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
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

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to hide the eye icon which toggles the password visibility.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code revealButtonHidden} property from the webcomponent
     */
    public boolean isRevealButtonHidden() {
        return super.isRevealButtonHiddenBoolean();
    }

    @Override
    public void setRevealButtonHidden(boolean revealButtonHidden) {
        super.setRevealButtonHidden(revealButtonHidden);
    }

    /**
     * <p>
     * Description copied from corresponding location in WebComponent:
     * </p>
     * <p>
     * Set to true to hide the eye icon which toggles the password visibility.
     * <p>
     * This property is not synchronized automatically from the client side, so
     * the returned value may not be the same as in client side.
     * </p>
     *
     * @return the {@code revealButtonHidden} property from the webcomponent
     */
    public boolean isPasswordVisible() {
        return super.isPasswordVisibleBoolean();
    }

    @Override
    public String getEmptyValue() {
        return "";
    }
}
