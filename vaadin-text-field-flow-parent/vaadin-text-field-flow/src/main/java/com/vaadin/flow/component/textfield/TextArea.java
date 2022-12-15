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

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.CompositionNotifier;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasHelper;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.InputNotifier;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasClearButton;
import com.vaadin.flow.component.shared.HasClientValidation;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.HasTooltip;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.HasValidator;
import com.vaadin.flow.data.binder.ValidationStatusChangeEvent;
import com.vaadin.flow.data.binder.ValidationStatusChangeListener;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.HasValueChangeMode;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;

/**
 * Text Area is an input field component for multi-line text input. Text Area is
 * typically used for descriptions, comments, and other longer free-form
 * content.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-text-area")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.0.0-alpha6")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/text-area", version = "24.0.0-alpha6")
@JsModule("@vaadin/text-area/src/vaadin-text-area.js")
public class TextArea extends AbstractSinglePropertyField<TextArea, String>
        implements CompositionNotifier, Focusable<TextArea>,
        HasAllowedCharPattern, HasAutocapitalize, HasAutocomplete,
        HasAutocorrect, HasClearButton, HasClientValidation, HasHelper,
        HasLabel, HasPrefixAndSuffix, HasSize, HasStyle,
        HasThemeVariant<TextAreaVariant>, HasTooltip, HasValidation,
        HasValidator<String>, HasValueChangeMode, InputNotifier, KeyNotifier {
    private ValueChangeMode currentMode;

    private boolean isConnectorAttached;

    private int valueChangeTimeout = DEFAULT_CHANGE_TIMEOUT;

    private TextFieldValidationSupport validationSupport;

    /**
     * Constructs an empty {@code TextArea}.
     */
    public TextArea() {
        super("value", "", false);

        if (getElement().getProperty("value") == null) {
            setPresentationValue("");
        }

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());

        addClientValidatedEventListener(e -> validate());
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
     * @see #setValue(Object)
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
    public TextArea(
            ValueChangeListener<? super ComponentValueChangeEvent<TextArea, String>> listener) {
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
            ValueChangeListener<? super ComponentValueChangeEvent<TextArea, String>> listener) {
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
     * @see #setValue(Object)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextArea(String label, String initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<TextArea, String>> listener) {
        this(label);
        setValue(initialValue);
        addValueChangeListener(listener);
    }

    private TextFieldValidationSupport getValidationSupport() {
        if (validationSupport == null) {
            validationSupport = new TextFieldValidationSupport(this);
        }
        return validationSupport;
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

    /**
     * Gets the current error message from the checkbox group.
     *
     * @return the current error message
     */
    public String getErrorMessage() {
        return getElement().getProperty("errorMessage");
    }

    /**
     * Sets the error message that should be displayed when the component
     * becomes invalid
     *
     * @param errorMessage
     *            the String value to set
     */
    public void setErrorMessage(String errorMessage) {
        getElement().setProperty("errorMessage",
                errorMessage == null ? "" : errorMessage);
    }

    /**
     * Whether the component has an invalid value or not.
     *
     * @return the {@code invalid} property from the field
     */
    public boolean isInvalid() {
        return getElement().getProperty("invalid", false);
    }

    /**
     * Sets whether the component has an invalid value or not.
     *
     *
     * @param invalid
     *            {@code true} for invalid, {@code false} for valid
     */
    public void setInvalid(boolean invalid) {
        getElement().setProperty("invalid", invalid);
    }

    /**
     * Sets the label for the component.
     *
     * @param label
     *            value for the {@code label} property
     */
    public void setLabel(String label) {
        getElement().setProperty("label", label == null ? "" : label);
    }

    /**
     * String used for the label element.
     *
     * @return the {@code label} property from the webcomponent
     */
    public String getLabel() {
        return getElement().getProperty("label");
    }

    /**
     * Sets the placeholder text that should be displayed in the input element,
     * when the user has not entered a value
     *
     * @param placeholder
     *            the placeholder text
     */
    public void setPlaceholder(String placeholder) {
        getElement().setProperty("placeholder",
                placeholder == null ? "" : placeholder);
    }

    /**
     * A hint to the user of what can be entered in the component.
     *
     * @return the {@code placeholder} property from the webcomponent
     */
    public String getPlaceholder() {
        return getElement().getProperty("placeholder");
    }

    /**
     * Specifies if the field value gets automatically selected when the field
     * gains focus.
     *
     * @return <code>true</code> if autoselect is active, <code>false</code>
     *         otherwise
     */
    public boolean isAutoselect() {
        return getElement().getProperty("autoselect", false);
    }

    /**
     * Set to <code>true</code> to always have the field value automatically
     * selected when the field gains focus, <code>false</code> otherwise.
     *
     * @param autoselect
     *            <code>true</code> to set auto select on, <code>false</code>
     *            otherwise
     */
    public void setAutoselect(boolean autoselect) {
        getElement().setProperty("autoselect", autoselect);
    }

    /**
     * Sets the whether the component should automatically receive focus when
     * the page loads. Defaults to {@code false}.
     *
     * @param autofocus
     *            {@code true} component should automatically receive focus
     */
    public void setAutofocus(boolean autofocus) {
        getElement().setProperty("autofocus", autofocus);
    }

    /**
     * Specify that this control should have input focus when the page loads.
     *
     * @return the {@code autofocus} property from the webcomponent
     */
    public boolean isAutofocus() {
        return getElement().getProperty("autofocus", false);
    }

    /**
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @param maxLength
     *            the maximum length
     */
    public void setMaxLength(int maxLength) {
        getElement().setProperty("maxlength", maxLength);
        getValidationSupport().setMaxLength(maxLength);
    }

    /**
     * Maximum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @return the {@code maxlength} property from the webcomponent
     */
    public int getMaxLength() {
        return (int) getElement().getProperty("maxlength", 0.0);
    }

    /**
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @param minLength
     *            the minimum length
     */
    public void setMinLength(int minLength) {
        getElement().setProperty("minlength", minLength);
        getValidationSupport().setMinLength(minLength);
    }

    /**
     * Minimum number of characters (in Unicode code points) that the user can
     * enter.
     *
     * @return the {@code minlength} property from the webcomponent
     */
    public int getMinLength() {
        return (int) getElement().getProperty("minlength", 0.0);
    }

    /**
     * Specifies that the user must fill in a value.
     *
     * @return the {@code required} property from the webcomponent
     */
    public boolean isRequired() {
        return getElement().getProperty("required", false);
    }

    /**
     * Specifies that the user must fill in a value.
     * <p>
     * NOTE: The required indicator will not be visible, if there is no
     * {@code label} property set for the component.
     *
     * @param required
     *            the boolean value to set
     */
    public void setRequired(boolean required) {
        getElement().setProperty("required", required);
        getValidationSupport().setRequired(required);
    }

    /**
     * Sets a regular expression for the value to pass on the client-side. The
     * pattern must be a valid JavaScript Regular Expression that matches the
     * entire value, not just some subset.
     *
     * @param pattern
     *            the new String pattern
     *
     * @see <a href=
     *      "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#htmlattrdefpattern">
     *      https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input#htmlattrdefpattern</>
     * @see <a href=
     *      "https://html.spec.whatwg.org/multipage/input.html#attr-input-pattern">
     *      https://html.spec.whatwg.org/multipage/input.html#attr-input-pattern</>
     */
    public void setPattern(String pattern) {
        getElement().setProperty("pattern", pattern == null ? "" : pattern);
        getValidationSupport().setPattern(pattern);
    }

    /**
     * A regular expression that the value is checked against. The pattern must
     * match the entire value, not just some subset.
     *
     * @return the {@code pattern} property
     */
    public String getPattern() {
        return getElement().getProperty("pattern");
    }

    @Override
    public String getEmptyValue() {
        return "";
    }

    /**
     * Sets the value of this text area. If the new value is not equal to
     * {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with text area, as long as no new converter is defined.
     *
     * @param value
     *            the new value, not {@code null}
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    /**
     * Returns the current value of the text area. By default, the empty text
     * area will return an empty string.
     *
     * @return the current value.
     */
    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
        getValidationSupport().setRequired(requiredIndicatorVisible);
    }

    @Override
    public Validator<String> getDefaultValidator() {
        return (value, context) -> getValidationSupport().checkValidity(value);
    }

    @Override
    public Registration addValidationStatusChangeListener(
            ValidationStatusChangeListener<String> listener) {
        return addClientValidatedEventListener(
                event -> listener.validationStatusChanged(
                        new ValidationStatusChangeEvent<>(this, !isInvalid())));
    }

    /**
     * Performs server-side validation of the current value. This is needed
     * because it is possible to circumvent the client-side validation
     * constraints using browser development tools.
     */
    protected void validate() {
        setInvalid(getValidationSupport().isInvalid(getValue()));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }

    // Override is only required to keep binary compatibility with other 23.x
    // minor versions, can be removed in a future major
    @Override
    public void addThemeVariants(TextAreaVariant... variants) {
        HasThemeVariant.super.addThemeVariants(variants);
    }

    // Override is only required to keep binary compatibility with other 23.x
    // minor versions, can be removed in a future major
    @Override
    public void removeThemeVariants(TextAreaVariant... variants) {
        HasThemeVariant.super.removeThemeVariants(variants);
    }
}
