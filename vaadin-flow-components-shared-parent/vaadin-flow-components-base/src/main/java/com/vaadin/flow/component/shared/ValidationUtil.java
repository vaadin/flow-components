/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.shared;

import com.vaadin.flow.data.binder.ValidationResult;

import java.util.Objects;

/**
 * Util methods for component validation
 */
public class ValidationUtil {

    private ValidationUtil() {
    }

    /**
     * Checks the required validation constraint
     *
     * @param required
     *            the required state of the component
     * @param value
     *            the current value set on the component
     * @param emptyValue
     *            the empty value for the component
     * @return <code>Validation.ok()</code> if the validation passes,
     *         <code>Validation.error()</code> otherwise
     * @param <V>
     *            the type of the component value
     */
    public static <V> ValidationResult checkRequired(boolean required, V value,
            V emptyValue) {
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
     * @return <code>Validation.ok()</code> if the validation passes,
     *         <code>Validation.error()</code> otherwise
     * @param <V>
     *            the type of the component value
     */
    public static <V extends Comparable<V>> ValidationResult checkGreaterThanMax(
            V value, V maxValue) {
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
     * @return <code>Validation.ok()</code> if the validation passes,
     *         <code>Validation.error()</code> otherwise
     * @param <V>
     *            the type of the component value
     */
    public static <V extends Comparable<V>> ValidationResult checkSmallerThanMin(
            V value, V minValue) {
        final boolean isSmallerThanMin = value != null && minValue != null
                && value.compareTo(minValue) < 0;
        if (isSmallerThanMin) {
            return ValidationResult.error("");
        }
        return ValidationResult.ok();
    }
}
