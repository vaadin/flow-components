package com.vaadin.flow.component.datepicker;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import java.time.LocalDate;

import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.ErrorMessageProvider;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;

final class DatePickerValidationSupport implements Serializable {

    private DatePicker field;

    private ErrorMessageProvider requiredErrorMessageProvider = context -> "";
    private ErrorMessageProvider badInputErrorMessageProvider = context -> "";
    private ErrorMessageProvider minErrorMessageProvider = context -> "";
    private ErrorMessageProvider maxErrorMessageProvider = context -> "";

    private final Validator<LocalDate> badInputValidator = (value, context) -> {
        boolean isError = field.valueEquals(value, field.getEmptyValue())
                && field.isInputValuePresent();

        if (isError) {
            return ValidationResult
                    .error(badInputErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

    private final Validator<LocalDate> requiredValidator = (value, context) -> {
        ValidationResult result = ValidationUtil.checkRequired(
                field.isRequiredIndicatorVisible(), value,
                field.getEmptyValue());

        if (result.isError()) {
            return ValidationResult
                    .error(requiredErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

    private final Validator<LocalDate> maxValidator = (value, context) -> {
        ValidationResult result = ValidationUtil.checkGreaterThanMax(value,
                field.getMax());

        if (result.isError()) {
            return ValidationResult
                    .error(maxErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

    private final Validator<LocalDate> minValidator = (value, context) -> {
        ValidationResult result = ValidationUtil.checkSmallerThanMin(value,
                field.getMin());

        if (result.isError()) {
            return ValidationResult
                    .error(minErrorMessageProvider.apply(context));
        }

        return ValidationResult.ok();
    };

    DatePickerValidationSupport(DatePicker field) {
        this.field = field;
    }

    ValidationResult checkValidity(LocalDate value, ValueContext context,
            boolean withRequired) {
        List<Validator<LocalDate>> validators = new ArrayList<>();
        if (withRequired) {
            validators.add(requiredValidator);
        }
        validators.add(badInputValidator);
        validators.add(minValidator);
        validators.add(maxValidator);

        return validators.stream()
                .map(validator -> validator.apply(value, context))
                .filter(ValidationResult::isError).findFirst()
                .orElse(ValidationResult.ok());
    }

    void setRequiredErrorMessageProvider(
            ErrorMessageProvider errorMessageProvider) {
        Objects.requireNonNull(errorMessageProvider);
        requiredErrorMessageProvider = errorMessageProvider;
    }

    void setBadInputErrorMessageProvider(
            ErrorMessageProvider errorMessageProvider) {
        Objects.requireNonNull(errorMessageProvider);
        badInputErrorMessageProvider = errorMessageProvider;
    }

    void setMinErrorMessageProvider(ErrorMessageProvider errorMessageProvider) {
        Objects.requireNonNull(errorMessageProvider);
        minErrorMessageProvider = errorMessageProvider;
    }

    void setMaxErrorMessageProvider(ErrorMessageProvider errorMessageProvider) {
        Objects.requireNonNull(errorMessageProvider);
        maxErrorMessageProvider = errorMessageProvider;
    }
}
