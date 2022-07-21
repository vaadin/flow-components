package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.Objects;

public class TextFieldValidationTest
        extends AbstractTextFieldValidationTest<String, TextField> {

    @Override
    protected void initField() {
        field = new TextField();
        field.setMaxLength(8);
    }

    @Override
    protected void setValidValue() {
        field.setValue("AAAA");
    }

    @Override
    protected void setComponentInvalidValue() {
        field.setValue("AAAAAAAAAA");
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
