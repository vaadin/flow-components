package com.vaadin.flow.component.textfield.binder;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.function.SerializablePredicate;

import java.util.Objects;

public class TextFieldValidationTest
        extends AbstractTextFieldValidationTest<String> {

    private TextField field;

    @Override
    protected HasValue<?, String> getField() {
        if (field == null) {
            field = new TextField();
            field.setMaxLength(8);
        }
        return field;
    }

    @Override
    protected void setValidValue() {
        getField().setValue("AAAA");
    }

    @Override
    protected void setComponentInvalidValue() {
        getField().setValue("AAAAAAAAAA");
    }

    @Override
    protected void setBinderInvalidValue() {
        getField().setValue("A");
    }

    @Override
    protected SerializablePredicate<? super String> getValidator() {
        return value -> Objects.equals(value, "") || value.length() > 2;
    }
}
