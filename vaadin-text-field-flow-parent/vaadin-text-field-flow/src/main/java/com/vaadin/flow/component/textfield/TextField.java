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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.shared.HasAllowedCharPattern;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.component.shared.internal.ValidationController;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Text Field allows the user to input and edit text. Prefix and suffix
 * components, such as icons, are also supported.
 *
 * <h2>Validation</h2>
 * <p>
 * Text Field comes with a built-in validation mechanism based on constraints.
 * Validation is triggered whenever the user applies an input change, for
 * example by pressing Enter or blurring the field. Programmatic value changes
 * trigger validation as well. In eager and lazy value change modes, validation
 * is also triggered on every character press with a delay according to the
 * selected mode.
 * <p>
 * Validation verifies that the value satisfies the specified constraints. If
 * any of the constraints are violated, the component is marked as invalid and
 * an error message is displayed below the input.
 * <p>
 * The following constraints are supported:
 * <ul>
 * <li>{@link #setRequiredIndicatorVisible(boolean)}
 * <li>{@link #setMinLength(int)}
 * <li>{@link #setMaxLength(int)}
 * <li>{@link #setPattern(String)}
 * </ul>
 * <p>
 * Error messages for constraints can be configured with the
 * {@link TextFieldI18n} object, using the respective properties. If you want to
 * provide a single catch-all error message, you can also use the
 * {@link #setErrorMessage(String)} method. Note that such an error message will
 * take priority over i18n error messages if both are set.
 * <p>
 * In addition to validation, constraints may also limit user input. For
 * example, the browser will prevent the user from entering more text than
 * specified by the max length constraint.
 * <p>
 * For more advanced validation that requires custom rules, you can use
 * {@link Binder}. By default, before running custom validators, Binder will
 * also check the component constraints and display error messages from the
 * {@link TextFieldI18n} object. The exception is the required constraint, for
 * which Binder provides its own API, see
 * {@link Binder.BindingBuilder#asRequired(String) asRequired()}.
 * <p>
 * However, if Binder doesn't fit your needs and you want to implement fully
 * custom validation logic, you can disable the constraint validation by setting
 * {@link #setManualValidation(boolean)} to true. This will allow you to control
 * the invalid state and the error message manually using
 * {@link #setInvalid(boolean)} and {@link #setErrorMessage(String)} API.
 *
 * @author Vaadin Ltd
 */
@Tag("vaadin-text-field")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "24.8.0-alpha18")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@NpmPackage(value = "@vaadin/text-field", version = "24.8.0-alpha18")
@JsModule("@vaadin/text-field/src/vaadin-text-field.js")
public class TextField extends TextFieldBase<TextField, String>
        implements HasAllowedCharPattern, HasThemeVariant<TextFieldVariant> {

    private TextFieldI18n i18n;

    private Validator<String> defaultValidator = (value, context) -> {
        boolean fromComponent = context == null;

        // Do the required check only if the validator is called from the
        // component, and not from Binder. Binder has its own implementation
        // of required validation.
        if (fromComponent) {
            ValidationResult requiredResult = ValidationUtil
                    .validateRequiredConstraint(
                            getI18nErrorMessage(
                                    TextFieldI18n::getRequiredErrorMessage),
                            isRequiredIndicatorVisible(), value,
                            getEmptyValue());
            if (requiredResult.isError()) {
                return requiredResult;
            }
        }

        ValidationResult maxLengthResult = ValidationUtil
                .validateMaxLengthConstraint(
                        getI18nErrorMessage(
                                TextFieldI18n::getMaxLengthErrorMessage),
                        value, hasMaxLength() ? getMaxLength() : null);
        if (maxLengthResult.isError()) {
            return maxLengthResult;
        }

        ValidationResult minLengthResult = ValidationUtil
                .validateMinLengthConstraint(
                        getI18nErrorMessage(
                                TextFieldI18n::getMinLengthErrorMessage),
                        value, getMinLength());
        if (minLengthResult.isError()) {
            return minLengthResult;
        }

        ValidationResult patternResult = ValidationUtil
                .validatePatternConstraint(
                        getI18nErrorMessage(
                                TextFieldI18n::getPatternErrorMessage),
                        value, getPattern());
        if (patternResult.isError()) {
            return patternResult;
        }

        return ValidationResult.ok();
    };

    private ValidationController<TextField, String> validationController = new ValidationController<>(
            this);

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

        getElement().setProperty("manualValidation", true);

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
     * {@inheritDoc}
     * <p>
     * Distinct error messages for different constraints can be configured with
     * the {@link TextFieldI18n} object, using the respective properties.
     * However, note that the error message set with
     * {@link #setErrorMessage(String)} will take priority and override any i18n
     * error messages if both are set.
     */
    @Override
    public void setErrorMessage(String errorMessage) {
        super.setErrorMessage(errorMessage);
    }

    /**
     * @see TextFieldI18n#setRequiredErrorMessage(String)
     */
    @Override
    public void setRequiredIndicatorVisible(boolean required) {
        super.setRequiredIndicatorVisible(required);
    }

    /**
     * Sets the maximum number of characters (in Unicode code points) that the
     * user can enter. Values with a length exceeding this limit will cause the
     * component to invalidate.
     * <p>
     * The maximum length is inclusive.
     *
     * @param maxLength
     *            the maximum length
     * @see TextFieldI18n#setMaxLengthErrorMessage(String)
     */
    public void setMaxLength(int maxLength) {
        getElement().setProperty("maxlength", maxLength);
    }

    /**
     * Gets the maximum number of characters (in Unicode code points) that the
     * user can enter.
     *
     * @return the maximum length
     * @see #setMaxLength(int)
     */
    public int getMaxLength() {
        return (int) getElement().getProperty("maxlength", 0.0);
    }

    private boolean hasMaxLength() {
        return getElement().getProperty("maxlength") != null;
    }

    /**
     * Sets the minimum number of characters (in Unicode code points) that the
     * user can enter. Values with a length shorter than this limit will cause
     * the component to invalidate.
     * <p>
     * The minimum length is inclusive.
     *
     * @param minLength
     *            the minimum length
     * @see TextFieldI18n#setMinLengthErrorMessage(String)
     */
    public void setMinLength(int minLength) {
        getElement().setProperty("minlength", minLength);
    }

    /**
     * Gests the minimum number of characters (in Unicode code points) that the
     * user can enter.
     *
     * @return the minimum length
     * @see #setMinLength(int)
     */
    public int getMinLength() {
        return (int) getElement().getProperty("minlength", 0.0);
    }

    /**
     * Sets a regular expression for the value to pass during validation. Values
     * that do not match the pattern will cause the component to invalidate.
     * <p>
     * The pattern must be a valid JavaScript Regular Expression that matches
     * the entire value, not just some subset.
     *
     * @param pattern
     *            the new String pattern or {@code null} to clear it
     *
     * @see TextFieldI18n#setPatternErrorMessage(String)
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
     * Gets the regular expression that the value is checked against during
     * validation.
     *
     * @return the pattern or {@code null} if not set
     * @see #setPattern(String)
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

    /**
     * Gets the internationalization object previously set for this component.
     * <p>
     * NOTE: Updating the instance that is returned from this method will not
     * update the component if not set again using
     * {@link #setI18n(TextFieldI18n)}
     *
     * @return the i18n object or {@code null} if no i18n object has been set
     */
    public TextFieldI18n getI18n() {
        return i18n;
    }

    /**
     * Sets the internationalization object for this component.
     *
     * @param i18n
     *            the i18n object, not {@code null}
     */
    public void setI18n(TextFieldI18n i18n) {
        this.i18n = Objects.requireNonNull(i18n,
                "The i18n properties object should not be null");
    }

    private String getI18nErrorMessage(Function<TextFieldI18n, String> getter) {
        return Optional.ofNullable(i18n).map(getter).orElse("");
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
         * <p>
         * Note, custom error messages set with
         * {@link TextField#setErrorMessage(String)} take priority over i18n
         * error messages.
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
         * the minimum allowed length.
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
         * than the minimum allowed length.
         * <p>
         * Note, custom error messages set with
         * {@link TextField#setErrorMessage(String)} take priority over i18n
         * error messages.
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
         * the maximum allowed length.
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
         * the maximum allowed length.
         * <p>
         * Note, custom error messages set with
         * {@link TextField#setErrorMessage(String)} take priority over i18n
         * error messages.
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
         * <p>
         * Note, custom error messages set with
         * {@link TextField#setErrorMessage(String)} take priority over i18n
         * error messages.
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
