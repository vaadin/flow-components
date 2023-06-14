package com.vaadin.flow.component.textfield.binder;

import java.util.Objects;

import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.function.SerializablePredicate;

public class TextAreaValidationTest
        extends AbstractTextFieldValidationTest<String, TextArea> {

    @Override
    protected void initField() {
        field = new TextArea();
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