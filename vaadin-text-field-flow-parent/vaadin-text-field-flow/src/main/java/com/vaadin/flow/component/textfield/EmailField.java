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
import java.util.function.Function;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.ClientValidationUtil;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Email Field is an extension of Text Field that only accepts email addresses
 * as input. If the given address is invalid, the field is highlighted in red
 * and an error message appears underneath the input. The validity of the email
 * addresses is checked according to the RFC 5322 standard, which includes the
 * format for email addresses. The component also supports supplying additional
 * validation criteria using a regular expression (see
 * {@link #setPattern(String)}). These extra validation criteria can be used,
 * for example, to require a specific email domain.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-email-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha7")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/email-field", version = "24.5.0-alpha7")
@JsModule("@vaadin/email-field/src/vaadin-email-field.js")
public class EmailField extends TextFieldBase<EmailField, String>
        implements HasAllowedCharPattern, HasThemeVariant<TextFieldVariant> {

    private EmailFieldI18n i18n;

    private Validator<String> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(
                            getI18nErrorMessage(
                                    EmailFieldI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        ValidationResult maxLengthResult = ValidationUtil
                .validateMaxLengthConstraint(
                        getI18nErrorMessage(
                                EmailFieldI18n::getMaxLengthErrorMessage),
                        value, hasMaxLength() ? getMaxLength() : null);
        if (maxLengthResult.isError()) {
            return maxLengthResult;
        }

        ValidationResult minLengthResult = ValidationUtil
                .validateMinLengthConstraint(
                        getI18nErrorMessage(
                                EmailFieldI18n::getMinLengthErrorMessage),
                        value, getMinLength());
        if (minLengthResult.isError()) {
            return minLengthResult;
        }

        ValidationResult patternResult = ValidationUtil
                .validatePatternConstraint(
                        getI18nErrorMessage(
                                EmailFieldI18n::getPatternErrorMessage),
                        value,
                        hasPattern() ? getPattern() : EmailValidator.PATTERN);
        if (patternResult.isError()) {
            return patternResult;
        }

        return ValidationResult.ok();
    };

    private ValidationController<EmailField, String> validationController = new ValidationController<>(
            this);

    /**
     * Constructs an empty {@code EmailField}.
     */
    public EmailField() {
        super("", "", false, true);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());
    }

    /**
     * Constructs an empty {@code EmailField} with the given label.
     *
     * @param label
     *            the text to set as the label
     */
    public EmailField(String label) {
        this();
        setLabel(label);
    }

    /**
     * Constructs an empty {@code EmailField} with the given label and
     * placeholder text.
     *
     * @param label
     *            the text to set as the label
     * @param placeholder
     *            the placeholder text to set
     */
    public EmailField(String label, String placeholder) {
        this(label);
        setPlaceholder(placeholder);
    }

    /**
     * Constructs an empty {@code EmailField} with a value change listener.
     *
     * @param listener
     *            the value change listener
     *
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public EmailField(
            ValueChangeListener<? super ComponentValueChangeEvent<EmailField, String>> listener) {
        this();
        addValueChangeListener(listener);
    }

    /**
     * Constructs an empty {@code EmailField} with a value change listener and a
     * label.
     *
     * @param label
     *            the text to set as the label
     * @param listener
     *            the value change listener
     *
     * @see #setLabel(String)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public EmailField(String label,
            ValueChangeListener<? super ComponentValueChangeEvent<EmailField, String>> listener) {
        this(label);
        addValueChangeListener(listener);
    }

    /**
     * Constructs a {@code EmailField} with a value change listener, a label and
     * an initial value.
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
    public EmailField(String label, String initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<EmailField, String>> listener) {
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

    private boolean hasPattern() {
        return getPattern() != null;
    }

    @Override
    public String getEmptyValue() {
        return "";
    }

    /**
     * Sets the value of this email field. If the new value is not equal to
     * {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with email field, as long as no new converter is defined.
     *
     * @param value
     *            the new value, not {@code null}
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    /**
     * Returns the current value of the email field. By default, the empty email
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
        return defaultValidator;
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
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        ClientValidationUtil.preventWebComponentFromModifyingInvalidState(this);
    }

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(EmailFieldI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public EmailFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(EmailFieldI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<EmailFieldI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link EmailField}.
     */
    public static class EmailFieldI18n implements Serializable {

        private String requiredErrorMessage;
        private String minLengthErrorMessage;
        private String maxLengthErrorMessage;
        private String patternErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see EmailField#isRequiredIndicatorVisible()
         * @see EmailField#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link EmailField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see EmailField#isRequiredIndicatorVisible()
         * @see EmailField#setRequiredIndicatorVisible(boolean)
         */
        public EmailFieldI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is shorter than
         * the minimum allowed length.
         *
         * @return the error message or {@code null} if not set
         * @see EmailField#getMinLength()
         * @see EmailField#setMinLength(int)
         */
        public String getMinLengthErrorMessage() {
            return minLengthErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is shorter
         * than the minimum allowed length.
         * <p>
         * Note, custom error messages set with
         * {@link EmailField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see EmailField#getMinLength()
         * @see EmailField#setMinLength(int)
         */
        public EmailFieldI18n setMinLengthErrorMessage(String errorMessage) {
            minLengthErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is longer than
         * the maximum allowed length.
         *
         * @return the error message or {@code null} if not set
         * @see EmailField#getMaxLength()
         * @see EmailField#setMaxLength(int)
         */
        public String getMaxLengthErrorMessage() {
            return maxLengthErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is longer than
         * the maximum allowed length.
         * <p>
         * Note, custom error messages set with
         * {@link EmailField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see EmailField#getMaxLength()
         * @see EmailField#setMaxLength(int)
         */
        public EmailFieldI18n setMaxLengthErrorMessage(String errorMessage) {
            maxLengthErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value does not match
         * the default email format, or alternatively, the custom format if
         * provided with {@link EmailField#setPattern(String)}.
         *
         * @return the error message or {@code null} if not set
         * @see EmailField#getPattern()
         * @see EmailField#setPattern(String)
         */
        public String getPatternErrorMessage() {
            return patternErrorMessage;
        }

        /**
         * Sets the error message to display when the field value does not match
         * the default email format, or alternatively, the custom format if
         * provided with {@link EmailField#setPattern(String)}.
         * <p>
         * Note, custom error messages set with
         * {@link EmailField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see EmailField#getPattern()
         * @see EmailField#setPattern(String)
         */
        public EmailFieldI18n setPatternErrorMessage(String errorMessage) {
            patternErrorMessage = errorMessage;
            return this;
        }
    }
}
