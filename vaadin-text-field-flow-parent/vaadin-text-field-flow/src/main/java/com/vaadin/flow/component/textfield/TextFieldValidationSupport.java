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

    private HasValue<?, String> field;
    private boolean required;
    private Integer minLength;
    private Integer maxLength;
    private Pattern pattern;

    private ErrorMessageProvider requiredErrorMessageProvider = context -> "";
    private ErrorMessageProvider minLengthErrorMessageProvider = context -> "";
    private ErrorMessageProvider maxLengthErrorMessageProvider = context -> "";
    private ErrorMessageProvider patternErrorMessageProvider = context -> "";

    private final Validator<String> requiredValidator = (value, context) -> {
        ValidationResult result = ValidationUtil.checkRequired(required, value,
                field.getEmptyValue());

        if (result.isError()) {
            return ValidationResult
                    .error(requiredErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

    private final Validator<String> minLengthValidator = (value, context) -> {
        boolean isError = value != null && maxLength != null
                && value.length() > maxLength;

        if (isError) {
            return ValidationResult
                    .error(minLengthErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

    private final Validator<String> maxLengthValidator = (value, context) -> {
        boolean isError = value != null && !value.isEmpty() && minLength != null
                && value.length() < minLength;

        if (isError) {
            return ValidationResult
                    .error(maxLengthErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

    private final Validator<String> patternValidator = (value, context) -> {
        boolean isError = value != null && !value.isEmpty() && pattern != null
                && !pattern.matcher(value).matches();

        if (isError) {
            return ValidationResult
                    .error(patternErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

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
        List<Validator<String>> validators = new ArrayList<>();
        if (withRequiredValidator) {
            validators.add(requiredValidator);
        }
        validators.add(minLengthValidator);
        validators.add(maxLengthValidator);
        validators.add(patternValidator);

        return validators.stream()
                .map(validator -> validator.apply(value, null))
                .filter(ValidationResult::isError).findFirst()
                .orElse(ValidationResult.ok());
    }

}
