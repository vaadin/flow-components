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
package com.vaadin.flow.component.textfield;

import java.io.Serializable;
import java.util.regex.Pattern;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.shared.ValidationUtil;
import com.vaadin.flow.data.binder.ValidationResult;

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
     * @see TextField#setRequired
     */
    void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @see TextField#setMinlength(double)
     */
    void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * @see TextField#setMaxlength(double)
     */
    void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @see TextField#setPattern(String)
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
        var requiredValidation = ValidationUtil.validateRequiredConstraint("",
                required, value, field.getEmptyValue());

        return requiredValidation.isError() || checkValidity(value).isError();
    }

    ValidationResult checkValidity(String value) {

        final boolean isMaxLengthExceeded = value != null && maxLength != null
                && value.length() > maxLength;
        if (isMaxLengthExceeded) {
            return ValidationResult.error("");
        }

        final boolean isMinLengthNotReached = value != null && !value.isEmpty()
                && minLength != null && value.length() < minLength;
        if (isMinLengthNotReached) {
            return ValidationResult.error("");
        }

        final boolean valueViolatePattern = value != null && !value.isEmpty()
                && pattern != null && !pattern.matcher(value).matches();
        if (valueViolatePattern) {
            return ValidationResult.error("");
        }

        return ValidationResult.ok();
    }

}
