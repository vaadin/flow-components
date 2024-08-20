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
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Password Field is an input field for entering passwords. The input is masked
 * by default. On mobile devices the last typed letter is shown for a brief
 * moment. The masking can be toggled using an optional reveal button.
 *
 * @author Vaadin Ltd.
 */
@Tag("vaadin-password-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.5.0-alpha8")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/password-field", version = "24.5.0-alpha8")
@JsModule("@vaadin/password-field/src/vaadin-password-field.js")
public class PasswordField extends TextFieldBase<PasswordField, String>
        implements HasAllowedCharPattern, HasThemeVariant<TextFieldVariant> {

    private PasswordFieldI18n i18n;

    private Validator<String> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(
                            getI18nErrorMessage(
                                    PasswordFieldI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        ValidationResult maxLengthResult = ValidationUtil
                .validateMaxLengthConstraint(
                        getI18nErrorMessage(
                                PasswordFieldI18n::getMaxLengthErrorMessage),
                        value, hasMaxLength() ? getMaxLength() : null);
        if (maxLengthResult.isError()) {
            return maxLengthResult;
        }

        ValidationResult minLengthResult = ValidationUtil
                .validateMinLengthConstraint(
                        getI18nErrorMessage(
                                PasswordFieldI18n::getMinLengthErrorMessage),
                        value, getMinLength());
        if (minLengthResult.isError()) {
            return minLengthResult;
        }

        ValidationResult patternResult = ValidationUtil
                .validatePatternConstraint(
                        getI18nErrorMessage(
                                PasswordFieldI18n::getPatternErrorMessage),
                        value, getPattern());
        if (patternResult.isError()) {
            return patternResult;
        }

        return ValidationResult.ok();
    };

    private ValidationController<PasswordField, String> validationController = new ValidationController<>(
            this);

    /**
     * Constructs an empty {@code PasswordField}.
     */
    public PasswordField() {
        this(true);
    }

    /**
     * Constructs an empty {@code PasswordField}.
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
    private PasswordField(boolean isInitialValueOptional) {
        super("", "", false, isInitialValueOptional);

        // workaround for https://github.com/vaadin/flow/issues/3496
        setInvalid(false);

        setValueChangeMode(ValueChangeMode.ON_CHANGE);

        addValueChangeListener(e -> validate());
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
    public PasswordField(
            ValueChangeListener<? super ComponentValueChangeEvent<PasswordField, String>> listener) {
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
            ValueChangeListener<? super ComponentValueChangeEvent<PasswordField, String>> listener) {
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
     * @see #setValue(Object)
     * @see #addValueChangeListener(com.vaadin.flow.component.HasValue.ValueChangeListener)
     */
    public PasswordField(String label, String initialValue,
            ValueChangeListener<? super ComponentValueChangeEvent<PasswordField, String>> listener) {
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

    /**
     * Set to <code>false</code> to hide the eye icon which toggles the password
     * visibility.
     *
     * @return <code>true</code> if the button is visible, <code>false</code>
     *         otherwise
     */
    public boolean isRevealButtonVisible() {
        return !getElement().getProperty("revealButtonHidden", false);
    }

    /**
     * Set to <code>false</code> to hide the eye icon which toggles the password
     * visibility.
     *
     * @param revealButtonVisible
     *            <code>true</code> to set the button visible,
     *            <code>false</code> otherwise
     */
    public void setRevealButtonVisible(boolean revealButtonVisible) {
        getElement().setProperty("revealButtonHidden", !revealButtonVisible);
    }

    @Override
    public String getEmptyValue() {
        return "";
    }

    /**
     * Sets the value of this password field. If the new value is not equal to
     * {@code getValue()}, fires a value change event. Throws
     * {@code NullPointerException}, if the value is null.
     * <p>
     * Note: {@link Binder} will take care of the {@code null} conversion when
     * integrates with password field, as long as no new converter is defined.
     *
     * @param value
     *            the new value, not {@code null}
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
    }

    /**
     * Returns the current value of the password field. By default, the empty
     * password field will return an empty string.
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
     * {@link #setI18n(PasswordFieldI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public PasswordFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(PasswordFieldI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(
            Function<PasswordFieldI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
    }

    /**
     * The internationalization properties for {@link PasswordField}.
     */
    public static class PasswordFieldI18n implements Serializable {

        private String requiredErrorMessage;
        private String minLengthErrorMessage;
        private String maxLengthErrorMessage;
        private String patternErrorMessage;

        /**
         * Gets the error message displayed when the field is required but
         * empty.
         *
         * @return the error message or {@code null} if not set
         * @see PasswordField#isRequiredIndicatorVisible()
         * @see PasswordField#setRequiredIndicatorVisible(boolean)
         */
        public String getRequiredErrorMessage() {
            return requiredErrorMessage;
        }

        /**
         * Sets the error message to display when the field is required but
         * empty.
         * <p>
         * Note, custom error messages set with
         * {@link PasswordField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see PasswordField#isRequiredIndicatorVisible()
         * @see PasswordField#setRequiredIndicatorVisible(boolean)
         */
        public PasswordFieldI18n setRequiredErrorMessage(String errorMessage) {
            requiredErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is shorter than
         * the minimum allowed length.
         *
         * @return the error message or {@code null} if not set
         * @see PasswordField#getMinLength()
         * @see PasswordField#setMinLength(int)
         */
        public String getMinLengthErrorMessage() {
            return minLengthErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is shorter
         * than the minimum allowed length.
         * <p>
         * Note, custom error messages set with
         * {@link PasswordField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see PasswordField#getMinLength()
         * @see PasswordField#setMinLength(int)
         */
        public PasswordFieldI18n setMinLengthErrorMessage(String errorMessage) {
            minLengthErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value is longer than
         * the maximum allowed length.
         *
         * @return the error message or {@code null} if not set
         * @see PasswordField#getMaxLength()
         * @see PasswordField#setMaxLength(int)
         */
        public String getMaxLengthErrorMessage() {
            return maxLengthErrorMessage;
        }

        /**
         * Sets the error message to display when the field value is longer than
         * the maximum allowed length.
         * <p>
         * Note, custom error messages set with
         * {@link PasswordField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see PasswordField#getMaxLength()
         * @see PasswordField#setMaxLength(int)
         */
        public PasswordFieldI18n setMaxLengthErrorMessage(String errorMessage) {
            maxLengthErrorMessage = errorMessage;
            return this;
        }

        /**
         * Gets the error message displayed when the field value does not match
         * the pattern.
         *
         * @return the error message or {@code null} if not set
         * @see PasswordField#getPattern()
         * @see PasswordField#setPattern(String)
         */
        public String getPatternErrorMessage() {
            return patternErrorMessage;
        }

        /**
         * Sets the error message to display when the field value does not match
         * the pattern.
         * <p>
         * Note, custom error messages set with
         * {@link PasswordField#setErrorMessage(String)} take priority over i18n
         * error messages.
         *
         * @param errorMessage
         *            the error message or {@code null} to clear it
         * @return this instance for method chaining
         * @see PasswordField#getPattern()
         * @see PasswordField#setPattern(String)
         */
        public PasswordFieldI18n setPatternErrorMessage(String errorMessage) {
            patternErrorMessage = errorMessage;
            return this;
        }
    }
}
