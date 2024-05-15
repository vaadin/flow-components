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
     * Checks if the value satistifies the required constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with an
     * empty error message depending on the result.
     *
     * @param <V>
     *            the type of the value
     * @param required
     *            whether the constraint is enabled
     * @param value
     *            the value to validate
     * @param emptyValue
     *            the value considered to be empty
     * @return {@code ValidationResult.ok()} if the value does not equal to the
     *         empty value, {@code ValidationResult.error()} otherwise
     * @deprecated since 24.5, use
     *             {@link #validateRequiredConstraint(String, boolean, Object, Object)}
     *             instead.
     */
    @Deprecated
    public static <V> ValidationResult checkRequired(boolean required, V value,
            V emptyValue) {
        return validateRequiredConstraint("", required, value, emptyValue);
    }

    /**
     * Checks if the value satistifies the required constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with
     * the given error message depending on the result.
     *
     * @param <V>
     *            the type of the value
     * @param errorMessage
     *            the error message to return if the check fails
     * @param required
     *            whether the constraint is enabled
     * @param value
     *            the value to validate
     * @param emptyValue
     *            the value considered to be empty
     * @return {@code ValidationResult.ok()} if the value does not equal to the
     *         empty value, {@code ValidationResult.error()} otherwise
     */
    public static <V> ValidationResult validateRequiredConstraint(
            String errorMessage, boolean required, V value, V emptyValue) {
        boolean isError = required && Objects.equals(emptyValue, value);
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }

    /**
     * Checks if the value satisfies the maximum value constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with an
     * empty error message depending on the result.
     *
     * @param <V>
     *            the type of the value
     * @param value
     *            the value to validate
     * @param maxValue
     *            the maximum allowed value
     * @return {@code ValidationResult.ok()} if the value is smaller or equal to
     *         the maximum value, {@code ValidationResult.error()} otherwise
     * @deprecated since 24.5, use
     *             {@link #validateMaxConstraint(String, Comparable, Comparable)}
     *             instead.
     */
    @Deprecated
    public static <V extends Comparable<V>> ValidationResult checkGreaterThanMax(
            V value, V maxValue) {
        return validateMaxConstraint("", value, maxValue);
    }

    /**
     * Checks if the value satisfies the maximum value constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with
     * the given error message depending on the result.
     *
     * @param <V>
     *            the type of the value
     * @param errorMessage
     *            the error message to return if the check fails
     * @param value
     *            the value to validate
     * @param maxValue
     *            the maximum allowed value
     * @return {@code ValidationResult.ok()} if the value is smaller or equal to
     *         the maximum value, {@code ValidationResult.error()} otherwise
     */
    public static <V extends Comparable<V>> ValidationResult validateMaxConstraint(
            String errorMessage, V value, V maxValue) {
        boolean isError = value != null && maxValue != null
                && value.compareTo(maxValue) > 0;
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }

    /**
     * Checks if the value satisfies the minimum value constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with an
     * empty error message depending on the result.
     *
     * @param <V>
     *            the type of the value
     * @param value
     *            the value to validate
     * @param minValue
     *            the minimum allowed value
     * @return {@code ValidationResult.ok()} if the value is greater or equal to
     *         the minimum value, {@code ValidationResult.error()} otherwise
     * @deprecated since 24.5, use
     *             {@link #validateMinConstraint(String, Comparable, Comparable)}
     *             instead.
     */
    @Deprecated
    public static <V extends Comparable<V>> ValidationResult checkSmallerThanMin(
            V value, V minValue) {
        return validateMinConstraint("", value, minValue);
    }

    /**
     * Checks if the value satisfies the minimum value constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with
     * the given error message depending on the result.
     *
     * @param <V>
     *            the type of the value
     * @param errorMessage
     *            the error message to return if the check fails
     * @param value
     *            the value to validate
     * @param minValue
     *            the minimum allowed value
     * @return {@code ValidationResult.ok()} if the value is greater or equal to
     *         the minimum value, {@code ValidationResult.error()} otherwise
     */
    public static <V extends Comparable<V>> ValidationResult validateMinConstraint(
            String errorMessage, V value, V minValue) {
        boolean isError = value != null && minValue != null
                && value.compareTo(minValue) < 0;
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }

    /**
     * Checks if the value satisfies the minimum length constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with
     * the given error message depending on the result.
     *
     * @param errorMessage
     *            the error message to return if the check fails
     * @param value
     *            the value to validate
     * @param minLength
     *            the minimum allowed length
     * @return {@code ValidationResult.ok()} if the value is shorter than or
     *         equal to the minimum length, {@code ValidationResult.error()}
     *         otherwise
     */
    public static ValidationResult validateMinLengthConstraint(
            String errorMessage, String value, Integer minLength) {
        boolean isError = value != null && !value.isEmpty() && minLength != null
                && value.length() < minLength;
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }

    /**
     * Checks if the value satisfies the maximum length constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with
     * the given error message depending on the result.
     *
     * @param errorMessage
     *            the error message to return if the check fails
     * @param value
     *            the value to validate
     * @param maxLength
     *            the maximum allowed length
     * @return {@code ValidationResult.ok()} if the value is longer than or
     *         equal to the minimum length, {@code ValidationResult.error()}
     *         otherwise
     */
    public static ValidationResult validateMaxLengthConstraint(
            String errorMessage, String value, Integer maxLength) {
        boolean isError = value != null && maxLength != null
                && value.length() > maxLength;
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }

    /**
     * Checks if the value satisfies the pattern constraint and returns a
     * {@code ValidationResult.ok()} or {@code ValidationResult.error()} with
     * the given error message depending on the result.
     *
     * @param errorMessage
     *            the error message to return if the check fails
     * @param value
     *            the value to validate
     * @param pattern
     *            the pattern to match
     * @return {@code ValidationResult.ok()} if the value matches the pattern,
     *         {@code ValidationResult.error()} otherwise
     */
    public static ValidationResult validatePatternConstraint(
            String errorMessage, String value, String pattern) {
        boolean isError = value != null && !value.isEmpty() && pattern != null
                && !pattern.isEmpty() && !value.matches(pattern);
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }
}
