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
    public static <V> ValidationResult checkRequired(boolean required, V value, V emptyValue) {
        return checkRequired("", required, value, emptyValue);
    }

    public static <V> ValidationResult checkRequired(String errorMessage, boolean required, V value,
            V emptyValue) {
        final boolean isRequiredButEmpty = required
                && Objects.equals(emptyValue, value);
        if (isRequiredButEmpty) {
            return ValidationResult.error(errorMessage);
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

    public static ValidationResult checkMaxLength(String errorMessage, String value,
            Integer maxLength) {
        boolean isError = value != null && maxLength != null
                && value.length() > maxLength;
        return isError ? ValidationResult.error(errorMessage) : ValidationResult.ok();
    }

    public static ValidationResult checkMinLength(String errorMessage, String value,
            Integer minLength) {
        boolean isError = value != null && !value.isEmpty() && minLength != null
                && value.length() < minLength;
        return isError ? ValidationResult.error(errorMessage) : ValidationResult.ok();
    }

    public static ValidationResult checkPattern(String errorMessage, String value, String pattern) {
        boolean isError = value != null && !value.isEmpty() && pattern != null
                && !pattern.isEmpty() && !value.matches(pattern);
        return isError ? ValidationResult.error(errorMessage) : ValidationResult.ok();
    }
}
