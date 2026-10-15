/**
 * Copyright 2000-2024 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.flow.component.textfield.validation;

import java.util.Objects;

import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.function.SerializablePredicate;

public class EmailFieldBinderValidationTest
        extends AbstractBinderValidationTest<String, EmailField> {

    @Override
    protected void initField() {
        field = new EmailField();
        // To disable pattern validation
        field.setPattern(null);
        field.setMaxLength(20);
    }

    @Override
    protected void setValidValue() {
        field.setValue("contact@example.com");
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue("reallylongemail@example.com");
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue("contact@another.com");
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "")
                || value.contains("@example.com");
    }
}
