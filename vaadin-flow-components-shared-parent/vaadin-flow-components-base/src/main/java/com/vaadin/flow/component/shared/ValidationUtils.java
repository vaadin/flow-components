package com.vaadin.flow.component.shared;

import com.vaadin.flow.data.binder.ValidationResult;

import java.util.Objects;

public class ValidationUtils {
    public static <T> ValidationResult checkRequired(boolean required, T value,
            T emptyValue) {
        final boolean isRequiredButEmpty = required
                && Objects.equals(emptyValue, value);
        if (isRequiredButEmpty) {
            return ValidationResult.error(ValidationError.REQUIRED);
        }
        return ValidationResult.ok();
    }

    public static <T extends Comparable<T>> ValidationResult checkGreaterThanMax(
            T value, T maxValue) {
        final boolean isGreaterThanMax = value != null && maxValue != null
                && value.compareTo(maxValue) > 0;
        if (isGreaterThanMax) {
            return ValidationResult.error(ValidationError.GREATER_THAN_MAX);
        }
        return ValidationResult.ok();
    }

    public static <T extends Comparable<T>> ValidationResult checkSmallerThanMin(
            T value, T minValue) {
        final boolean isSmallerThanMin = value != null && minValue != null
                && value.compareTo(minValue) < 0;
        if (isSmallerThanMin) {
            return ValidationResult.error(ValidationError.SMALLER_THAN_MIN);
        }
        return ValidationResult.ok();
    }
}
