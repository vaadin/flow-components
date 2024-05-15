package com.vaadin.flow.component.textfield;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.ValidationResult;

import java.io.Serializable;

import com.vaadin.flow.data.binder.ErrorMessageProvider;
import com.vaadin.flow.data.binder.Validator;

/**
 * Utility class for performing server-side validation of string values in text
 * fields. This is needed because it is possible to circumvent the client side
 * validation constraints using browser development tools.
 *
 * @author Vaadin Ltd
 */
final class TextFieldValidationSupport implements Serializable {

    private final HasValue<?, String> field;
    private boolean required;
    private Integer minLength;
    private Integer maxLength;
    private Pattern pattern;

    private ErrorMessageProvider requiredErrorMessageProvider = context -> "";
    private ErrorMessageProvider minLengthErrorMessageProvider = context -> "";
    private ErrorMessageProvider maxLengthErrorMessageProvider = context -> "";
    private ErrorMessageProvider patternErrorMessageProvider = context -> "";

    TextFieldValidationSupport(HasValue<?, String> field) {
        this.field = field;
    }

    /**
     * @see TextField#setRequired
     */
    void setRequired(boolean required) {
        this.required = required;
    }

    void setRequiredErrorMessageProvider(
            ErrorMessageProvider errorMessageProvider) {
        requiredErrorMessageProvider = Objects
                .requireNonNull(errorMessageProvider);
    }

    /**
     * @see TextField#setMinlength(double)
     */
    void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    void setMinLengthErrorMessageProvider(
            ErrorMessageProvider errorMessageProvider) {
        minLengthErrorMessageProvider = Objects
                .requireNonNull(errorMessageProvider);
    }

    /**
     * @see TextField#setMaxlength(double)
     */
    void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    void setMaxLengthErrorMessageProvider(
            ErrorMessageProvider errorMessageProvider) {
        maxLengthErrorMessageProvider = Objects
                .requireNonNull(errorMessageProvider);
    }

    /**
     * @see TextField#setPattern(String)
     */
    void setPattern(String pattern) {
        this.pattern = pattern == null || pattern.isEmpty() ? null
                : Pattern.compile(pattern);
    }

    void setPatternErrorMessageProvider(
            ErrorMessageProvider errorMessageProvider) {
        patternErrorMessageProvider = Objects
                .requireNonNull(errorMessageProvider);
    }

    ValidationResult checkValidity(String value,
            boolean withRequiredValidator) {
        if (withRequiredValidator) {
            ValidationResult requiredValidation = ValidationUtil
                    .checkRequired(required, value, field.getEmptyValue());
            if (requiredValidation.isError()) {
                return ValidationResult
                        .error(requiredErrorMessageProvider.apply(null));
            }
        }

        final boolean isMaxLengthExceeded = value != null && maxLength != null
                && value.length() > maxLength;
        if (isMaxLengthExceeded) {
            return ValidationResult
                    .error(maxLengthErrorMessageProvider.apply(null));
        }

        final boolean isMinLengthNotReached = value != null && !value.isEmpty()
                && minLength != null && value.length() < minLength;
        if (isMinLengthNotReached) {
            return ValidationResult
                    .error(minLengthErrorMessageProvider.apply(null));
        }

        final boolean valueViolatePattern = value != null && !value.isEmpty()
                && pattern != null && !pattern.matcher(value).matches();
        if (valueViolatePattern) {
            return ValidationResult
                    .error(patternErrorMessageProvider.apply(null));
        }

        return ValidationResult.ok();
    }

}
