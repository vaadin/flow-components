package com.vaadin.flow.component.textfield.binder;

import java.util.Objects;

import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.function.SerializablePredicate;

public class PasswordFieldValidationTest
        extends AbstractTextFieldValidationTest<String, PasswordField> {

    @Override
    protected void initField() {
        field = new PasswordField();
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