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
package com.vaadin.flow.component.textfield;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Text Field allows the user to input and edit text. Prefix and suffix
 * components, such as icons, are also supported.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-text-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha4")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/text-field", version = "24.5.0-alpha4")
@JsModule("@vaadin/text-field/src/vaadin-text-field.js")
public class TextField extends TextFieldBase<TextField, String>
        implements HasAllowedCharPattern, HasThemeVariant<TextFieldVariant> {

    private TextFieldI18n i18n;

    private boolean isConnectorAttached;

    private boolean manualValidationEnabled = false;

    /**
     * Constructs an empty {@code TextField}.
     */
    public TextField() {
        this(true);
    }

    /**
     * Constructs an empty {@code TextField}.
     * <p>
     * If {@code isInitialValueOptional} is {@code true} then the initial value
     * is used only if element has no {@code "value"} property value, otherwise
     * element {@code "value"} property is ignored and the initial value is set.
     *
     * @param isInitialValueOptional
     *            if {@code isInitialValueOptional} is {@code true} then the
     *            initial value is used only if element has no {@code "value"}
     *            property value, otherwise element {@code "value"} property is
     *            ignored and the initial value is set
     */
    private TextField(boolean isInitialValueOptional) {
        super("", "", false, isInitialValueOptional);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());
    }

    /**
     * Constructs an empty {@code TextField} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public TextField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code TextField} with the given label and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public TextField(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs a {@code TextField} with the given label, an initial value and
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
    public TextField(String label, String initialValue, String placeholder) {
        this(label);
        setValue(initialValue);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code TextField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextField(
            ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code TextField} with a label and a value change
     * listener.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     * @see #setLabel(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public TextField(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code TextField} with a label,a value change
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
    public TextField(String label, String initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<TextField, String>> listener) {
        this(label);
        setValue(initialValue);
        addValueChangeListener(listener);
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

    private boolean hasMaxLength() {
        return getElement().getProperty("maxlength") != null;
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
    }

    /**
     * A regular expression that the value is checked against. The pattern must
     * match the entire value, not just some subset.
     *
     * @return the {@code pattern} property from the webcomponent
     */
    public String getPattern() {
        return getElement().getProperty("pattern");
    }

    @Override
    public String getEmptyValue() {
        return "";
    }

    /**
     * Sets the value of this text field. If the new value is not equal to
     * {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with text field, as long as no new converter is defined.
     *
     * @param value
     *            the new value, not {@code null}
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    /**
     * Returns the current value of the text field. By default, the empty text
     * field will return an empty string.
     *
     * @return the current value.
     */
    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public Validator<String> getDefaultValidator() {
        return (value, context) -> checkValidity(value, false);
    }

    @Override
    public void setManualValidation(boolean enabled) {
        this.manualValidationEnabled = enabled;
    }

    private ValidationResult checkValidity(String value,
            boolean withRequiredValidator) {
        if (withRequiredValidator) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(getRequiredErrorMessage(),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        ValidationResult maxLengthResult = ValidationUtil
                .validateMaxLengthConstraint(getMaxLengthErrorMessage(), value,
                        hasMaxLength() ? getMaxLength() : null);
        if (maxLengthResult.isError()) {
            return maxLengthResult;
        }

        ValidationResult minLengthResult = ValidationUtil
                .validateMinLengthConstraint(getMinLengthErrorMessage(), value,
                        getMinLength());
        if (minLengthResult.isError()) {
            return minLengthResult;
        }

        ValidationResult patternResult = ValidationUtil
                .validatePatternConstraint(getPatternErrorMessage(), value,
                        getPattern());
        if (patternResult.isError()) {
            return patternResult;
        }

        return ValidationResult.ok();
    }

    /**
     * Validates the current value against the constraints and sets the
     * {@code invalid} property and the {@code errorMessage} property using the
     * error messages defined in the i18n object.
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
            setErrorMessage(result.getErrorMessage());
        } else {
            setInvalid(false);
            setErrorMessage(null);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link TextField#setI18n(TextFieldI18n)}
     *
     * @return the i18n object. It will be {@code null}, If the i18n properties
     *         weren't set.
     */
    public TextFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization properties for this component.
     *
     * @param i18n
     *            the internationalized properties, not {@code null}
     */
    public void setI18n(TextFieldI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getRequiredErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(TextFieldI18n::getRequiredErrorMessage).orElse("");
    }

    private String getMinLengthErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(TextFieldI18n::getMinLengthErrorMessage).orElse("");
    }

    private String getMaxLengthErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(TextFieldI18n::getMaxLengthErrorMessage).orElse("");
    }

    private String getPatternErrorMessage() {
        return Optional.ofNullable(i18n)
                .map(TextFieldI18n::getPatternErrorMessage).orElse("");
    }

    /**
     * The internationalization properties for {@link TextField}.
     */
    public static class TextFieldI18n implements Serializable {

        private String requiredErrorMessage;
        private String minLengthErrorMessage;
        private String maxLengthErrorMessage;
        private String patternErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see TextField#isRequiredIndicatorVisible()
         * @see TextField#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see TextField#isRequiredIndicatorVisible()
         * @see TextField#setRequiredIndicatorVisible(boolean)
         */
        public TextFieldI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is shorter than
         * the minimum length.
         *
         * @return the error message or {@code null} if not set
         * @see TextField#getMinLength()
         * @see TextField#setMinLength(int)
         */
        public String getMinLengthErrorMessage() {
            return minLengthErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is shorter
         * than the minimum length.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see TextField#getMinLength()
         * @see TextField#setMinLength(int)
         */
        public TextFieldI18n setMinLengthErrorMessage(String errorMessage) {
            minLengthErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is longer than
         * the maximum length.
         *
         * @return the error message or {@code null} if not set
         * @see TextField#getMaxLength()
         * @see TextField#setMaxLength(int)
         */
        public String getMaxLengthErrorMessage() {
            return maxLengthErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is longer than
         * the maximum length.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see TextField#getMaxLength()
         * @see TextField#setMaxLength(int)
         */
        public TextFieldI18n setMaxLengthErrorMessage(String errorMessage) {
            maxLengthErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value does not match
         * the pattern.
         *
         * @return the error message or {@code null} if not set
         * @see TextField#getPattern()
         * @see TextField#setPattern(String)
         */
        public String getPatternErrorMessage() {
            return patternErrorMessage;
        }

        /**
         * Sets the error message to display when the field value does not match
         * the pattern.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see TextField#getPattern()
         * @see TextField#setPattern(String)
         */
        public TextFieldI18n setPatternErrorMessage(String errorMessage) {
            patternErrorMessage = errorMessage;
            return this;
        }
    }
}
