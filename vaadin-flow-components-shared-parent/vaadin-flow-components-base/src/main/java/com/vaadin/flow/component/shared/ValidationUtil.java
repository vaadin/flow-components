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
     * Checks if the given value is empty and returns a ValidationResult with an
     * empty error message if it is.
     *
     * @param <V>
     *            the type of the component value
     * @param required
     *            the required state of the component
     * @param value
     *            the current value set on the component
     * @param emptyValue
     *            the empty value for the component
     * @return {@code ValidationResult.ok()} if the validation passes,
     *         {@code ValidationResult.error()} otherwise
     * @deprecated since 24.5, use
     *             {@link #checkRequired(String, boolean, Object, Object)}
     *             instead.
     */
    @Deprecated
    public static <V> ValidationResult checkRequired(boolean required, V value,
            V emptyValue) {
        return checkRequired("", required, value, emptyValue);
    }

    /**
     * Checks if the given value is empty and returns a ValidationResult with
     * the given error message if it is.
     *
     * @param <V>
     *            the type of the component value
     * @param errorMessage
     *            the error message to use if the validation fails
     * @param required
     *            the required state of the component
     * @param value
     *            the current value set on the component
     * @param emptyValue
     *            the empty value for the component
     * @return {@code ValidationResult.ok()} if the validation passes,
     *         {@code ValidationResult.error()} otherwise
     */
    public static <V> ValidationResult checkRequired(String errorMessage,
            boolean required, V value, V emptyValue) {
        boolean isError = required && Objects.equals(emptyValue, value);
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }

    /**
     * Checks if the given value is greater than the maximum value and returns a
     * ValidationResult with an empty error message if it is.
     *
     * @param <V>
     *            the type of the component value
     * @param value
     *            the current value set on the component
     * @param maxValue
     *            the maximum value set on the component
     * @return {@code ValidationResult.ok()} if the validation passes,
     *         {@code ValidationResult.error()} otherwise
     * @deprecated since 24.5, use
     *            {@link #checkGreaterThanMax(String, Comparable, Comparable)}
     *           instead.
     */
    @Deprecated
    public static <V extends Comparable<V>> ValidationResult checkGreaterThanMax(
            V value, V maxValue) {
        return checkGreaterThanMax("", value, maxValue);
    }

    /**
     * Checks if the given value is greater than the maximum value and returns a
     * ValidationResult with an empty error message if it is.
     *
     * @param <V>
     *            the type of the component value
     * @param errorMessage
     *            the error message to use if the validation fails
     * @param value
     *            the current value set on the component
     * @param maxValue
     *            the maximum value set on the component
     * @return {@code ValidationResult.ok()} if the validation passes,
     *         {@code ValidationResult.error()} otherwise
     */
    public static <V extends Comparable<V>> ValidationResult checkGreaterThanMax(
            String errorMessage, V value, V maxValue) {
        boolean isError = value != null && maxValue != null
                && value.compareTo(maxValue) > 0;
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }

    /**
     * Checks if the given value is smaller than the minimum value and returns a
     * ValidationResult with an empty error message if it is.
     *
     * @param <V>
     *            the type of the component value
     * @param value
     *            the current value set on the component
     * @param minValue
     *            the minimum value set on the component
     * @return {@code ValidationResult.ok()} if the validation passes,
     *         {@code ValidationResult.error()} otherwise
     * @deprecated since 24.5, use
     *           {@link #checkSmallerThanMin(String, Comparable, Comparable)}
     *          instead.
     */
    @Deprecated
    public static <V extends Comparable<V>> ValidationResult checkSmallerThanMin(
            V value, V minValue) {
        return checkSmallerThanMin("", value, minValue);
    }

    /**
     * Checks if the given value is smaller than the minimum value and returns a
     * ValidationResult with an empty error message if it is.
     *
     * @param <V>
     *            the type of the component value
     * @param errorMessage
     *            the error message to use if the validation fails
     * @param value
     *            the current value set on the component
     * @param minValue
     *            the minimum value set on the component
     * @return {@code ValidationResult.ok()} if the validation passes,
     *         {@code ValidationResult.error()} otherwise
     */
    public static <V extends Comparable<V>> ValidationResult checkSmallerThanMin(
            String errorMessage, V value, V minValue) {
        boolean isError = value != null && minValue != null
                && value.compareTo(minValue) < 0;
        return isError ? ValidationResult.error(errorMessage)
                : ValidationResult.ok();
    }
}
