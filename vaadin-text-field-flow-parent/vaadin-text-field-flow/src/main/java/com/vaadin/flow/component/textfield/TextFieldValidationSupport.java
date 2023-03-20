/**
 * Copyright (C) 2000-2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full
 * license.
 */
package com.vaadin.flow.component.textfield;

import com.vaadin.flow.component.HasValue;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

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

    TextFieldValidationSupport(HasValue<?, String> field) {
        this.field = field;
    }

    /**
     * @see GeneratedVaadinTextField#setRequired
     */
    void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @see GeneratedVaadinTextField#setMinlength(double)
     */
    void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * @see GeneratedVaadinTextField#setMaxlength(double)
     */
    void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @see GeneratedVaadinTextField#setPattern(String)
     */
    void setPattern(String pattern) {
        this.pattern = pattern == null || pattern.isEmpty() ? null
                : Pattern.compile(pattern);
    }

    /**
     * Test if value is invalid for the field.
     *
     * @param value
     *            value to be tested.
     * @return <code>true</code> if the value is invalid.
     */
    boolean isInvalid(String value) {
        final boolean isRequiredButEmpty = required
                && Objects.equals(field.getEmptyValue(), value);
        final boolean isMaxLengthExceeded = value != null && maxLength != null
                && value.length() > maxLength;
        final boolean isMinLengthNotReached = value != null && minLength != null
                && value.length() < minLength;
        // Only evaluate if necessary.
        final BooleanSupplier doesValueViolatePattern = () -> value != null
                && pattern != null && !pattern.matcher(value).matches();
        return isRequiredButEmpty || isMaxLengthExceeded
                || isMinLengthNotReached
                || doesValueViolatePattern.getAsBoolean();
    }

}
