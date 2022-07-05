package com.vaadin.flow.component.shared;

import com.vaadin.flow.data.binder.ValidationResult;

import java.util.Objects;

/**
 * Util methods for component validation
 */
public class ValidationUtils {

    /**
     * Checks the required validation constraint
     *
     * @param required
     *            the required state of the component
     * @param value
     *            the current value set on the component
     * @param emptyValue
     *            the empty value for the component
     * @return <code>Validation.ok()</code> if the validation pass,
     *         <code>Validation.error()</code> otherwise
     * @param <T>
     *            the type of the component value
     */
    public static <T> ValidationResult checkRequired(boolean required, T value,
            T emptyValue) {
        final boolean isRequiredButEmpty = required
                && Objects.equals(emptyValue, value);
        if (isRequiredButEmpty) {
            return ValidationResult.error("");
        }
        return ValidationResult.ok();
    }

    /**
     * Checks if the value being set to the component is greater than the max
     * value defined
     *
     * @param value
     *            the current value set on the component
     * @param maxValue
     *            the max value set on the component
     * @return <code>Validation.ok()</code> if the validation pass,
     *         <code>Validation.error()</code> otherwise
     * @param <T>
     *            the type of the component value
     */
    public static <T extends Comparable<T>> ValidationResult checkGreaterThanMax(
            T value, T maxValue) {
        final boolean isGreaterThanMax = value != null && maxValue != null
                && value.compareTo(maxValue) > 0;
        if (isGreaterThanMax) {
            return ValidationResult.error("");
        }
        return ValidationResult.ok();
    }

    /**
     * Checks if the value being set to the component is smaller than the max
     * value defined
     *
     * @param value
     *            the current value set on the component
     * @param minValue
     *            the min value set on the component
     * @return <code>Validation.ok()</code> if the validation pass,
     *         <code>Validation.error()</code> otherwise
     * @param <T>
     *            the type of the component value
     */
    public static <T extends Comparable<T>> ValidationResult checkSmallerThanMin(
            T value, T minValue) {
        final boolean isSmallerThanMin = value != null && minValue != null
                && value.compareTo(minValue) < 0;
        if (isSmallerThanMin) {
            return ValidationResult.error("");
        }
        return ValidationResult.ok();
    }
}
