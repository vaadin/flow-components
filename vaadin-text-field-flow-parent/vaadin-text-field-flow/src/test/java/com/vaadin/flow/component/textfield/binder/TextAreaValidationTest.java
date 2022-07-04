package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.Objects;

public class TextAreaValidationTest
        extends AbstractTextFieldValidationTest<String> {

    private TextArea field;

    @Override
    protected HasValue<?, String> getField() {
        if (field == null) {
            field = new TextArea();
            field.setMaxLength(10);
        }
        return field;
    }

    @Override
    protected void setValidValue() {
        getField().setValue("AAAA");
    }

    @Override
    protected void setComponentInvalidValue() {
        getField().setValue("AAAAAAAAAAAAAA");
    }

    @Override
    protected void setBinderInvalidValue() {
        getField().setValue("A");
    }

    @Override
    protected void setEmptyValue() {
        getField().clear();
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "") || value.length() > 2;
    }
}
