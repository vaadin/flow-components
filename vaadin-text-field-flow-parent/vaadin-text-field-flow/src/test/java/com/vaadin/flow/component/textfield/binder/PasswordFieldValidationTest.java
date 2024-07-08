/*
 * Copyright (C) 2024 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>}  for the full
 * license.
 */
package com.vaadin.flow.component.textfield.binder;

import java.util.Objects;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.function.SerializablePredicate;

public class PasswordFieldValidationTest
        extends AbstractTextFieldValidationTest<String, PasswordField> {

    @Tag("test-password-field")
    private class TestPasswordField extends PasswordField {
        protected boolean isEnforcedFieldValidationEnabled() {
            return true;
        }
    }

    @Override
    protected void initField() {
        field = new TestPasswordField();
        field.setMaxLength(10);
    }

    @Override
    protected void setValidValue() {
        field.setValue("AAAA");
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue("AAAAAAAAAAAAAA");
    }

    @Override
    protected void setBinderInvalidValue() {
        field.setValue("A");
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "") || value.length() > 2;
    }
}